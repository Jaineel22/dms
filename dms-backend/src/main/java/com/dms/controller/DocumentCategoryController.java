package com.dms.controller;

import com.dms.constant.ApiConstants;
import com.dms.constant.RoleConstants;
import com.dms.dto.request.DocumentCategoryRequest;
import com.dms.dto.response.ApiResponse;
import com.dms.dto.response.DocumentCategoryResponse;
import com.dms.service.DocumentCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(ApiConstants.CATEGORIES_BASE)
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Document Categories", description = "Manage document categories used to classify uploaded documents")
public class DocumentCategoryController {

    private final DocumentCategoryService categoryService;

    // ─── POST /categories ─────────────────────────────────────────────────────

    @Operation(summary = "Create a new document category (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Category created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Category name already exists"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<DocumentCategoryResponse>> createCategory(
            @Valid @RequestBody DocumentCategoryRequest request) {

        DocumentCategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", response));
    }

    // ─── PUT /categories/{categoryId} ─────────────────────────────────────────

    @Operation(summary = "Update a document category (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Category name already in use")
    })
    @PutMapping("/{categoryId}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<DocumentCategoryResponse>> updateCategory(
            @Parameter(description = "ID of the category to update") @PathVariable Long categoryId,
            @Valid @RequestBody DocumentCategoryRequest request) {

        DocumentCategoryResponse response = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", response));
    }

    // ─── DELETE /categories/{categoryId} ──────────────────────────────────────

    @Operation(
        summary     = "Delete a document category (ADMIN only)",
        description = "Soft-deletes the category. Fails with 422 if the category still has documents assigned to it.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Category has active documents and cannot be deleted")
    })
    @DeleteMapping("/{categoryId}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID of the category to delete") @PathVariable Long categoryId) {

        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    // ─── GET /categories/{categoryId} ─────────────────────────────────────────

    @Operation(summary = "Get a document category by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{categoryId}")
    @PreAuthorize(RoleConstants.IS_AUTHENTICATED)
    public ResponseEntity<ApiResponse<DocumentCategoryResponse>> getCategoryById(
            @Parameter(description = "ID of the category") @PathVariable Long categoryId) {

        DocumentCategoryResponse response = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── GET /categories ──────────────────────────────────────────────────────

    @Operation(
        summary     = "Get all document categories (active + inactive)",
        description = "Returns all categories. ADMIN users see both active and inactive. "
                    + "For upload dropdowns, use GET /categories/active instead.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category list returned")
    })
    @GetMapping
    @PreAuthorize(RoleConstants.IS_AUTHENTICATED)
    public ResponseEntity<ApiResponse<List<DocumentCategoryResponse>>> getAllCategories() {

        List<DocumentCategoryResponse> response = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── GET /categories/active ───────────────────────────────────────────────

    @Operation(
        summary     = "Get all active document categories",
        description = "Returns only active categories. Use this endpoint to populate "
                    + "the category dropdown on the document upload form.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Active category list returned")
    })
    @GetMapping("/active")
    @PreAuthorize(RoleConstants.IS_AUTHENTICATED)
    public ResponseEntity<ApiResponse<List<DocumentCategoryResponse>>> getActiveCategories() {

        List<DocumentCategoryResponse> response = categoryService.getActiveCategories();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── GET /categories/paginated ────────────────────────────────────────────

    @Operation(summary = "Get all document categories — paginated (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paginated category list returned")
    })
    @GetMapping("/paginated")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<Page<DocumentCategoryResponse>>> getAllCategoriesPaginated(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0")       int page,
            @Parameter(description = "Page size")             @RequestParam(defaultValue = "20")      int size,
            @Parameter(description = "Sort field,direction")  @RequestParam(defaultValue = "name,asc") String sort) {

        Page<DocumentCategoryResponse> response =
                categoryService.getAllCategories(buildPageable(page, size, sort));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Pageable buildPageable(int page, int size, String sort) {
        int safeSize = Math.min(size, 100);
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, safeSize, Sort.by(dir, field));
    }
}