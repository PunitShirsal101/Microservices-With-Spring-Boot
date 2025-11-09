package com.enterprise.ecommerce.payment.controller;

import com.enterprise.ecommerce.payment.dto.PaymentResponse;
import com.enterprise.ecommerce.payment.dto.ProcessPaymentRequest;
import com.enterprise.ecommerce.payment.entity.PaymentMethod;
import com.enterprise.ecommerce.payment.entity.PaymentStatus;
import com.enterprise.ecommerce.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for payment processing and management
 */
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment", description = "Payment processing and management API")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Process a payment
     */
    @PostMapping("/process")
    @Operation(summary = "Process a payment")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody ProcessPaymentRequest request) {
        try {
            PaymentResponse payment = paymentService.processPayment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get payment by ID
     */
    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long paymentId) {
        Optional<PaymentResponse> payment = paymentService.getPaymentById(paymentId);
        return payment.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get payment by payment number
     */
    @GetMapping("/number/{paymentNumber}")
    @Operation(summary = "Get payment by payment number")
    public ResponseEntity<PaymentResponse> getPaymentByNumber(@PathVariable String paymentNumber) {
        Optional<PaymentResponse> payment = paymentService.getPaymentByPaymentId(paymentNumber);
        return payment.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get payments by order ID
     */
    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payments by order ID")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrderId(@PathVariable Long orderId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Get payments by user ID
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get payments by user ID")
    public ResponseEntity<Page<PaymentResponse>> getPaymentsByUserId(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId, pageable);
        return ResponseEntity.ok(payments);
    }

    /**
     * Get payments by status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get payments by status")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        List<PaymentResponse> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }

    /**
     * Refund a payment
     */
    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Refund a payment")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Long paymentId,
            @RequestParam BigDecimal refundAmount,
            @RequestParam(required = false) String reason) {
        try {
            PaymentResponse payment = paymentService.refundPayment(paymentId, refundAmount, reason);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cancel a payment
     */
    @PutMapping("/{paymentId}/cancel")
    @Operation(summary = "Cancel a payment")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable Long paymentId,
            @RequestParam(required = false) String reason) {
        try {
            PaymentResponse payment = paymentService.cancelPayment(paymentId, reason);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Retry a failed payment
     */
    @PostMapping("/{paymentId}/retry")
    @Operation(summary = "Retry a failed payment")
    public ResponseEntity<PaymentResponse> retryPayment(@PathVariable Long paymentId) {
        try {
            PaymentResponse payment = paymentService.retryPayment(paymentId);
            return ResponseEntity.status(HttpStatus.CREATED).body(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get available payment methods
     */
    @GetMapping("/methods")
    @Operation(summary = "Get available payment methods")
    public ResponseEntity<PaymentMethod[]> getPaymentMethods() {
        return ResponseEntity.ok(PaymentMethod.values());
    }

    /**
     * Get available payment statuses
     */
    @GetMapping("/statuses")
    @Operation(summary = "Get available payment statuses")
    public ResponseEntity<PaymentStatus[]> getPaymentStatuses() {
        return ResponseEntity.ok(PaymentStatus.values());
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Payment Service is running");
    }
}