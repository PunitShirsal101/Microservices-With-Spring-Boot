package com.enterprise.ecommerce.product.repository;

import com.enterprise.ecommerce.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Category entity
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Find category by name
     * @param name category name
     * @return Optional containing the category if found
     */
    Optional<Category> findByName(String name);
    
    /**
     * Find all active categories
     * @return list of active categories
     */
    List<Category> findByActiveTrue();
    
    /**
     * Find all root categories (no parent)
     * @return list of root categories
     */
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.active = true ORDER BY c.sortOrder")
    List<Category> findRootCategories();
    
    /**
     * Find children of a parent category
     * @param parentId parent category ID
     * @return list of child categories
     */
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.active = true ORDER BY c.sortOrder")
    List<Category> findByParentId(@Param("parentId") Long parentId);
    
    /**
     * Find categories by name containing text (case insensitive)
     * @param name text to search for
     * @return list of matching categories
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) AND c.active = true")
    List<Category> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Check if category name exists
     * @param name category name
     * @return true if name exists, false otherwise
     */
    Boolean existsByName(String name);
    
    /**
     * Find categories ordered by sort order
     * @return list of categories ordered by sort order
     */
    List<Category> findByActiveTrueOrderBySortOrder();
}