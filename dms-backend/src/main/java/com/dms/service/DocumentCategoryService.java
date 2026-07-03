package com.dms.service;

import com.dms.dto.request.DocumentCategoryRequest;
import com.dms.dto.response.DocumentCategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DocumentCategoryService {

    /** Creates a new category. Name must be unique. */
    DocumentCategoryResponse createCategory(DocumentCategoryRequest request);

    /** Updates an existing category's name, description, or active flag. */
    DocumentCategoryResponse updateCategory(Long categoryId, DocumentCategoryRequest request);

    /**
     * Soft-deletes a category (sets isActive = false).
     * Throws a {@link com.dms.exception.BusinessException} if the category
     * still has documents assigned to it.
     */
    void deleteCategory(Long categoryId);

    /** Returns a single category by ID. */
    DocumentCategoryResponse getCategoryById(Long categoryId);

    /** Returns all categories (active and inactive) for admin management. */
    List<DocumentCategoryResponse> getAllCategories();

    /** Returns all categories in paginated form. */
    Page<DocumentCategoryResponse> getAllCategories(Pageable pageable);

    /** Returns only active categories — used in document upload dropdowns. */
    List<DocumentCategoryResponse> getActiveCategories();
}   