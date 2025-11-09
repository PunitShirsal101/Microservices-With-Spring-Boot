package com.enterprise.ecommerce.order.repository;

import com.enterprise.ecommerce.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for OrderItem entity
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find order items by order ID
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * Find order items by product ID
     */
    List<OrderItem> findByProductId(Long productId);

    /**
     * Count items in order
     */
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.order.id = :orderId")
    Long countByOrderId(@Param("orderId") Long orderId);

    /**
     * Delete all order items by order ID
     */
    void deleteByOrderId(Long orderId);

    /**
     * Find order items by order ID and product ID
     */
    List<OrderItem> findByOrderIdAndProductId(Long orderId, Long productId);
}