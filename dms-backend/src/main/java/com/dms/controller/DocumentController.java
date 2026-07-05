package com.dms.controller;

import com.dms.constant.ApiConstants;
import com.dms.constant.RoleConstants;
import com.dms.dto.request.DocumentSearchRequest;
import com.dms.dto.request.DocumentUpdateRequest;
import com.dms.dto.request.DocumentUploadRequest;
import com.dms.dto.request.DocumentVersionUploadRequest;
import com.dms.dto.response.ApiResponse;
import com.dms.dto.response.DocumentResponse;
import com.dms.dto.response.DocumentSummaryResponse;
import com.dms.dto.response.DocumentUploadResponse;
import com.dms.dto.response.DocumentVersionResponse;
import com.dms.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(ApiConstants.DOCUMENTS_BASE)
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Document Management", description = "Upload, download, search and manage documents")
public class DocumentController {

    private final DocumentService documentService;

    // ─── POST /documents/upload ───────────────────────────────────────────────

    @Operation(
        summary     = "Upload a new document",
        description = "Accepts multipart/form-data. File size limit: 10 MB. "
                    + "Allowed types: pdf, docx, doc, xlsx, xls, png, jpg, jpeg, txt.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Document uploaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or invalid file"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category or Department not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "413", description = "File exceeds 10 MB limit")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(RoleConstants.IS_AUTHENTICATED)
    public ResponseEntity<ApiResponse<DocumentUploadResponse>> uploadDocument(
            @Parameter(description = "File to upload (max 10 MB)")
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Document title", required = true)
            @RequestParam("title") String title,

            @Parameter(description = "Document category ID", required = true)
            @RequestParam("categoryId") Long categoryId,

            @Parameter(description = "Document description")
            @RequestParam(value = "description", required = false) String description,

            @Parameter(description = "Department ID (optional)")
            @RequestParam(value = "departmentId", required = false) Long departmentId,

            @Parameter(description = "Mark document as confidential")
            @RequestParam(value = "isConfidential", defaultValue = "false") Boolean isConfidential,

            @Parameter(description = "Expiry date in ISO format: yyyy-MM-dd")
            @RequestParam(value = "expiryDate", required = false) String expiryDate,

            @Parameter(description = "Comma-separated tags")
            @RequestParam(value = "tags", required = false) String tags) throws IOException {

        DocumentUploadRequest request = DocumentUploadRequest.builder()
                .file(file)
                .title(title)
                .description(description)
                .categoryId(categoryId)
                .departmentId(departmentId)
                .isConfidential(isConfidential)
                .expiryDate(expiryDate != null ? LocalDate.parse(expiryDate) : null)
                .tags(tags)
                .build();

        DocumentUploadResponse response = documentService.uploadDocument(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document uploaded successfully", response));
    }

    // ─── POST /documents/{documentId}/version ─────────────────────────────────

    @Operation(summary = "Upload a new version of an existing document")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "New version uploaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file or document is archived")
    })
    @PostMapping(value = "/{documentId}/version", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(RoleConstants.IS_AUTHENTICATED)
    public ResponseEntity<ApiResponse<DocumentUploadResponse>> uploadNewVersion(
            @Parameter(description = "ID of the document") @PathVariable Long documentId,
            @Parameter(description = "Replacement file") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Reason for this version upload")
            @RequestParam(value = "uploadReason", required = false) String uploadReason) throws IOException {

        DocumentVersionUploadRequest request = DocumentVersionUploadRequest.builder()
                .file(file)
                .uploadReason(uploadReason)
                .build();

        DocumentUploadResponse response = documentService.uploadNewVersion(documentId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("New version uploaded successfully", response));
    }

    // ─── GET /documents ───────────────────────────────────────────────────────

    @Operation(summary = "Get all non-archived documents (paginated)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Documents returned")
    })
    @GetMapping
    @PreAuthorize(RoleConstants.IS_AUTHENTICATED)
    public ResponseEntity<ApiResponse<Page<DocumentResponse>>> getAllDocuments(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0")             int page,
            @Parameter(description = "Page size")             @RequestParam(defaultValue = "20")            int size,
            @Parameter(description = "Sort field,direction")  @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Page<DocumentResponse> response = documentService.getAllDocuments(buildPageable(page, size, sort));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── GET /documents/{documentId} ──────────────────────────────────────────

    @Operation(summary = "Get document by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found")
    })
    @GetMapping("/{documentId}")
    @PreAuthorize(RoleConstants.IS_AUTHENTICATED)
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocumentById(
            @Parameter(description = "ID of the document") @PathVariable Long documentId) {

        return ResponseEntity.ok(ApiResponse.success(documentService.getDocumentById(documentId)));
    }

    // ─── GET /documents/number/{documentNumber} ───────────────────────────────

    @Operation(summary = "Get document by document number (e.g. DOC-2024-00001)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found")
    })
    @GetMapping("/number/{documentNumber}")
    @PreAuthorize(RoleConstants.IS_AUTHENTICATED)
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocumentByNumber(
            @Parameter(description = "Human-readable document number") @PathVariable String documentNumber) {

        return ResponseEntity.ok(ApiResponse.success(documentService.getDocumentByNumber(documentNumber)));
    }

    // ─── PUT /documents/{documentId} ──────────────────────────────────────────

    @Operation(summary = "Update document metadata (title, description, tags, category, department)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or document is archived")
    })
    @PutMapping("/{documentId}")
    @PreAuthorize(RoleConstants.IS_AUTHENTICATED)
    public ResponseEntity<ApiResponse<DocumentResponse>> updateDocument(
            @Parameter(description = "ID of the document") @PathVariable Long documentId,
            @Valid @RequestBody DocumentUpdateRequest request) {

        DocumentResponse response = documentService.updateDocument(documentId, request);
        return ResponseEntity.ok(ApiResponse.success("Document updated successfully", response));
    }

    // ─── DELETE /documents/{documentId} ───────────────────────────────────────

    @Operation(summary = "Soft-delete a document (sets isArchived = true)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Document deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found")
    })
    @DeleteMapping("/{documentId}")
    @PreAuthorize(RoleConstants.IS_AUTHENTICATED)
    public ResponseEntity<Void> deleteDocument(
            @Parameter(description = "ID of the document") @PathVariable Long documentId) {

        documentService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }

    // ─── POST /documents/{documentId}/archive ─────────────────────────────────

    @Operation(summary = "Archive a document (changes status to ARCHIVED)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document archived"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Document already archived")
    })
    @PostMapping("/{documentId}/archive")
    @PreAuthorize(RoleConstants.IS_AUTHENTICATED)
    public ResponseEntity<ApiResponse<Void>> archiveDocument(
            @Parameter(description = "ID of the document") @PathVariable Long documentId) {

        documentService.archiveDocument(documentId);
        return ResponseEntity.ok(ApiResponse.success("Document archived successfully"));
    }

    // ─── POST /documents/{documentId}/restore ─────────────────────────────────

    @Operation(summary = "Restore an archived document")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document restored"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Document is not archived")
    })
    @PostMapping("/{documentId}/restore")
    @PreAuthorize(RoleConstants.IS_AUTHENTICATED)
    public ResponseEntity<ApiResponse<Void>> restoreDocument(
            @Parameter(description = "ID of the document") @PathVariable Long documentId) {

        documentService.restoreDocument(documentId);
        return ResponseEntity.ok(ApiResponse.success("Document restored successfully"));
    }

    // ─── GET /documents/download/{documentId} ────────────────────────────────

    @Operation(summary = "Download the current (latest) version of a document")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "File bytes returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not the owner and not ADMIN")
    })
    @GetMapping("/download/{documentId}")
    @PreAuthorize(RoleConstants.IS_AUTHENTICATED)
    public ResponseEntity<Resource> downloadDocument(
            @Parameter(description = "ID of the document") @PathVariable Long documentId) throws IOException {

        byte[] fileBytes   = documentService.downloadDocument(documentId);
        DocumentResponse doc = documentService.getDocumentById(documentId);
        String encodedName = URLEncoder.encode(doc.getFileName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedName)
                .contentLength(fileBytes.length)
                .body(new ByteArrayResource(fileBytes));
    }

    // ─── GET /documents/download/{documentId}/version/{versionNumber} ─────────

    @Operation(summary = "Download a specific version of a document")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "File bytes returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document or version not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/download/{documentId}/version/{versionNumber}")
    @PreAuthorize(RoleConstants.IS_AUTHENTICATED)
    public ResponseEntity<Resource> downloadDocumentVersion(
            @Parameter(description = "ID of the document")  @PathVariable Long    documentId,
            @Parameter(description = "Version number")      @PathVariable Integer versionNumber) throws IOException {

        byte[] fileBytes   = documentService.downloadDocumentVersion(documentId, versionNumber);
        DocumentResponse doc = documentService.getDocumentById(documentId);

        // Inject version into filename: report.pdf → report_v2.pdf
        String base        = doc.getFileName();
        int    dotIdx      = base.lastIndexOf('.');
        String versionedName = dotIdx > 0
                ? base.substring(0, dotIdx) + "_v" + versionNumber + base.substring(dotIdx)
                : base + "_v" + versionNumber;
        String encodedName = URLEncoder.encode(versionedName, StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedName)
                .contentLength(fileBytes.length)
                .body(new ByteArrayResource(fileBytes));
    }

    // ─── GET /documents/versions/{documentId} ─────────────────────────────────

    @Operation(summary = "Get all version history entries for a document (newest first)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Version list returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found")
    })
    @GetMapping("/versions/{documentId}")
    @PreAuthorize(RoleConstants.IS_AUTHENTICATED)
    public ResponseEntity<ApiResponse<List<DocumentVersionResponse>>> getDocumentVersions(
            @Parameter(description = "ID of the document") @PathVariable Long documentId) {

        return ResponseEntity.ok(ApiResponse.success(documentService.getDocumentVersions(documentId)));
    }

    // ─── GET /documents/search ────────────────────────────────────────────────

    @Operation(summary = "Full-text search across title, document number, description and tags")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search results returned")
    })
    @GetMapping("/search")
    @PreAuthorize(RoleConstants.IS_AUTHENTICATED)
    public ResponseEntity<ApiResponse<Page<DocumentResponse>>> searchDocuments(
            @Parameter(description = "Search keyword", required = true) @RequestParam String search,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0")             int page,
            @Parameter(description = "Page size")             @RequestParam(defaultValue = "20")            int size,
            @Parameter(description = "Sort field,direction")  @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Page<DocumentResponse> response =
                documentService.searchDocuments(search, buildPageable(page, size, sort));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── POST /documents/advanced-search ──────────────────────────────────────

    @Operation(
        summary     = "Advanced document search with multiple optional filters",
        description = "All fields in the request body are optional. "
                    + "Combine any subset of title, category, department, status, owner, date range.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search results returned")
    })
    @PostMapping("/advanced-search")
    @PreAuthorize(RoleConstants.IS_AUTHENTICATED)
    public ResponseEntity<ApiResponse<Page<DocumentResponse>>> advancedSearch(
            @Valid @RequestBody DocumentSearchRequest request) {

        Pageable pageable = PageRequest.of(
                request.getPage(),
                Math.min(request.getSize(), 100),
                Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy()));

        return ResponseEntity.ok(ApiResponse.success(documentService.advancedSearch(request, pageable)));
    }

    // ─── GET /documents/summary ───────────────────────────────────────────────

    @Operation(summary = "Get aggregate document statistics (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Summary returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Admin access required")
    })
    @GetMapping("/summary")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<DocumentSummaryResponse>> getDocumentSummary() {
        return ResponseEntity.ok(ApiResponse.success(documentService.getDocumentSummary()));
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