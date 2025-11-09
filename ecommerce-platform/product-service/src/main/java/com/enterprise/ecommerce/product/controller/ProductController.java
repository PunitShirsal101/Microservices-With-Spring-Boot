package com.enterprise.ecommerce.product.controller;

import com.enterprise.ecommerce.common.dto.ApiResponse;
import com.enterprise.ecommerce.product.dto.*;
import com.enterprise.ecommerce.product.service.CategoryService;
import com.enterprise.ecommerce.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for product and category management
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Management", description = "APIs for product and category management")
public class ProductController {
    
    private final ProductService productService;
    private final CategoryService categoryService;
    
    // ===============================
    // PRODUCT ENDPOINTS
    // ===============================
    
    /**
     * Create a new product
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new product", description = "Creates a new product (Admin only)")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        
        log.info("Creating product: {}", request.getName());
        ProductResponse response = productService.createProduct(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Product created successfully"));
    }
    
    /**
     * Get product by ID
     */
    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID", description = "Retrieves a product by its ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable Long productId) {
        
        ProductResponse response = productService.getProductById(productId);
        return ResponseEntity.ok(ApiResponse.success(response, "Product retrieved successfully"));
    }
    
    /**
     * Get product by SKU
     */
    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Retrieves a product by its SKU")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductBySku(
            @PathVariable String sku) {
        
        ProductResponse response = productService.getProductBySku(sku);
        return ResponseEntity.ok(ApiResponse.success(response, "Product retrieved successfully"));
    }
    
    /**
     * Get all active products with pagination
     */
    @GetMapping
    @Operation(summary = "Get all active products", description = "Retrieves all active products with pagination")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        Page<ProductResponse> response = productService.getAllActiveProducts(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(response, "Products retrieved successfully"));
    }
    
    /**
     * Get products by category
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category", description = "Retrieves products by category with pagination")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        Page<ProductResponse> response = productService.getProductsByCategory(categoryId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(response, "Products retrieved successfully"));
    }
    
    /**
     * Get featured products
     */
    @GetMapping("/featured")
    @Operation(summary = "Get featured products", description = "Retrieves featured products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getFeaturedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<ProductResponse> response = productService.getFeaturedProducts(page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "Featured products retrieved successfully"));
    }
    
    /**
     * Search products
     */
    @PostMapping("/search")
    @Operation(summary = "Search products", description = "Search products with multiple criteria")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @Valid @RequestBody ProductSearchCriteria criteria) {
        
        Page<ProductResponse> response = productService.searchProducts(criteria);
        return ResponseEntity.ok(ApiResponse.success(response, "Search completed successfully"));
    }
    
    /**
     * Get products in stock
     */
    @GetMapping("/in-stock")
    @Operation(summary = "Get in-stock products", description = "Retrieves products that are in stock")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getInStockProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<ProductResponse> response = productService.getInStockProducts(page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "In-stock products retrieved successfully"));
    }
    
    /**
     * Get products on sale
     */
    @GetMapping("/on-sale")
    @Operation(summary = "Get products on sale", description = "Retrieves products that are on sale")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getOnSaleProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<ProductResponse> response = productService.getOnSaleProducts(page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "On-sale products retrieved successfully"));
    }
    
    /**
     * Get related products
     */
    @GetMapping("/{productId}/related")
    @Operation(summary = "Get related products", description = "Retrieves products related to the given product")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getRelatedProducts(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<ProductResponse> response = productService.getRelatedProducts(productId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "Related products retrieved successfully"));
    }
    
    /**
     * Update product
     */
    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product", description = "Updates an existing product (Admin only)")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest request) {
        
        log.info("Updating product ID: {}", productId);
        ProductResponse response = productService.updateProduct(productId, request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Product updated successfully"));
    }
    
    /**
     * Delete product
     */
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete product", description = "Deletes a product (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long productId) {
        
        log.info("Deleting product ID: {}", productId);
        productService.deleteProduct(productId);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }
    
    /**
     * Update product stock
     */
    @PatchMapping("/{productId}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product stock", description = "Updates product stock quantity (Admin only)")
    public ResponseEntity<ApiResponse<ProductResponse>> updateStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        
        log.info("Updating stock for product ID: {} to quantity: {}", productId, quantity);
        ProductResponse response = productService.updateStock(productId, quantity);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Stock updated successfully"));
    }
    
    /**
     * Get low stock products
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get low stock products", description = "Retrieves products with low stock (Admin only)")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold) {
        
        List<ProductResponse> response = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(ApiResponse.success(response, "Low stock products retrieved successfully"));
    }
    
    /**
     * Get distinct brands
     */
    @GetMapping("/brands")
    @Operation(summary = "Get distinct brands", description = "Retrieves all distinct product brands")
    public ResponseEntity<ApiResponse<List<String>>> getDistinctBrands() {
        
        List<String> response = productService.getDistinctBrands();
        return ResponseEntity.ok(ApiResponse.success(response, "Brands retrieved successfully"));
    }
    
    /**
     * Check if SKU exists
     */
    @GetMapping("/sku/{sku}/exists")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Check if SKU exists", description = "Checks if a product SKU already exists (Admin only)")
    public ResponseEntity<ApiResponse<Boolean>> existsBySku(@PathVariable String sku) {
        
        boolean exists = productService.existsBySku(sku);
        return ResponseEntity.ok(ApiResponse.success(exists, "SKU check completed"));
    }
    
    // ===============================
    // CATEGORY ENDPOINTS
    // ===============================
    
    /**
     * Create a new category
     */
    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new category", description = "Creates a new product category (Admin only)")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        
        log.info("Creating category: {}", request.getName());
        CategoryResponse response = categoryService.createCategory(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Category created successfully"));
    }
    
    /**
     * Get category by ID
     */
    @GetMapping("/categories/{categoryId}")
    @Operation(summary = "Get category by ID", description = "Retrieves a category by its ID")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @PathVariable Long categoryId) {
        
        CategoryResponse response = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(ApiResponse.success(response, "Category retrieved successfully"));
    }
    
    /**
     * Get all active categories
     */
    @GetMapping("/categories")
    @Operation(summary = "Get all active categories", description = "Retrieves all active categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllActiveCategories() {
        
        List<CategoryResponse> response = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(ApiResponse.success(response, "Categories retrieved successfully"));
    }
    
    /**
     * Get category hierarchy
     */
    @GetMapping("/categories/hierarchy")
    @Operation(summary = "Get category hierarchy", description = "Retrieves the complete category hierarchy")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoryHierarchy() {
        
        List<CategoryResponse> response = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(ApiResponse.success(response, "Category hierarchy retrieved successfully"));
    }
    
    /**
     * Get subcategories
     */
    @GetMapping("/categories/{categoryId}/subcategories")
    @Operation(summary = "Get subcategories", description = "Retrieves subcategories of a parent category")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getSubcategories(
            @PathVariable Long categoryId) {
        
        List<CategoryResponse> response = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(ApiResponse.success(response, "Subcategories retrieved successfully"));
    }
    
    /**
     * Update category
     */
    @PutMapping("/categories/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update category", description = "Updates an existing category (Admin only)")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequest request) {
        
        log.info("Updating category ID: {}", categoryId);
        CategoryResponse response = categoryService.updateCategory(categoryId, request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Category updated successfully"));
    }
    
    /**
     * Delete category
     */
    @DeleteMapping("/categories/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete category", description = "Deletes a category (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long categoryId) {
        
        log.info("Deleting category ID: {}", categoryId);
        categoryService.deleteCategory(categoryId);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Category deleted successfully"));
    }
}