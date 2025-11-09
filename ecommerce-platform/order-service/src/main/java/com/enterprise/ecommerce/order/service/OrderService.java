package com.enterprise.ecommerce.order.service;

import com.enterprise.ecommerce.common.exception.InvalidRequestException;
import com.enterprise.ecommerce.common.exception.ResourceNotFoundException;
import com.enterprise.ecommerce.common.kafka.KafkaProducerService;
import com.enterprise.ecommerce.common.events.OrderPlacedEvent;
import com.enterprise.ecommerce.order.dto.*;
import com.enterprise.ecommerce.order.entity.Order;
import com.enterprise.ecommerce.order.entity.OrderItem;
import com.enterprise.ecommerce.order.entity.OrderStatus;
import com.enterprise.ecommerce.order.repository.OrderRepository;
import com.enterprise.ecommerce.order.repository.OrderItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for order management
 */
@Service
@Transactional
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RestTemplate restTemplate;
    private final KafkaProducerService kafkaProducerService;

    // Tax and shipping constants
    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.08); // 8% tax
    private static final BigDecimal SHIPPING_RATE = BigDecimal.valueOf(9.99);
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = BigDecimal.valueOf(100.00);

    public OrderService(OrderRepository orderRepository, 
                       OrderItemRepository orderItemRepository,
                       RestTemplate restTemplate,
                       KafkaProducerService kafkaProducerService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.restTemplate = restTemplate;
        this.kafkaProducerService = kafkaProducerService;
    }

    /**
     * Create a new order
     */
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Validate cart items exist and get product details
        validateAndEnrichOrderItems(request.getItems());

        // Create order entity
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(request.getShippingAddress());
        order.setNotes(request.getNotes());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // Calculate amounts
        BigDecimal subtotal = calculateSubtotal(request.getItems());
        BigDecimal taxAmount = calculateTax(subtotal);
        BigDecimal shippingAmount = calculateShipping(subtotal);
        BigDecimal discountAmount = request.getDiscountAmount() != null ? 
            request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.add(taxAmount).add(shippingAmount).subtract(discountAmount);

        order.setTotalAmount(totalAmount);
        order.setTaxAmount(taxAmount);
        order.setShippingAmount(shippingAmount);
        order.setDiscountAmount(discountAmount);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Create and save order items
        List<OrderItem> orderItems = createOrderItems(savedOrder, request.getItems());
        if (orderItems != null && !orderItems.isEmpty()) {
            orderItemRepository.saveAll(orderItems);
        }

        // Clear user's cart (call cart service)
        clearUserCart(request.getUserId());

        // Publish order placed event
        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .orderId(savedOrder.getId().toString())
                .userId(savedOrder.getUserId().toString())
                .totalAmount(savedOrder.getTotalAmount())
                .currency("USD")
                .items(orderItems != null ? orderItems.stream()
                        .map((OrderItem item) -> OrderPlacedEvent.OrderItem.builder()
                                .productId(item.getProductId().toString())
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .totalPrice(item.getSubtotal())
                                .build())
                        .toList() : List.of())
                .orderedAt(savedOrder.getCreatedAt())
                .build();

        kafkaProducerService.sendMessage("order-events", savedOrder.getId().toString(), event);
        log.info("Published OrderPlacedEvent for order: {}", savedOrder.getId());

        return convertToOrderResponse(savedOrder, orderItems);
    }

    /**
     * Get order by ID
     */
    @Cacheable(value = "orders", key = "#orderId")
    @Transactional(readOnly = true)
    public Optional<OrderResponse> getOrderById(Long orderId) {
        if (orderId == null) {
            return Optional.empty();
        }
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
            return Optional.of(convertToOrderResponse(order, items));
        }
        return Optional.empty();
    }

    /**
     * Get orders by user ID
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return orders.map(order -> {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            return convertToOrderResponse(order, items);
        });
    }

    /**
     * Get all orders
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
        return orders.map(order -> {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            return convertToOrderResponse(order, items);
        });
    }

    /**
     * Update order status
     */
    @CacheEvict(value = "orders", key = "#orderId")
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(request.getStatus());
        order.setUpdatedAt(LocalDateTime.now());

        // Set specific timestamps based on status
        LocalDateTime now = LocalDateTime.now();
        switch (request.getStatus()) {
            case PENDING:
                // Order is pending
                break;
            case CONFIRMED:
                // Order confirmed, process payment
                break;
            case PROCESSING:
                // Order is being prepared
                break;
            case SHIPPED:
                order.setShippedAt(now);
                break;
            case DELIVERED:
                order.setDeliveredAt(now);
                break;
            case CANCELLED:
                order.setCancelledAt(now);
                break;
            case REFUNDED:
                // Order has been refunded
                break;
        }

        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            order.setNotes(order.getNotes() != null ? 
                order.getNotes() + "\n" + request.getNotes() : request.getNotes());
        }

        Order savedOrder = orderRepository.save(order);
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        // Send status update notification (integrate with notification service)
        sendStatusUpdateNotification(savedOrder, oldStatus, request.getStatus());

        return convertToOrderResponse(savedOrder, items);
    }

    /**
     * Cancel order
     */
    @CacheEvict(value = "orders", key = "#orderId")
    public OrderResponse cancelOrder(Long orderId, String reason) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidRequestException("Cannot cancel order with status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        if (reason != null && !reason.trim().isEmpty()) {
            order.setNotes(order.getNotes() != null ? 
                order.getNotes() + "\nCancellation reason: " + reason : "Cancellation reason: " + reason);
        }

        Order savedOrder = orderRepository.save(order);
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        return convertToOrderResponse(savedOrder, items);
    }

    /**
     * Get orders by status
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return orders.map(order -> {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            return convertToOrderResponse(order, items);
        });
    }

    // Private helper methods

    private void validateAndEnrichOrderItems(List<OrderItemRequest> items) {
        for (OrderItemRequest item : items) {
            // Validate product exists (call product service)
            try {
                String productUrl = "http://product-service/api/products/" + item.getProductId();
                restTemplate.getForObject(productUrl, Object.class);
            } catch (Exception e) {
                throw new ResourceNotFoundException("Product not found with id: " + item.getProductId());
            }
        }
    }

    private BigDecimal calculateSubtotal(List<OrderItemRequest> items) {
        return items.stream()
                .map(OrderItemRequest::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTax(BigDecimal subtotal) {
        return subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateShipping(BigDecimal subtotal) {
        return subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0 ? 
            BigDecimal.ZERO : SHIPPING_RATE;
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + 
               UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private List<OrderItem> createOrderItems(Order order, List<OrderItemRequest> itemRequests) {
        return itemRequests.stream().map(request -> {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(request.getProductId());
            item.setQuantity(request.getQuantity());
            item.setUnitPrice(request.getUnitPrice());
            item.setSubtotal(request.getSubtotal());
            return item;
        }).toList();
    }

    private void clearUserCart(Long userId) {
        try {
            String cartUrl = "http://cart-service/api/cart/user/" + userId + "/clear";
            restTemplate.delete(cartUrl);
        } catch (Exception e) {
            // Log warning but don't fail order creation
            log.warn("Warning: Could not clear user cart: {}", e.getMessage());
        }
    }

    private void sendStatusUpdateNotification(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        // Complete integration with notification service
        log.info("Order {} status changed from {} to {}", order.getOrderNumber(), oldStatus, newStatus);
    }

    private OrderResponse convertToOrderResponse(Order order, List<OrderItem> items) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setUserId(order.getUserId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setTaxAmount(order.getTaxAmount());
        response.setShippingAmount(order.getShippingAmount());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setShippingAddress(order.getShippingAddress());
        response.setNotes(order.getNotes());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setShippedAt(order.getShippedAt());
        response.setDeliveredAt(order.getDeliveredAt());
        response.setCancelledAt(order.getCancelledAt());

        List<OrderItemResponse> itemResponses = items != null ? items.stream().map(item -> {
            OrderItemResponse itemResponse = new OrderItemResponse();
            itemResponse.setId(item.getId());
            itemResponse.setProductId(item.getProductId());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setUnitPrice(item.getUnitPrice());
            itemResponse.setSubtotal(item.getSubtotal());
            return itemResponse;
        }).toList() : List.of();

        response.setItems(itemResponses);
        return response;
    }
}