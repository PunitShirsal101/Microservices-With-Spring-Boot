package com.enterprise.ecommerce.cart.service;

import com.enterprise.ecommerce.cart.dto.*;
import com.enterprise.ecommerce.cart.entity.Cart;
import com.enterprise.ecommerce.cart.entity.CartItem;
import com.enterprise.ecommerce.cart.repository.CartRepository;
import com.enterprise.ecommerce.cart.repository.CartItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}

class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(String message) {
        super(message);
    }
}

class UnauthorizedCartAccessException extends RuntimeException {
    public UnauthorizedCartAccessException(String message) {
        super(message);
    }
}

/**
 * Service class for cart management
 */
@Service
@Transactional
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductClient productClient;

    public CartService(CartRepository cartRepository, 
                      CartItemRepository cartItemRepository,
                      ProductClient productClient) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productClient = productClient;
    }

    /**
     * Get cart by user ID
     */
    @Cacheable(value = "carts", key = "#userId")
    @Transactional(readOnly = true)
    public CartResponse getCartByUserId(Long userId) {
        logger.info("Getting cart for user: {}", userId);
        
        Optional<Cart> cartOpt = cartRepository.findByUserIdWithItems(userId);
        
        if (cartOpt.isEmpty()) {
            // Create empty cart if not exists
            Cart cart = new Cart(userId);
            cart = cartRepository.save(cart);
            return mapToCartResponse(cart);
        }
        
        Cart cart = cartOpt.get();
        return mapToCartResponse(cart);
    }

    /**
     * Add item to cart
     */
    @CacheEvict(value = {"carts", "cartCounts"}, key = "#userId")
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        logger.info("Adding item to cart for user: {}, productId: {}, quantity: {}", 
                   userId, request.getProductId(), request.getQuantity());

        // Get or create cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(new Cart(userId));
        
        if (cart.getId() == null) {
            cart = cartRepository.save(cart);
        }

        // Get product details
        ProductClient.ProductResponse product = productClient.getProductById(request.getProductId());
        if (product == null) {
            throw new ProductNotFoundException("Product not found: " + request.getProductId());
        }

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), request.getProductId());

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            item.calculateSubtotal();
            cartItemRepository.save(item);
        } else {
            // Add new item
            CartItem newItem = new CartItem(
                product.getId(),
                product.getName(),
                product.getPrice(),
                request.getQuantity()
            );
            newItem.setProductImageUrl(product.getImageUrl());
            newItem.setCart(cart);
            cartItemRepository.save(newItem);
        }

        // Recalculate cart totals
        cart = cartRepository.findByUserIdWithItems(userId).orElseThrow();
        cart.recalculateTotal();
        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    /**
     * Update cart item quantity
     */
    @CacheEvict(value = {"carts", "cartCounts"}, key = "#userId")
    public CartResponse updateCartItem(Long userId, Long itemId, UpdateCartItemRequest request) {
        logger.info("Updating cart item: {} for user: {}, new quantity: {}", 
                   itemId, userId, request.getQuantity());

        if (itemId == null) {
            throw new CartItemNotFoundException("Cart item ID cannot be null");
        }

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found: " + itemId));

        // Verify cart belongs to user
        if (!Objects.equals(item.getCart().getUserId(), userId)) {
            throw new UnauthorizedCartAccessException("Unauthorized access to cart item");
        }

        item.setQuantity(request.getQuantity());
        item.calculateSubtotal();
        cartItemRepository.save(item);

        // Recalculate cart totals
        Cart cart = item.getCart();
        cart.recalculateTotal();
        cartRepository.save(cart);

        return mapToCartResponse(cartRepository.findByUserIdWithItems(userId).orElseThrow());
    }

    /**
     * Remove item from cart
     */
    @CacheEvict(value = {"carts", "cartCounts"}, key = "#userId")
    public CartResponse removeFromCart(Long userId, Long itemId) {
        logger.info("Removing item: {} from cart for user: {}", itemId, userId);

        if (itemId == null) {
            throw new CartItemNotFoundException("Cart item ID cannot be null");
        }

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found: " + itemId));

        // Verify cart belongs to user
        if (!Objects.equals(item.getCart().getUserId(), userId)) {
            throw new UnauthorizedCartAccessException("Unauthorized access to cart item");
        }

        cartItemRepository.delete(item);

        // Recalculate cart totals
        Cart cart = cartRepository.findByUserIdWithItems(userId).orElseThrow();
        cart.recalculateTotal();
        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    /**
     * Clear cart
     */
    @CacheEvict(value = {"carts", "cartCounts"}, key = "#userId")
    public void clearCart(Long userId) {
        logger.info("Clearing cart for user: {}", userId);

        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            cartItemRepository.deleteByCartId(cart.getId());
            cart.clearItems();
            cartRepository.save(cart);
        }
    }

    /**
     * Get cart item count
     */
    @Cacheable(value = "cartCounts", key = "#userId")
    @Transactional(readOnly = true)
    public Long getCartItemCount(Long userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        if (cartOpt.isPresent()) {
            return cartItemRepository.countByCartId(cartOpt.get().getId());
        }
        return 0L;
    }

    /**
     * Map Cart entity to CartResponse DTO
     */
    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::mapToCartItemResponse)
                .toList();

        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                cart.getTotalAmount(),
                cart.getTotalItems(),
                itemResponses,
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }

    /**
     * Map CartItem entity to CartItemResponse DTO
     */
    private CartItemResponse mapToCartItemResponse(CartItem item) {
        CartItemResponse response = new CartItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProductId());
        response.setProductName(item.getProductName());
        response.setProductImageUrl(item.getProductImageUrl());
        response.setPrice(item.getPrice());
        response.setQuantity(item.getQuantity());
        response.setSubtotal(item.getSubtotal());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
        return response;
    }
}