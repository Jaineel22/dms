package com.dms.service.impl;

import com.dms.dto.request.DocumentSearchRequest;
import com.dms.dto.request.DocumentUpdateRequest;
import com.dms.dto.request.DocumentUploadRequest;
import com.dms.dto.request.DocumentVersionUploadRequest;
import com.dms.dto.response.DocumentResponse;
import com.dms.dto.response.DocumentSummaryResponse;
import com.dms.dto.response.DocumentUploadResponse;
import com.dms.dto.response.DocumentVersionResponse;
import com.dms.entity.Department;
import com.dms.entity.Document;
import com.dms.entity.DocumentCategory;
import com.dms.entity.DocumentVersion;
import com.dms.entity.User;
import com.dms.exception.AccessDeniedException;
import com.dms.exception.BusinessException;
import com.dms.exception.ResourceNotFoundException;
import com.dms.mapper.DocumentMapper;
import com.dms.mapper.DocumentVersionMapper;
import com.dms.repository.DepartmentRepository;
import com.dms.repository.DocumentCategoryRepository;
import com.dms.repository.DocumentRepository;
import com.dms.repository.DocumentVersionRepository;
import com.dms.service.DocumentService;
import com.dms.service.FileStorageService;
import com.dms.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    // ─── Status constants ─────────────────────────────────────────────────────
    public static final String STATUS_DRAFT                = "DRAFT";
    public static final String STATUS_UPLOADED             = "UPLOADED";
    public static final String STATUS_READY_FOR_SUBMISSION = "READY_FOR_SUBMISSION";
    public static final String STATUS_ARCHIVED             = "ARCHIVED";

    // ─── Dependencies ─────────────────────────────────────────────────────────
    private final DocumentRepository         documentRepository;
    private final DocumentVersionRepository  versionRepository;
    private final DocumentCategoryRepository categoryRepository;
    private final DepartmentRepository       departmentRepository;
    private final DocumentMapper             documentMapper;
    private final DocumentVersionMapper      versionMapper;
    private final FileStorageService         fileStorageService;
    private final SecurityUtils              securityUtils;

    // ─── Upload new document ──────────────────────────────────────────────────

    @Override
    @Transactional
    public DocumentUploadResponse uploadDocument(DocumentUploadRequest request) throws IOException {
        MultipartFile file = request.getFile();

        // ── File validation ──
        validateFile(file);

        // ── Category validation ──
        DocumentCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DocumentCategory", "id", request.getCategoryId()));
        if (!Boolean.TRUE.equals(category.getIsActive())) {
            throw new BusinessException(
                    "Category '" + category.getName() + "' is inactive and cannot be used.");
        }

        // ── Department validation (optional) ──
        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department", "id", request.getDepartmentId()));
        }

        // ── Store file ──
        String subDir  = buildSubDirectory();
        String relPath = fileStorageService.storeFile(file, subDir);
        String docNum  = fileStorageService.generateDocumentNumber();

        // ── Build and save Document ──
        User owner = securityUtils.getCurrentUser();

        Document document = documentMapper.toEntity(request);
        document.setDocumentNumber(docNum);
        document.setOwner(owner);
        document.setCategory(category);
        document.setDepartment(department);
        document.setFilePath(relPath);
        document.setFileName(file.getOriginalFilename());
        document.setFileSize(file.getSize());
        document.setMimeType(fileStorageService.getContentType(file.getOriginalFilename()));
        document.setCurrentVersion(1);
        document.setStatus(STATUS_UPLOADED);
        document.setIsActive(true);

        Document saved = documentRepository.save(document);

        // ── Create version 1 record ──
        DocumentVersion v1 = DocumentVersion.builder()
                .document(saved)
                .versionNumber(1)
                .filePath(relPath)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .uploadReason("Initial upload")
                .uploadedBy(owner)
                .createdAt(LocalDateTime.now())
                .build();
        versionRepository.save(v1);

        log.info("Document '{}' uploaded as [{}] by [{}]",
                document.getTitle(), docNum, owner.getEmail());

        return DocumentUploadResponse.builder()
                .documentId(saved.getId())
                .documentNumber(saved.getDocumentNumber())
                .fileName(saved.getFileName())
                .fileSize(saved.getFileSize())
                .versionNumber(1)
                .status(saved.getStatus())
                .message("Document uploaded successfully")
                .build();
    }

    // ─── Update metadata ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public DocumentResponse updateDocument(Long documentId, DocumentUpdateRequest request) {
        Document document = resolveDocument(documentId);
        requireNotArchived(document);

        // Resolve category if provided
        if (request.getCategoryId() != null) {
            DocumentCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "DocumentCategory", "id", request.getCategoryId()));
            if (!Boolean.TRUE.equals(category.getIsActive())) {
                throw new BusinessException(
                        "Category '" + category.getName() + "' is inactive.");
            }
            document.setCategory(category);
        }

        // Resolve department if provided
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department", "id", request.getDepartmentId()));
            document.setDepartment(dept);
        }

        documentMapper.updateEntityFromRequest(request, document);
        Document updated = documentRepository.save(document);

        log.info("Document [{}] updated by [{}]", documentId, securityUtils.getCurrentEmail());
        return documentMapper.toResponse(updated);
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(Long documentId) {
        return documentMapper.toResponse(resolveDocument(documentId));
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentByNumber(String documentNumber) {
        Document document = documentRepository.findByDocumentNumber(documentNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document", "documentNumber", documentNumber));
        return documentMapper.toResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentResponse> getDocumentsByOwner(Long ownerId, Pageable pageable) {
        return documentRepository.findByOwnerIdAndIsArchivedFalse(ownerId, pageable)
                .map(documentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentResponse> getAllDocuments(Pageable pageable) {
        return documentRepository.findAllByIsArchivedFalse(pageable)
                .map(documentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentResponse> searchDocuments(String search, Pageable pageable) {
        return documentRepository.searchDocuments(search, pageable)
                .map(documentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentResponse> advancedSearch(DocumentSearchRequest request, Pageable pageable) {
        // If a general search term is set, use full-text search instead of advanced
        if (request.getSearch() != null && !request.getSearch().isBlank()) {
            return documentRepository
                    .searchDocuments(request.getSearch(), pageable)
                    .map(documentMapper::toResponse);
        }

        // Convert LocalDate → LocalDateTime for repository query
        LocalDateTime fromDt = request.getFromDate() != null
                ? request.getFromDate().atStartOfDay() : null;
        LocalDateTime toDt   = request.getToDate()   != null
                ? request.getToDate().atTime(23, 59, 59) : null;

        Pageable sortedPageable = buildPageable(
                request.getPage(), request.getSize(),
                request.getSortBy(), request.getSortDirection());

        return documentRepository.advancedSearch(
                        request.getTitle(),
                        request.getDocumentNumber(),
                        request.getCategoryId(),
                        request.getDepartmentId(),
                        request.getStatus(),
                        request.getOwnerId(),
                        fromDt,
                        toDt,
                        sortedPageable)
                .map(documentMapper::toResponse);
    }

    // ─── Delete / Archive / Restore ───────────────────────────────────────────

    @Override
    @Transactional
    public void deleteDocument(Long documentId) {
        Document document = resolveDocument(documentId);
        document.setIsArchived(true);
        document.setArchivedAt(LocalDateTime.now());
        document.setIsActive(false);
        documentRepository.save(document);
        log.info("Document [{}] deleted (archived) by [{}]",
                documentId, securityUtils.getCurrentEmail());
    }

    @Override
    @Transactional
    public void archiveDocument(Long documentId) {
        Document document = resolveDocument(documentId);
        requireNotArchived(document);

        document.setIsArchived(true);
        document.setArchivedAt(LocalDateTime.now());
        document.setStatus(STATUS_ARCHIVED);
        documentRepository.save(document);

        log.info("Document [{}] archived by [{}]", documentId, securityUtils.getCurrentEmail());
    }

    @Override
    @Transactional
    public void restoreDocument(Long documentId) {
        Document document = resolveDocument(documentId);
        if (!Boolean.TRUE.equals(document.getIsArchived())) {
            throw new BusinessException("Document [" + documentId + "] is not archived.");
        }

        document.setIsArchived(false);
        document.setArchivedAt(null);
        document.setStatus(STATUS_UPLOADED);
        document.setIsActive(true);
        documentRepository.save(document);

        log.info("Document [{}] restored by [{}]", documentId, securityUtils.getCurrentEmail());
    }

    // ─── Upload new version ───────────────────────────────────────────────────

    @Override
    @Transactional
    public DocumentUploadResponse uploadNewVersion(
            Long documentId, DocumentVersionUploadRequest request) throws IOException {

        Document document = resolveDocument(documentId);
        requireNotArchived(document);

        MultipartFile file = request.getFile();
        validateFile(file);

        // Determine next version number
        int nextVersion = document.getCurrentVersion() + 1;

        // Store the new file
        String subDir  = buildSubDirectory();
        String relPath = fileStorageService.storeFile(file, subDir);

        User uploader = securityUtils.getCurrentUser();

        // Create version record
        DocumentVersion newVersion = DocumentVersion.builder()
                .document(document)
                .versionNumber(nextVersion)
                .filePath(relPath)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .uploadReason(request.getUploadReason())
                .uploadedBy(uploader)
                .createdAt(LocalDateTime.now())
                .build();
        versionRepository.save(newVersion);

        // Update document current version + file references
        document.setCurrentVersion(nextVersion);
        document.setFilePath(relPath);
        document.setFileName(file.getOriginalFilename());
        document.setFileSize(file.getSize());
        document.setMimeType(fileStorageService.getContentType(file.getOriginalFilename()));
        document.setStatus(STATUS_UPLOADED);
        documentRepository.save(document);

        log.info("New version v{} uploaded for document [{}] by [{}]",
                nextVersion, documentId, uploader.getEmail());

        return DocumentUploadResponse.builder()
                .documentId(document.getId())
                .documentNumber(document.getDocumentNumber())
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .versionNumber(nextVersion)
                .status(document.getStatus())
                .message("New version v" + nextVersion + " uploaded successfully")
                .build();
    }

    // ─── Version history ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<DocumentVersionResponse> getDocumentVersions(Long documentId) {
        resolveDocument(documentId); // validate existence
        return versionRepository.findByDocumentIdOrderByVersionNumberDesc(documentId)
                .stream()
                .map(versionMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Download ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadDocument(Long documentId) throws IOException {
        Document document = resolveDocument(documentId);
        checkDownloadAccess(document);

        if (!fileStorageService.fileExists(document.getFilePath())) {
            throw new BusinessException(
                    "File not found on disk for document [" + documentId + "]. "
                  + "Please contact an administrator.");
        }

        log.info("Document [{}] downloaded by [{}]", documentId, securityUtils.getCurrentEmail());
        return fileStorageService.loadFileAsBytes(document.getFilePath());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadDocumentVersion(Long documentId, Integer versionNumber) throws IOException {
        Document document = resolveDocument(documentId);
        checkDownloadAccess(document);

        DocumentVersion version = versionRepository
                .findByDocumentIdAndVersionNumber(documentId, versionNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DocumentVersion", "documentId/version",
                        documentId + "/" + versionNumber));

        if (!fileStorageService.fileExists(version.getFilePath())) {
            throw new BusinessException(
                    "File not found on disk for document [" + documentId
                  + "] version [" + versionNumber + "].");
        }

        log.info("Document [{}] v{} downloaded by [{}]",
                documentId, versionNumber, securityUtils.getCurrentEmail());
        return fileStorageService.loadFileAsBytes(version.getFilePath());
    }

    // ─── Summary ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public DocumentSummaryResponse getDocumentSummary() {
        long total    = documentRepository.count();
        long draft    = documentRepository.countByStatusAndIsArchivedFalse(STATUS_DRAFT);
        long uploaded = documentRepository.countByStatusAndIsArchivedFalse(STATUS_UPLOADED);
        long ready    = documentRepository.countByStatusAndIsArchivedFalse(STATUS_READY_FOR_SUBMISSION);
        long archived = documentRepository.countByStatusAndIsArchivedFalse(STATUS_ARCHIVED);

        long storageSize;
        try {
            storageSize = fileStorageService.getTotalStorageSize();
        } catch (IOException e) {
            log.warn("Could not compute total storage size: {}", e.getMessage());
            storageSize = 0L;
        }

        return DocumentSummaryResponse.builder()
                .totalDocuments(total)
                .draftDocuments(draft)
                .uploadedDocuments(uploaded)
                .readyForSubmissionDocuments(ready)
                .archivedDocuments(archived)
                .totalStorageSize(storageSize)
                .build();
    }

    // ─── Validation helpers ───────────────────────────────────────────────────

    /**
     * Validates a multipart file against size and type rules.
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File must not be empty.");
        }
        if (!fileStorageService.isValidFileSize(file.getSize())) {
            throw new BusinessException(
                    "File size " + formatBytes(file.getSize())
                  + " exceeds the maximum allowed size of 10 MB.");
        }
        String originalName = file.getOriginalFilename();
        if (!fileStorageService.isValidFileType(originalName)) {
            throw new BusinessException(
                    "File type '" + fileStorageService.getFileExtension(originalName)
                  + "' is not allowed. Allowed types: pdf, docx, doc, xlsx, xls, png, jpg, jpeg, txt.");
        }
    }

    /**
     * Guards against operations on archived documents.
     */
    private void requireNotArchived(Document document) {
        if (Boolean.TRUE.equals(document.getIsArchived())) {
            throw new BusinessException(
                    "Document '" + document.getDocumentNumber()
                  + "' is archived and cannot be modified. Restore it first.");
        }
    }

    /**
     * Allows download only to the document owner or an ADMIN.
     */
    private void checkDownloadAccess(Document document) {
        User current = securityUtils.getCurrentUser();
        boolean isOwner = document.getOwner() != null
                && document.getOwner().getId().equals(current.getId());

        if (!isOwner && !securityUtils.isAdmin()) {
            throw new AccessDeniedException(
                    "You do not have permission to download document '"
                  + document.getDocumentNumber() + "'.");
        }

        if (Boolean.TRUE.equals(document.getIsArchived()) && !securityUtils.isAdmin()) {
            throw new BusinessException(
                    "Document '" + document.getDocumentNumber()
                  + "' is archived. Only administrators can download archived documents.");
        }
    }

    // ─── Private utilities ────────────────────────────────────────────────────

    private Document resolveDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));
    }

    /**
     * Builds the sub-directory path for file storage: {@code documents/YYYY/MM}.
     */
    private String buildSubDirectory() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("documents/%d/%02d", now.getYear(), now.getMonthValue());
    }

    /**
     * Creates a {@link Pageable} from raw request parameters.
     */
    private Pageable buildPageable(Integer page, Integer size, String sortBy, String sortDir) {
        int safePage = (page  != null && page  >= 0) ? page  : 0;
        int safeSize = (size  != null && size  > 0 && size <= 100) ? size : 20;
        Sort.Direction dir = "desc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        String field = (sortBy != null && !sortBy.isBlank()) ? sortBy : "createdAt";
        return PageRequest.of(safePage, safeSize, Sort.by(dir, field));
    }

    /**
     * Human-readable byte count for error messages.
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}