package com.enterprise.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for category response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Boolean active;
    private Integer sortOrder;
    private Long parentId;
    private String parentName;
    private List<CategoryResponse> children;
    private Long productCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}