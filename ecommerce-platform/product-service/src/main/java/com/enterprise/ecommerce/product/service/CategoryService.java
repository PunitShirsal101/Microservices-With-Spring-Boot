package com.enterprise.ecommerce.product.service;

import com.enterprise.ecommerce.common.exception.InvalidRequestException;
import com.enterprise.ecommerce.common.exception.ResourceNotFoundException;
import com.enterprise.ecommerce.product.dto.CategoryRequest;
import com.enterprise.ecommerce.product.dto.CategoryResponse;
import com.enterprise.ecommerce.product.entity.Category;
import com.enterprise.ecommerce.product.repository.CategoryRepository;
import com.enterprise.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for category management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private static final String CATEGORY_NOT_FOUND_MESSAGE = "Category not found with ID: ";
    
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;    
    
    /**
     * Create a new category
     * @param request category request
     * @return category response
     */
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Creating category with name: {}", request.getName());
        
        // Check if category name already exists
        if (Boolean.TRUE.equals(categoryRepository.existsByName(request.getName()))) {
            throw new InvalidRequestException("Category with name '" + request.getName() + "' already exists");
        }
        
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .active(request.getActive())
                .sortOrder(request.getSortOrder())
                .build();
        
        // Set parent if specified
        if (request.getParentId() != null) {
            Long parentId = request.getParentId();
            if (parentId == null) {
                throw new InvalidRequestException("Parent category ID cannot be null");
            }
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            if (parent != null) {
                category.setParent(parent);
            }
        }
        
        if (category == null) {
            throw new ResourceNotFoundException("Category not found");
        }

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());
        
        return mapToCategoryResponse(savedCategory);
    }
    
    /**
     * Get category by ID
     * @param categoryId category ID
     * @return category response
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long categoryId) {
        if (categoryId == null) {
            throw new InvalidRequestException("Category ID cannot be null");
        }
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND_MESSAGE + categoryId));
        
        return mapToCategoryResponse(category);
    }
    
    /**
     * Get all active categories
     * @return list of category responses
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllActiveCategories() {
        List<Category> categories = categoryRepository.findByActiveTrueOrderBySortOrder();
        return categories.stream()
                .map(this::mapToCategoryResponse)
                .toList();
    }
    
    /**
     * Get root categories (no parent)
     * @return list of root category responses
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        List<Category> categories = categoryRepository.findRootCategories();
        return categories.stream()
                .map(this::mapToCategoryResponse)
                .toList();
    }
    
    /**
     * Get child categories of a parent
     * @param parentId parent category ID
     * @return list of child category responses
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getChildCategories(Long parentId) {
        List<Category> categories = categoryRepository.findByParentId(parentId);
        return categories.stream()
                .map(this::mapToCategoryResponse)
                .toList();
    }
    
    /**
     * Update category
     * @param categoryId category ID
     * @param request category request
     * @return updated category response
     */
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request) {
        log.info("Updating category with ID: {}", categoryId);
        
        if (categoryId == null) {
            throw new InvalidRequestException("Category ID cannot be null");
        }
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND_MESSAGE + categoryId));
        
        // Check if name is being changed and if it conflicts with existing
        if (!category.getName().equals(request.getName()) && 
            Boolean.TRUE.equals(categoryRepository.existsByName(request.getName()))) {
            throw new InvalidRequestException("Category with name '" + request.getName() + "' already exists");
        }
        
        // Update fields
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        category.setActive(request.getActive());
        category.setSortOrder(request.getSortOrder());
        
        // Update parent if specified
        if (request.getParentId() != null && !request.getParentId().equals(categoryId)) {
            Long parentId = request.getParentId();
                        
            if (parentId == null) {
                throw new InvalidRequestException("Parent category ID cannot be null");
            }
            
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            if (parent != null) {
                category.setParent(parent);
            }
        } else if (request.getParentId() == null) {
            category.setParent(null);
        }
        
        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully: {}", updatedCategory.getName());
        
        return mapToCategoryResponse(updatedCategory);
    }
    
    /**
     * Delete category
     * @param categoryId category ID
     */
    public void deleteCategory(Long categoryId) {
        log.info("Deleting category with ID: {}", categoryId);
        
        if (categoryId == null) {
            throw new InvalidRequestException("Category ID cannot be null");
        }
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND_MESSAGE + categoryId));
        
        // Check if category has products
        Long productCount = productRepository.countByCategoryId(categoryId);
        if (productCount > 0) {
            throw new InvalidRequestException("Cannot delete category with existing products. Product count: " + productCount);
        }
        
        // Check if category has child categories
        List<Category> children = categoryRepository.findByParentId(categoryId);
        if (!children.isEmpty()) {
            throw new InvalidRequestException("Cannot delete category with child categories. Child count: " + children.size());
        }
        
        if (category != null) {
            categoryRepository.delete(category);
            log.info("Category deleted successfully: {}", category.getName());
        }
    }
    
    /**
     * Search categories by name
     * @param name search term
     * @return list of matching category responses
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> searchCategories(String name) {
        List<Category> categories = categoryRepository.findByNameContainingIgnoreCase(name);
        return categories.stream()
                .map(this::mapToCategoryResponse)
                .toList();
    }
    
    /**
     * Check if category name exists
     * @param name category name
     * @return true if name exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
    
    /**
     * Map Category entity to CategoryResponse DTO
     * @param category category entity
     * @return category response DTO
     */
    private CategoryResponse mapToCategoryResponse(Category category) {
        CategoryResponse.CategoryResponseBuilder builder = CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .active(category.getActive())
                .sortOrder(category.getSortOrder())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt());
        
        if (category.getParent() != null) {
            builder.parentId(category.getParent().getId())
                   .parentName(category.getParent().getName());
        }
        
        // Get product count for this category
        Long productCount = productRepository.countByCategoryId(category.getId());
        builder.productCount(productCount);
        
        return builder.build();
    }
}