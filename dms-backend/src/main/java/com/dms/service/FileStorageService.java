package com.dms.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {

    /**
     * Persists {@code file} under {@code subDirectory} inside the configured upload root.
     * Returns the relative path string (e.g. {@code documents/2024/01/abc.pdf}) that should
     * be stored in the database.
     *
     * @param file         the uploaded multipart file
     * @param subDirectory e.g. {@code "documents/2024/01"}
     * @return relative stored path
     * @throws IOException if the write fails
     */
    String storeFile(MultipartFile file, String subDirectory) throws IOException;

    /**
     * Reads the file at the given stored (relative) path and returns its bytes.
     *
     * @param filePath relative path returned by {@link #storeFile}
     * @return raw bytes of the file
     * @throws IOException if the file cannot be read
     */
    byte[] loadFileAsBytes(String filePath) throws IOException;

    /**
     * Returns the absolute {@link Path} for a stored relative file path.
     */
    Path getFilePath(String filePath);

    /**
     * Deletes the file at the given relative path.
     *
     * @throws IOException if deletion fails
     */
    void deleteFile(String filePath) throws IOException;

    /**
     * Returns {@code true} if the file exists on disk.
     */
    boolean fileExists(String filePath);

    /**
     * Extracts the lowercase extension from a filename (e.g. {@code "pdf"}).
     */
    String getFileExtension(String fileName);

    /**
     * Returns the MIME type for a given filename based on its extension.
     */
    String getContentType(String fileName);

    /**
     * Returns {@code true} if the file's extension is on the allowed list.
     */
    boolean isValidFileType(String fileName);

    /**
     * Returns {@code true} if {@code fileSize} is within the configured limit (10 MB).
     */
    boolean isValidFileSize(long fileSize);

    /**
     * Generates a unique on-disk filename to avoid collisions while preserving
     * the original extension.
     */
    String generateUniqueFileName(String originalFileName);

    /**
     * Generates the next sequential document number in the format {@code DOC-YYYY-NNNNN}.
     * The sequence counter is based on the total documents already in the repository.
     */
    String generateDocumentNumber();

    /**
     * Scans the upload root and returns the total bytes used by all stored files.
     *
     * @throws IOException if the directory walk fails
     */
    long getTotalStorageSize() throws IOException;
}