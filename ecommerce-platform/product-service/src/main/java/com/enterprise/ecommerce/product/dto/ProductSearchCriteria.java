package com.enterprise.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for product search criteria
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchCriteria {
    
    private String searchTerm;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String brand;
    private Boolean featured;
    private Boolean inStock;
    private Boolean onSale;
    @Builder.Default
    private String sortBy = "name"; // name, price, createdAt
    @Builder.Default
    private String sortDirection = "asc"; // asc, desc
    @Builder.Default
    private Integer page = 0;
    @Builder.Default
    private Integer size = 20;
}