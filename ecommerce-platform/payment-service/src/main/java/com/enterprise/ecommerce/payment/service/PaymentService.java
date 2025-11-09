package com.enterprise.ecommerce.payment.service;

import com.enterprise.ecommerce.common.exception.InvalidRequestException;
import com.enterprise.ecommerce.common.exception.ResourceNotFoundException;
import com.enterprise.ecommerce.common.kafka.KafkaProducerService;
import com.enterprise.ecommerce.common.events.PaymentProcessedEvent;
import com.enterprise.ecommerce.payment.dto.PaymentResponse;
import com.enterprise.ecommerce.payment.dto.ProcessPaymentRequest;
import com.enterprise.ecommerce.payment.entity.Payment;
import com.enterprise.ecommerce.payment.entity.PaymentMethod;
import com.enterprise.ecommerce.payment.entity.PaymentStatus;
import com.enterprise.ecommerce.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for payment processing and management
 */
@Service
@Transactional
@Slf4j
public class PaymentService {

    private static final String PAYMENT_NOT_FOUND_MESSAGE = "Payment not found with id: ";

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;
    private final KafkaProducerService kafkaProducerService;

    public PaymentService(PaymentRepository paymentRepository, 
                        RestTemplate restTemplate,
                        KafkaProducerService kafkaProducerService) {
        this.paymentRepository = paymentRepository;
        this.restTemplate = restTemplate;
        this.kafkaProducerService = kafkaProducerService;
    }

    /**
     * Process a payment
     */
    public PaymentResponse processPayment(ProcessPaymentRequest request) {
        // Validate order exists and amount matches
        validatePaymentRequest(request);

        // Create payment record
        Payment payment = new Payment();
        payment.setPaymentId(generatePaymentNumber());
        payment.setOrderId(request.getOrderId());
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.PROCESSING);

        // Save initial payment record
        Payment savedPayment = paymentRepository.save(payment);

        try {
            // Process payment through gateway
            PaymentGatewayResponse gatewayResponse = processPaymentThroughGateway(request, savedPayment);
            
            // Update payment based on gateway response
            updatePaymentFromGatewayResponse(savedPayment, gatewayResponse);
            
            // Update order status if payment successful
            if (savedPayment.getStatus() == PaymentStatus.COMPLETED) {
                updateOrderStatus(request.getOrderId(), "CONFIRMED");
            }

        } catch (Exception e) {
            // Handle payment failure
            savedPayment.setStatus(PaymentStatus.FAILED);
            savedPayment.setFailureReason(e.getMessage());
            savedPayment.setUpdatedAt(LocalDateTime.now());
        }

        // Save final payment state
        Payment finalPayment = paymentRepository.save(savedPayment);
        
        // Publish payment processed event
        PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                .paymentId(finalPayment.getId().toString())
                .orderId(finalPayment.getOrderId().toString())
                .userId(finalPayment.getUserId().toString())
                .amount(finalPayment.getAmount())
                .currency("USD")
                .paymentMethod(finalPayment.getPaymentMethod().name())
                .status(convertToEventStatus(finalPayment.getStatus()))
                .processedAt(finalPayment.getUpdatedAt() != null ? finalPayment.getUpdatedAt() : LocalDateTime.now())
                .build();
        
        kafkaProducerService.sendMessage("payment-events", finalPayment.getId().toString(), event);
        log.info("Published PaymentProcessedEvent for payment: {}", finalPayment.getId());
        
        return convertToPaymentResponse(finalPayment);
    }

    /**
     * Get payment by ID
     */
    @Cacheable(value = "payments", key = "#paymentId")
    @Transactional(readOnly = true)
    public Optional<PaymentResponse> getPaymentById(Long paymentId) {
        if (paymentId == null) {
            return Optional.empty();
        }
        return paymentRepository.findById(paymentId)
                .map(this::convertToPaymentResponse);
    }

    /**
     * Get payment by payment ID
     */
    @Transactional(readOnly = true)
    public Optional<PaymentResponse> getPaymentByPaymentId(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId)
                .map(this::convertToPaymentResponse);
    }

    /**
     * Get payments by order ID
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .stream()
                .map(this::convertToPaymentResponse)
                .toList();
    }

    /**
     * Get payments by user ID
     */
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByUserId(Long userId, Pageable pageable) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::convertToPaymentResponse);
    }

    /**
     * Get payments by status
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status)
                .stream()
                .map(this::convertToPaymentResponse)
                .toList();
    }

    /**
     * Refund a payment
     */
    @CacheEvict(value = "payments", key = "#paymentId")
    public PaymentResponse refundPayment(Long paymentId, BigDecimal refundAmount, String reason) {
        if (paymentId == null) {
            throw new IllegalArgumentException("Payment ID cannot be null");
        }
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(PAYMENT_NOT_FOUND_MESSAGE + paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new InvalidRequestException("Cannot refund payment with status: " + payment.getStatus());
        }

        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new InvalidRequestException("Refund amount cannot exceed payment amount");
        }

        try {
            // Process refund through gateway
            PaymentGatewayResponse refundResponse = processRefundThroughGateway(payment, refundAmount, reason);
            
            // Update payment status
            if (refundAmount.compareTo(payment.getAmount()) == 0) {
                payment.setStatus(PaymentStatus.REFUNDED);
            } else {
                payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
            }
            
            payment.setUpdatedAt(LocalDateTime.now());
            payment.setGatewayResponse(refundResponse.getResponseMessage());

        } catch (Exception e) {
            throw new InvalidRequestException("Refund processing failed: " + e.getMessage());
        }

        Payment updatedPayment = paymentRepository.save(payment);
        return convertToPaymentResponse(updatedPayment);
    }

    /**
     * Cancel a pending payment
     */
    public PaymentResponse cancelPayment(Long paymentId, String reason) {
        if (paymentId == null) {
            throw new IllegalArgumentException("Payment ID cannot be null");
        }
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(PAYMENT_NOT_FOUND_MESSAGE + paymentId));

        if (payment.getStatus() != PaymentStatus.PENDING && payment.getStatus() != PaymentStatus.PROCESSING) {
            throw new InvalidRequestException("Cannot cancel payment with status: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setFailureReason(reason);
        payment.setUpdatedAt(LocalDateTime.now());

        Payment cancelledPayment = paymentRepository.save(payment);
        return convertToPaymentResponse(cancelledPayment);
    }

    /**
     * Retry a failed payment
     */
    public PaymentResponse retryPayment(Long paymentId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("Payment ID cannot be null");
        }
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(PAYMENT_NOT_FOUND_MESSAGE + paymentId));

        if (payment.getStatus() != PaymentStatus.FAILED) {
            throw new InvalidRequestException("Can only retry failed payments");
        }

        // Create new payment request from existing payment
        ProcessPaymentRequest retryRequest = new ProcessPaymentRequest();
        retryRequest.setOrderId(payment.getOrderId());
        retryRequest.setUserId(payment.getUserId());
        retryRequest.setAmount(payment.getAmount());
        retryRequest.setPaymentMethod(payment.getPaymentMethod());

        return processPayment(retryRequest);
    }

    // Private helper methods

    private void validatePaymentRequest(ProcessPaymentRequest request) {
        // Validate order exists and get order details
        try {
            String orderUrl = "http://order-service/api/orders/" + request.getOrderId();
            Object order = restTemplate.getForObject(orderUrl, Object.class);
            if (order == null) {
                throw new InvalidRequestException("Order not found with id: " + request.getOrderId());
            }
        } catch (Exception e) {
            throw new InvalidRequestException("Order validation failed: " + e.getMessage());
        }

        // Validate payment method specific requirements
        validatePaymentMethodRequirements(request);
    }

    private void validatePaymentMethodRequirements(ProcessPaymentRequest request) {
        PaymentMethod method = request.getPaymentMethod();
        
        switch (method) {
            case CREDIT_CARD, DEBIT_CARD:
                validateCardPaymentRequirements(request);
                break;
            case PAYPAL, STRIPE:
                validateTokenBasedPaymentRequirements(request, method);
                break;
            case APPLE_PAY, GOOGLE_PAY:
                validateTokenBasedPaymentRequirements(request, method);
                break;
            case BANK_TRANSFER:
                // Bank transfer typically requires additional verification
                break;
            case CRYPTOCURRENCY:
                validateCryptocurrencyRequirements(request);
                break;
            case CASH_ON_DELIVERY:
                // No additional validation required
                break;
        }
    }

    private void validateCardPaymentRequirements(ProcessPaymentRequest request) {
        if (request.getCardNumber() == null || request.getCardNumber().trim().isEmpty()) {
            throw new InvalidRequestException("Card number is required for card payments");
        }
        if (request.getExpiryMonth() == null || request.getExpiryYear() == null) {
            throw new InvalidRequestException("Card expiry date is required");
        }
        if (request.getCvv() == null || request.getCvv().trim().isEmpty()) {
            throw new InvalidRequestException("CVV is required for card payments");
        }
    }

    private void validateTokenBasedPaymentRequirements(ProcessPaymentRequest request, PaymentMethod method) {
        if (method == PaymentMethod.PAYPAL || method == PaymentMethod.STRIPE) {
            if (request.getPaymentToken() == null && request.getPaymentMethodId() == null) {
                throw new InvalidRequestException("Payment token or method ID is required for " + method.getDisplayName());
            }
        } else if (request.getPaymentToken() == null) {
            throw new InvalidRequestException("Payment token is required for " + method.getDisplayName());
        }
    }

    private void validateCryptocurrencyRequirements(ProcessPaymentRequest request) {
        if (request.getPaymentToken() == null) {
            throw new InvalidRequestException("Wallet address or payment token is required for cryptocurrency");
        }
    }

    private PaymentGatewayResponse processPaymentThroughGateway(ProcessPaymentRequest request, Payment payment) {
        // Mock payment gateway processing
        // In real implementation, integrate with actual payment gateways like Stripe, PayPal, etc.
        log.info("Processing payment through gateway for order {} with amount {}", 
                request.getOrderId(), payment.getAmount());
        
        PaymentGatewayResponse response = new PaymentGatewayResponse();
        
        try {
            Thread.sleep(1000); // Simulate processing time
            
            // Mock success/failure logic (90% success rate)
            if (Math.random() < 0.9) {
                response.setSuccess(true);
                response.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
                response.setResponseMessage("Payment processed successfully");
                response.setGatewayReference("GW-" + System.currentTimeMillis());
            } else {
                response.setSuccess(false);
                response.setResponseMessage("Payment declined by bank");
                response.setErrorCode("DECLINED");
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            response.setSuccess(false);
            response.setResponseMessage("Payment processing timeout");
            response.setErrorCode("TIMEOUT");
        }
        
        return response;
    }

    private PaymentGatewayResponse processRefundThroughGateway(Payment payment, BigDecimal refundAmount, String reason) {
        // Mock refund processing
        log.info("Processing refund through gateway for payment {} with amount {} and reason: {}", 
                payment.getId(), refundAmount, reason);
        
        PaymentGatewayResponse response = new PaymentGatewayResponse();
        response.setSuccess(true);
        response.setTransactionId("REFUND-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        response.setResponseMessage("Refund processed successfully");
        return response;
    }

    private void updatePaymentFromGatewayResponse(Payment payment, PaymentGatewayResponse response) {
        if (response.isSuccess()) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId(response.getTransactionId());
            payment.setProcessedAt(LocalDateTime.now());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(response.getResponseMessage());
        }
        
        payment.setGatewayResponse(response.getResponseMessage());
        payment.setUpdatedAt(LocalDateTime.now());
    }

    private void updateOrderStatus(Long orderId, String status) {
        try {
            // In real implementation, make REST call to update order status
            log.info("Updating order {} status to {}", orderId, status);
        } catch (Exception e) {
            log.error("Failed to update order status: {}", e.getMessage());
        }
    }

    private String generatePaymentNumber() {
        return "PAY-" + System.currentTimeMillis() + "-" + 
               UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private PaymentResponse convertToPaymentResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setPaymentNumber(payment.getPaymentId());
        response.setOrderId(payment.getOrderId());
        response.setUserId(payment.getUserId());
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setTransactionId(payment.getTransactionId());
        response.setFailureReason(payment.getFailureReason());
        response.setProcessedAt(payment.getProcessedAt());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        return response;
    }

    /**
     * Convert PaymentStatus to PaymentProcessedEvent.PaymentStatus
     */
    private PaymentProcessedEvent.PaymentStatus convertToEventStatus(PaymentStatus status) {
        return switch (status) {
            case COMPLETED -> PaymentProcessedEvent.PaymentStatus.SUCCESS;
            case FAILED -> PaymentProcessedEvent.PaymentStatus.FAILED;
            case CANCELLED -> PaymentProcessedEvent.PaymentStatus.CANCELLED;
            default -> PaymentProcessedEvent.PaymentStatus.PENDING;
        };
    }

    // Inner class for gateway response
    private static class PaymentGatewayResponse {
        private boolean success;
        private String transactionId;
        private String responseMessage;
        private String errorCode;
        private String gatewayReference;

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public String getResponseMessage() { return responseMessage; }
        public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }
        @SuppressWarnings("unused")
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        @SuppressWarnings("unused")
        public String getGatewayReference() { return gatewayReference; }
        public void setGatewayReference(String gatewayReference) { this.gatewayReference = gatewayReference; }
    }
}