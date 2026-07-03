package com.dms.service;

import com.dms.dto.request.DocumentSearchRequest;
import com.dms.dto.request.DocumentUpdateRequest;
import com.dms.dto.request.DocumentUploadRequest;
import com.dms.dto.request.DocumentVersionUploadRequest;
import com.dms.dto.response.DocumentResponse;
import com.dms.dto.response.DocumentSummaryResponse;
import com.dms.dto.response.DocumentUploadResponse;
import com.dms.dto.response.DocumentVersionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface DocumentService {

    /**
     * Uploads a new document, generates a document number, stores the file,
     * creates the initial version (v1), and returns an upload summary.
     */
    DocumentUploadResponse uploadDocument(DocumentUploadRequest request) throws IOException;

    /**
     * Updates the metadata fields of an existing document.
     * Does not touch the file or version; document must not be archived.
     */
    DocumentResponse updateDocument(Long documentId, DocumentUpdateRequest request);

    /** Returns full document details including versions. */
    DocumentResponse getDocumentById(Long documentId);

    /** Returns full document details by human-readable document number. */
    DocumentResponse getDocumentByNumber(String documentNumber);

    /** Returns paginated documents owned by the given user. */
    Page<DocumentResponse> getDocumentsByOwner(Long ownerId, Pageable pageable);

    /** Returns all non-archived documents paginated. */
    Page<DocumentResponse> getAllDocuments(Pageable pageable);

    /** Full-text search across title, documentNumber, description, and tags. */
    Page<DocumentResponse> searchDocuments(String search, Pageable pageable);

    /** Multi-criteria search with optional filter dimensions. */
    Page<DocumentResponse> advancedSearch(DocumentSearchRequest request, Pageable pageable);

    /** Soft-deletes a document (sets isArchived = true). */
    void deleteDocument(Long documentId);

    /** Archives a document and stamps archivedAt. */
    void archiveDocument(Long documentId);

    /** Restores a previously archived document. */
    void restoreDocument(Long documentId);

    /**
     * Uploads a new file version for an existing document.
     * Increments currentVersion and creates a {@link com.dms.entity.DocumentVersion} record.
     */
    DocumentUploadResponse uploadNewVersion(Long documentId, DocumentVersionUploadRequest request) throws IOException;

    /** Returns all versions for a document, newest first. */
    List<DocumentVersionResponse> getDocumentVersions(Long documentId);

    /**
     * Downloads the current (latest) version of a document.
     *
     * @throws IOException if the file cannot be read from disk
     */
    byte[] downloadDocument(Long documentId) throws IOException;

    /**
     * Downloads a specific version of a document.
     *
     * @throws IOException if the file cannot be read from disk
     */
    byte[] downloadDocumentVersion(Long documentId, Integer versionNumber) throws IOException;

    /** Returns aggregated count and storage statistics. */
    DocumentSummaryResponse getDocumentSummary();
}