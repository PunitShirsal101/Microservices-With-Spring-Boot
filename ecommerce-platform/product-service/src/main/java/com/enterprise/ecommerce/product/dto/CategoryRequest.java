package com.enterprise.ecommerce.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for category requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {
    
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Size(max = 200, message = "Image URL must not exceed 200 characters")
    private String imageUrl;
    
    @Builder.Default
    private Boolean active = true;
    
    @Builder.Default
    private Integer sortOrder = 0;
    
    private Long parentId;
}