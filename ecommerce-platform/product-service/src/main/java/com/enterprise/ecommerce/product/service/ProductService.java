package com.enterprise.ecommerce.product.service;

import com.enterprise.ecommerce.common.exception.InvalidRequestException;
import com.enterprise.ecommerce.common.exception.ResourceNotFoundException;
import com.enterprise.ecommerce.common.kafka.KafkaProducerService;
import com.enterprise.ecommerce.common.events.ProductUpdatedEvent;
import com.enterprise.ecommerce.product.dto.ProductRequest;
import com.enterprise.ecommerce.product.dto.ProductResponse;
import com.enterprise.ecommerce.product.dto.ProductSearchCriteria;
import com.enterprise.ecommerce.product.entity.Category;
import com.enterprise.ecommerce.product.entity.Product;
import com.enterprise.ecommerce.product.repository.CategoryRepository;
import com.enterprise.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * Service class for product management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {
    
    private static final String PRODUCT_NOT_FOUND_MESSAGE = "Product not found with ID: ";
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final KafkaProducerService kafkaProducerService;
    
    /**
     * Create a new product
     * @param request product request
     * @return product response
     */
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating product with name: {}", request.getName());
        
        // Check if SKU already exists
        if (request.getSku() != null && Boolean.TRUE.equals(productRepository.existsBySku(request.getSku()))) {
            throw new InvalidRequestException("Product with SKU '" + request.getSku() + "' already exists");
        }
        
        // Get category
        Long categoryId = request.getCategoryId();
        if (categoryId == null) {
            throw new InvalidRequestException("Category ID is required");
        }
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));
        
        Product product = Product.builder()
                .name(request.getName())
                .sku(request.getSku())
                .description(request.getDescription())
                .detailedDescription(request.getDetailedDescription())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .stockQuantity(request.getStockQuantity())
                .active(request.getActive())
                .featured(request.getFeatured())
                .imageUrl(request.getImageUrl())
                .additionalImages(request.getAdditionalImages() != null ? request.getAdditionalImages().toArray(new String[0]) : null)
                .tags(request.getTags() != null ? request.getTags().toArray(new String[0]) : null)
                .weight(request.getWeight())
                .length(request.getLength())
                .width(request.getWidth())
                .height(request.getHeight())
                .brand(request.getBrand())
                .manufacturer(request.getManufacturer())
                .model(request.getModel())
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .metaKeywords(request.getMetaKeywords())
                .category(category)
                .build();
        
        if (product == null) {
            throw new ResourceNotFoundException("Product not found");
        }

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());
        
        return mapToProductResponse(savedProduct);
    }
    
    /**
     * Get product by ID
     * @param productId product ID
     * @return product response
     */
    @Cacheable(value = "products", key = "#productId")
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long productId) {
        if (productId == null) {
            throw new InvalidRequestException("Product ID cannot be null");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND_MESSAGE + productId));
        
        return mapToProductResponse(product);
    }
    
    /**
     * Get product by SKU
     * @param sku product SKU
     * @return product response
     */
    @Cacheable(value = "products", key = "#sku")
    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Product not found with SKU: " + sku));
        
        return mapToProductResponse(product);
    }
    
    /**
     * Get all active products with pagination
     * @param page page number
     * @param size page size
     * @param sortBy sort field
     * @param sortDirection sort direction
     * @return page of product responses
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllActiveProducts(int page, int size, String sortBy, String sortDirection) {

        if (sortDirection == null || sortDirection.isBlank() || sortDirection.isEmpty()) {
            sortDirection = "ASC";
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> products = productRepository.findByActiveTrue(pageable);
        return products.map(this::mapToProductResponse);
    }
    
    /**
     * Get products by category
     * @param categoryId category ID
     * @param page page number
     * @param size page size
     * @param sortBy sort field
     * @param sortDirection sort direction
     * @return page of product responses
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(Long categoryId, int page, int size, String sortBy, String sortDirection) {
        
        if (sortDirection == null || sortDirection.isBlank() || sortDirection.isEmpty()) {
            sortDirection = "ASC";
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
        return products.map(this::mapToProductResponse);
    }
    
    /**
     * Get featured products
     * @param page page number
     * @param size page size
     * @return page of featured product responses
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getFeaturedProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByFeaturedTrueAndActiveTrue(pageable);
        return products.map(this::mapToProductResponse);
    }
    
    /**
     * Search products with multiple criteria
     * @param criteria search criteria
     * @return page of product responses
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(ProductSearchCriteria criteria) {

        if (criteria == null) {
            throw new InvalidRequestException("Search criteria cannot be null");
        }

        String sortDirection = criteria.getSortDirection();
        if (sortDirection == null || sortDirection.isBlank() || sortDirection.isEmpty()) {
            sortDirection = "ASC";
        }
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), criteria.getSortBy());
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);
        
        Page<Product> products = productRepository.searchProducts(
                criteria.getSearchTerm(),
                criteria.getCategoryId(),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                criteria.getBrand(),
                pageable
        );
        
        return products.map(this::mapToProductResponse);
    }
    
    /**
     * Get products in stock
     * @param page page number
     * @param size page size
     * @return page of in-stock product responses
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getInStockProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findInStockProducts(pageable);
        return products.map(this::mapToProductResponse);
    }
    
    /**
     * Get products on sale
     * @param page page number
     * @param size page size
     * @return page of on-sale product responses
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getOnSaleProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findOnSaleProducts(pageable);
        return products.map(this::mapToProductResponse);
    }
    
    /**
     * Get related products by category
     * @param productId product ID to exclude
     * @param page page number
     * @param size page size
     * @return page of related product responses
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getRelatedProducts(Long productId, int page, int size) {

        if (productId == null) {
            throw new InvalidRequestException("Product ID cannot be null");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND_MESSAGE + productId));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findRelatedProducts(product.getCategory().getId(), productId, pageable);
        return products.map(this::mapToProductResponse);
    }
    
    /**
     * Update product
     * @param productId product ID
     * @param request product request
     * @return updated product response
     */
    @CacheEvict(value = {"products", "productBrands"}, allEntries = true)
    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        log.info("Updating product with ID: {}", productId);
        
        if (productId == null) {
            throw new InvalidRequestException("Product ID cannot be null");
        }


        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND_MESSAGE + productId));

        // Check if SKU is being changed and if it conflicts with existing
        if (request.getSku() != null && !request.getSku().equals(product.getSku()) && Boolean.TRUE.equals(productRepository.existsBySku(request.getSku()))) {
            throw new InvalidRequestException("Product with SKU '" + request.getSku() + "' already exists");
        }
        
        // Get category if changed
        if (request.getCategoryId() != null && !request.getCategoryId().equals(product.getCategory().getId())) {
            Long categoryId = request.getCategoryId();
            if (categoryId == null) {
                throw new InvalidRequestException("Category ID cannot be null");
            }

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));
            product.setCategory(category);
        }
        
        // Update product fields
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setDetailedDescription(request.getDetailedDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setActive(request.getActive());
        product.setFeatured(request.getFeatured());
        product.setImageUrl(request.getImageUrl());
        product.setAdditionalImages(request.getAdditionalImages() != null ? request.getAdditionalImages().toArray(new String[0]) : null);
        product.setTags(request.getTags() != null ? request.getTags().toArray(new String[0]) : null);
        product.setWeight(request.getWeight());
        product.setLength(request.getLength());
        product.setWidth(request.getWidth());
        product.setHeight(request.getHeight());
        product.setBrand(request.getBrand());
        product.setManufacturer(request.getManufacturer());
        product.setModel(request.getModel());
        product.setMetaTitle(request.getMetaTitle());
        product.setMetaDescription(request.getMetaDescription());
        product.setMetaKeywords(request.getMetaKeywords());
        
        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully: {}", updatedProduct.getName());
        
        // Publish product updated event
        ProductUpdatedEvent event = ProductUpdatedEvent.builder()
                .productId(updatedProduct.getId().toString())
                .name(updatedProduct.getName())
                .description(updatedProduct.getDescription())
                .price(updatedProduct.getPrice())
                .stockQuantity(updatedProduct.getStockQuantity())
                .category(updatedProduct.getCategory().getName())
                .available(updatedProduct.getActive())
                .updatedAt(updatedProduct.getUpdatedAt())
                .build();
        
        kafkaProducerService.sendMessage("product-events", updatedProduct.getId().toString(), event);
        log.info("Published ProductUpdatedEvent for product: {}", updatedProduct.getId());
        
        return mapToProductResponse(updatedProduct);
    }
    
    /**
     * Delete product
     * @param productId product ID
     */
    @CacheEvict(value = {"products", "productBrands"}, allEntries = true)
    public void deleteProduct(Long productId) {
        log.info("Deleting product with ID: {}", productId);

            if (productId == null) {
                throw new InvalidRequestException("Product ID cannot be null");
            }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND_MESSAGE + productId));
        
        if (product == null) {
            throw new ResourceNotFoundException(PRODUCT_NOT_FOUND_MESSAGE + productId);
        }

        productRepository.delete(product);
        log.info("Product deleted successfully: {}", product.getName());
    }
    
    /**
     * Update product stock
     * @param productId product ID
     * @param quantity new stock quantity
     * @return updated product response
     */
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse updateStock(Long productId, Integer quantity) {
        log.info("Updating stock for product ID: {} to quantity: {}", productId, quantity);
        
        if (productId == null) {
            throw new InvalidRequestException("Product ID cannot be null");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND_MESSAGE + productId));

        product.setStockQuantity(quantity);
        Product updatedProduct = productRepository.save(product);
        
        log.info("Stock updated successfully for product: {}", updatedProduct.getName());
        return mapToProductResponse(updatedProduct);
    }
    
    /**
     * Get low stock products
     * @param threshold stock threshold
     * @return list of low stock product responses
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts(Integer threshold) {
        List<Product> products = productRepository.findLowStockProducts(threshold);
        return products.stream()
                .map(this::mapToProductResponse)
                .toList();
    }
    
    /**
     * Get distinct brands
     * @return list of distinct brands
     */
    @Cacheable(value = "productBrands")
    @Transactional(readOnly = true)
    public List<String> getDistinctBrands() {
        return productRepository.findDistinctBrands();
    }
    
    /**
     * Check if SKU exists
     * @param sku product SKU
     * @return true if SKU exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsBySku(String sku) {
        return productRepository.existsBySku(sku);
    }
    
    /**
     * Map Product entity to ProductResponse DTO
     * @param product product entity
     * @return product response DTO
     */
    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .description(product.getDescription())
                .detailedDescription(product.getDetailedDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .effectivePrice(product.getEffectivePrice())
                .stockQuantity(product.getStockQuantity())
                .reservedQuantity(product.getReservedQuantity())
                .availableQuantity(product.getAvailableQuantity())
                .active(product.getActive())
                .featured(product.getFeatured())
                .inStock(product.isInStock())
                .onSale(product.isOnSale())
                .imageUrl(product.getImageUrl())
                .additionalImages(product.getAdditionalImages() != null ? Arrays.asList(product.getAdditionalImages()) : null)
                .tags(product.getTags() != null ? Arrays.asList(product.getTags()) : null)
                .weight(product.getWeight())
                .length(product.getLength())
                .width(product.getWidth())
                .height(product.getHeight())
                .brand(product.getBrand())
                .manufacturer(product.getManufacturer())
                .model(product.getModel())
                .metaTitle(product.getMetaTitle())
                .metaDescription(product.getMetaDescription())
                .metaKeywords(product.getMetaKeywords())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}