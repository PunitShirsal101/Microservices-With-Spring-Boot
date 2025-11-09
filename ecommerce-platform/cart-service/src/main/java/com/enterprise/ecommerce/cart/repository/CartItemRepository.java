package com.enterprise.ecommerce.cart.repository;

import com.enterprise.ecommerce.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CartItem entity
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find cart item by cart ID and product ID
     */
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    /**
     * Find all cart items by cart ID
     */
    List<CartItem> findByCartId(Long cartId);

    /**
     * Count items in cart
     */
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Long countByCartId(@Param("cartId") Long cartId);

    /**
     * Delete all cart items by cart ID
     */
    void deleteByCartId(Long cartId);

    /**
     * Find cart items by product ID
     */
    List<CartItem> findByProductId(Long productId);
}