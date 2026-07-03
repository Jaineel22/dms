package com.dms.service.impl;

import com.dms.dto.request.DocumentCategoryRequest;
import com.dms.dto.response.DocumentCategoryResponse;
import com.dms.entity.DocumentCategory;
import com.dms.exception.BusinessException;
import com.dms.exception.DuplicateResourceException;
import com.dms.exception.ResourceNotFoundException;
import com.dms.mapper.DocumentCategoryMapper;
import com.dms.repository.DocumentCategoryRepository;
import com.dms.repository.DocumentRepository;
import com.dms.service.DocumentCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentCategoryServiceImpl implements DocumentCategoryService {

    private final DocumentCategoryRepository categoryRepository;
    private final DocumentRepository         documentRepository;
    private final DocumentCategoryMapper     categoryMapper;

    // ─── Create ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public DocumentCategoryResponse createCategory(DocumentCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("DocumentCategory", "name", request.getName());
        }

        DocumentCategory category = categoryMapper.toEntity(request);
        DocumentCategory saved    = categoryRepository.save(category);

        log.info("Document category '{}' created", saved.getName());
        return categoryMapper.toResponse(saved, 0L);
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public DocumentCategoryResponse updateCategory(Long categoryId, DocumentCategoryRequest request) {
        DocumentCategory category = resolveCategory(categoryId);

        if (categoryRepository.existsByNameAndIdNot(request.getName(), categoryId)) {
            throw new DuplicateResourceException("DocumentCategory", "name", request.getName());
        }

        categoryMapper.updateEntityFromRequest(request, category);

        // isActive update handled explicitly (MapStruct IGNORE for patch — but this field is part of request)
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        DocumentCategory updated = categoryRepository.save(category);
        Long docCount = documentRepository.countByCategoryIdAndIsArchivedFalse(categoryId);

        log.info("Document category [{}] updated", categoryId);
        return categoryMapper.toResponse(updated, docCount);
    }

    // ─── Delete (soft) ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        DocumentCategory category = resolveCategory(categoryId);

        long docCount = documentRepository.countByCategoryIdAndIsArchivedFalse(categoryId);
        if (docCount > 0) {
            throw new BusinessException(
                "Cannot delete category '" + category.getName() + "' because it has "
                + docCount + " active document(s) assigned. Reassign or archive them first.");
        }

        category.setIsActive(false);
        categoryRepository.save(category);

        log.info("Document category [{}] soft-deleted", categoryId);
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public DocumentCategoryResponse getCategoryById(Long categoryId) {
        DocumentCategory category = resolveCategory(categoryId);
        Long docCount = documentRepository.countByCategoryIdAndIsArchivedFalse(categoryId);
        return categoryMapper.toResponse(category, docCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentCategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(c -> categoryMapper.toResponse(
                        c, documentRepository.countByCategoryIdAndIsArchivedFalse(c.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentCategoryResponse> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(c -> categoryMapper.toResponse(
                        c, documentRepository.countByCategoryIdAndIsArchivedFalse(c.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentCategoryResponse> getActiveCategories() {
        return categoryRepository.findAllByIsActiveTrue().stream()
                .map(c -> categoryMapper.toResponse(
                        c, documentRepository.countByCategoryIdAndIsArchivedFalse(c.getId())))
                .collect(Collectors.toList());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private DocumentCategory resolveCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentCategory", "id", categoryId));
    }
}