package com.enterprise.ecommerce.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product entity representing products in the e-commerce platform
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_products_name", columnList = "name"),
    @Index(name = "idx_products_category", columnList = "category_id"),
    @Index(name = "idx_products_active", columnList = "active"),
    @Index(name = "idx_products_price", columnList = "price")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 200)
    @Column(nullable = false)
    private String name;
    
    @Size(max = 100)
    @Column(unique = true)
    private String sku;
    
    @Size(max = 1000)
    private String description;
    
    @Size(max = 2000)
    private String detailedDescription;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @DecimalMin(value = "0.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal discountPrice;
    
    @Builder.Default
    @Column(nullable = false)
    private Integer stockQuantity = 0;
    
    @Builder.Default
    @Column(nullable = false)
    private Integer reservedQuantity = 0;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean featured = false;
    
    @Size(max = 500)
    private String imageUrl;
    
    @Column(name = "additional_images", columnDefinition = "text[]")
    private String[] additionalImages;
    
    @Column(name = "tags", columnDefinition = "text[]")
    private String[] tags;
    
    // Product dimensions
    @Column(precision = 8, scale = 2)
    private BigDecimal weight;
    
    @Column(precision = 8, scale = 2)
    private BigDecimal length;
    
    @Column(precision = 8, scale = 2)
    private BigDecimal width;
    
    @Column(precision = 8, scale = 2)
    private BigDecimal height;
    
    @Size(max = 50)
    private String brand;
    
    @Size(max = 50)
    private String manufacturer;
    
    @Size(max = 100)
    private String model;
    
    // SEO fields
    @Size(max = 200)
    private String metaTitle;
    
    @Size(max = 500)
    private String metaDescription;
    
    @Size(max = 200)
    private String metaKeywords;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    // Audit fields
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    /**
     * Calculate available quantity (stock - reserved)
     * @return available quantity
     */
    public Integer getAvailableQuantity() {
        return stockQuantity - reservedQuantity;
    }
    
    /**
     * Check if product is in stock
     * @return true if product has available stock
     */
    public boolean isInStock() {
        return getAvailableQuantity() > 0;
    }
    
    /**
     * Check if product is on sale (has discount price)
     * @return true if product has discount price
     */
    public boolean isOnSale() {
        return discountPrice != null && discountPrice.compareTo(price) < 0;
    }
    
    /**
     * Get effective price (discount price if available, otherwise regular price)
     * @return effective price
     */
    public BigDecimal getEffectivePrice() {
        return isOnSale() ? discountPrice : price;
    }
}