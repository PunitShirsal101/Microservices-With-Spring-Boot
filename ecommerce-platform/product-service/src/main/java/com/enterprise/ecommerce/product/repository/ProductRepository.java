package com.enterprise.ecommerce.product.repository;

import com.enterprise.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Find product by SKU
     * @param sku product SKU
     * @return Optional containing the product if found
     */
    Optional<Product> findBySku(String sku);
    
    /**
     * Find all active products
     * @param pageable pagination information
     * @return page of active products
     */
    Page<Product> findByActiveTrue(Pageable pageable);
    
    /**
     * Find products by category
     * @param categoryId category ID
     * @param pageable pagination information
     * @return page of products in the category
     */
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.active = true")
    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
    
    /**
     * Find featured products
     * @param pageable pagination information
     * @return page of featured products
     */
    Page<Product> findByFeaturedTrueAndActiveTrue(Pageable pageable);
    
    /**
     * Find products by name containing text (case insensitive)
     * @param name text to search for
     * @param pageable pagination information
     * @return page of matching products
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.active = true")
    Page<Product> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    /**
     * Find products by price range
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @param pageable pagination information
     * @return page of products in the price range
     */
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.active = true")
    Page<Product> findByPriceBetween(@Param("minPrice") BigDecimal minPrice, 
                                   @Param("maxPrice") BigDecimal maxPrice, 
                                   Pageable pageable);
    
    /**
     * Find products by brand
     * @param brand product brand
     * @param pageable pagination information
     * @return page of products from the brand
     */
    Page<Product> findByBrandAndActiveTrue(String brand, Pageable pageable);
    
    /**
     * Find products that are in stock
     * @param pageable pagination information
     * @return page of products that are in stock
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > p.reservedQuantity AND p.active = true")
    Page<Product> findInStockProducts(Pageable pageable);
    
    /**
     * Find products on sale (with discount price)
     * @param pageable pagination information
     * @return page of products on sale
     */
    @Query("SELECT p FROM Product p WHERE p.discountPrice IS NOT NULL AND p.discountPrice < p.price AND p.active = true")
    Page<Product> findOnSaleProducts(Pageable pageable);
    
    /**
     * Search products by multiple criteria
     * @param searchTerm search term for name, description, or tags
     * @param categoryId category ID (optional)
     * @param minPrice minimum price (optional)
     * @param maxPrice maximum price (optional)
     * @param brand brand (optional)
     * @param pageable pagination information
     * @return page of matching products
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:searchTerm IS NULL OR " +
           " LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:brand IS NULL OR LOWER(p.brand) = LOWER(:brand)) AND " +
           "p.active = true")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm,
                                @Param("categoryId") Long categoryId,
                                @Param("minPrice") BigDecimal minPrice,
                                @Param("maxPrice") BigDecimal maxPrice,
                                @Param("brand") String brand,
                                Pageable pageable);
    
    /**
     * Find related products by category (excluding the given product)
     * @param categoryId category ID
     * @param productId product ID to exclude
     * @param pageable pagination information
     * @return page of related products
     */
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.id != :productId AND p.active = true")
    Page<Product> findRelatedProducts(@Param("categoryId") Long categoryId, 
                                    @Param("productId") Long productId, 
                                    Pageable pageable);
    
    /**
     * Find low stock products (stock quantity below threshold)
     * @param threshold stock threshold
     * @return list of low stock products
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold AND p.active = true")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);
    
    /**
     * Check if SKU exists
     * @param sku product SKU
     * @return true if SKU exists, false otherwise
     */
    Boolean existsBySku(String sku);
    
    /**
     * Count products by category
     * @param categoryId category ID
     * @return count of products in the category
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.active = true")
    Long countByCategoryId(@Param("categoryId") Long categoryId);
    
    /**
     * Find distinct brands
     * @return list of distinct brands
     */
    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.brand IS NOT NULL AND p.active = true ORDER BY p.brand")
    List<String> findDistinctBrands();
}