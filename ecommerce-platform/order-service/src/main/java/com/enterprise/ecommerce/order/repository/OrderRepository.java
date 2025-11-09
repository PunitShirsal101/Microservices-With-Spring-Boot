package com.enterprise.ecommerce.order.repository;

import com.enterprise.ecommerce.order.entity.Order;
import com.enterprise.ecommerce.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order entity
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find order by order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find orders by user ID
     */
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find orders by user ID and status
     */
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    /**
     * Find orders by status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Find orders by status ordered by creation date descending
     */
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    /**
     * Find all orders ordered by creation date descending
     */
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find order by user ID and order ID
     */
    Optional<Order> findByIdAndUserId(Long id, Long userId);

    /**
     * Find orders created between dates
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Find orders by user ID with items loaded
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.userId = :userId")
    List<Order> findByUserIdWithItems(@Param("userId") Long userId);

    /**
     * Count orders by status
     */
    long countByStatus(OrderStatus status);

    /**
     * Check if order exists by order number
     */
    boolean existsByOrderNumber(String orderNumber);
}