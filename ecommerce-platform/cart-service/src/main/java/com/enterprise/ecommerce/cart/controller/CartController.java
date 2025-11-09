package com.enterprise.ecommerce.cart.controller;

import com.enterprise.ecommerce.cart.dto.*;
import com.enterprise.ecommerce.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for cart management
 */
@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "Shopping cart management API")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Operation(summary = "Get user's cart", description = "Retrieve the shopping cart for the specified user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<CartResponse> getCart(
            @Parameter(description = "User ID", required = true) 
            @PathVariable Long userId) {
        
        logger.info("GET request to fetch cart for user: {}", userId);
        
        try {
            CartResponse cart = cartService.getCartByUserId(userId);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            logger.error("Error fetching cart for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Add item to cart", description = "Add a product to the user's shopping cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item added to cart successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PostMapping("/user/{userId}/items")
    public ResponseEntity<CartResponse> addToCart(
            @Parameter(description = "User ID", required = true) 
            @PathVariable Long userId,
            @Valid @RequestBody AddToCartRequest request) {
        
        logger.info("POST request to add item to cart for user: {}, product: {}", 
                   userId, request.getProductId());
        
        try {
            CartResponse cart = cartService.addToCart(userId, request);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            logger.error("Error adding item to cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error adding item to cart for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Update cart item", description = "Update the quantity of an item in the cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cart item updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @PutMapping("/user/{userId}/items/{itemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @Parameter(description = "User ID", required = true) 
            @PathVariable Long userId,
            @Parameter(description = "Cart item ID", required = true) 
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        
        logger.info("PUT request to update cart item: {} for user: {}", itemId, userId);
        
        try {
            CartResponse cart = cartService.updateCartItem(userId, itemId, request);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            logger.error("Error updating cart item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error updating cart item: {} for user: {}", itemId, userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Remove item from cart", description = "Remove an item from the user's shopping cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item removed from cart successfully"),
        @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @DeleteMapping("/user/{userId}/items/{itemId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @Parameter(description = "User ID", required = true) 
            @PathVariable Long userId,
            @Parameter(description = "Cart item ID", required = true) 
            @PathVariable Long itemId) {
        
        logger.info("DELETE request to remove item: {} from cart for user: {}", itemId, userId);
        
        try {
            CartResponse cart = cartService.removeFromCart(userId, itemId);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            logger.error("Error removing item from cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error removing item: {} from cart for user: {}", itemId, userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Clear cart", description = "Remove all items from the user's shopping cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cart cleared successfully"),
        @ApiResponse(responseCode = "404", description = "Cart not found")
    })
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> clearCart(
            @Parameter(description = "User ID", required = true) 
            @PathVariable Long userId) {
        
        logger.info("DELETE request to clear cart for user: {}", userId);
        
        try {
            cartService.clearCart(userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error clearing cart for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get cart item count", description = "Get the total number of items in the user's cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item count retrieved successfully")
    })
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> getCartItemCount(
            @Parameter(description = "User ID", required = true) 
            @PathVariable Long userId) {
        
        logger.info("GET request to fetch cart item count for user: {}", userId);
        
        try {
            Long count = cartService.getCartItemCount(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            logger.error("Error fetching cart item count for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}