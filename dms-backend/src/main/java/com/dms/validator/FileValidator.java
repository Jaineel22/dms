package com.dms.validator;

import com.dms.exception.DocumentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * Spring-managed validator for uploaded files.
 *
 * <p>Centralises all file-type and file-size rules so they can be reused from
 * both {@code DocumentServiceImpl} and any future import / bulk-upload service.</p>
 */
@Slf4j
@Component
public class FileValidator {

    // ─── Allowed types ────────────────────────────────────────────────────────

    public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx",
            "png", "jpg", "jpeg", "txt"
    );

    public static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "image/png",
            "image/jpeg",
            "text/plain"
    );

    // ─── Limits ───────────────────────────────────────────────────────────────

    /** Maximum allowed file size: 10 MB in bytes. */
    public static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    // ─── Composite validation ─────────────────────────────────────────────────

    /**
     * Validates a file against all rules: non-empty, size, and type.
     *
     * @param file the uploaded file
     * @throws DocumentException if any rule is violated
     */
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DocumentException("File is required and cannot be empty.");
        }
        validateFileSize(file);
        validateFileType(file);
    }

    // ─── Individual checks ────────────────────────────────────────────────────

    /**
     * Validates the file size against the 10 MB limit.
     *
     * @throws DocumentException if the file exceeds the limit
     */
    public void validateFileSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new DocumentException(String.format(
                    "File size %.1f MB exceeds the maximum allowed limit of %d MB.",
                    file.getSize() / (1024.0 * 1024),
                    MAX_FILE_SIZE / (1024 * 1024)));
        }
    }

    /**
     * Validates the file extension (and optionally logs a MIME-type mismatch).
     *
     * <p>The extension is treated as the authoritative source of truth because
     * browsers sometimes send incorrect MIME types for the same content.</p>
     *
     * @throws DocumentException if the extension is missing or not allowed
     */
    public void validateFileType(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new DocumentException("File name is invalid or missing.");
        }

        String extension = getFileExtension(fileName);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new DocumentException(String.format(
                    "File type '%s' is not allowed. Allowed types: %s.",
                    extension,
                    String.join(", ", ALLOWED_EXTENSIONS)));
        }

        // MIME type check — informational only; extension takes precedence
        String mimeType = file.getContentType();
        if (mimeType != null && !ALLOWED_MIME_TYPES.contains(mimeType)) {
            log.warn("File '{}' has extension '{}' but MIME type '{}' is not in the allowed list. "
                    + "Proceeding based on extension.", fileName, extension, mimeType);
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Extracts the lowercase extension from a filename.
     *
     * @param fileName original file name
     * @return lowercase extension (e.g. {@code "pdf"}), or {@code null} if absent
     */
    public String getFileExtension(String fileName) {
        if (fileName == null) return null;
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return null;
    }

    /**
     * Sanitises a filename by replacing path-traversal and shell-unsafe characters
     * with underscores.
     *
     * @param fileName raw filename from the client
     * @return sanitised filename safe for use in file-system paths
     */
    public String sanitizeFileName(String fileName) {
        if (fileName == null) return null;
        return fileName.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
    }

    /**
     * Returns {@code true} if the given filename has an allowed extension.
     */
    public boolean isValidFileType(String fileName) {
        String ext = getFileExtension(fileName);
        return ext != null && ALLOWED_EXTENSIONS.contains(ext);
    }

    /**
     * Returns {@code true} if the given byte count is within the allowed limit.
     */
    public boolean isValidFileSize(long fileSize) {
        return fileSize > 0 && fileSize <= MAX_FILE_SIZE;
    }
}