package com.enterprise.ecommerce.payment.repository;

import com.enterprise.ecommerce.payment.entity.Payment;
import com.enterprise.ecommerce.payment.entity.PaymentStatus;
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
 * Repository interface for Payment entity
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by payment ID
     */
    Optional<Payment> findByPaymentId(String paymentId);

    /**
     * Find payments by order ID
     */
    List<Payment> findByOrderId(Long orderId);

    /**
     * Find payments by user ID
     */
    Page<Payment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find payments by status
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Find payments by user ID and status
     */
    List<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status);

    /**
     * Find payments by order ID and status
     */
    List<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status);

    /**
     * Find payments created between dates
     */
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
    List<Payment> findPaymentsBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Find payments by transaction ID
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Count payments by status
     */
    long countByStatus(PaymentStatus status);

    /**
     * Count payments by user ID
     */
    long countByUserId(Long userId);

    /**
     * Find successful payments by user ID
     */
    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.status = 'COMPLETED' ORDER BY p.createdAt DESC")
    List<Payment> findSuccessfulPaymentsByUserId(@Param("userId") Long userId);

    /**
     * Find failed payments that can be retried
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.createdAt > :cutoffDate")
    List<Payment> findRetriableFailedPayments(@Param("cutoffDate") LocalDateTime cutoffDate);
}