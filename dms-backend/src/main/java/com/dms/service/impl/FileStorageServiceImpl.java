package com.dms.service.impl;

import com.dms.repository.DocumentRepository;
import com.dms.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    // ─── Configuration ────────────────────────────────────────────────────────

    @Value("${file.storage.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.storage.max-file-size:10485760}")
    private long maxFileSize;

    private Path uploadRoot;

    private final DocumentRepository documentRepository;

    public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "pdf", "docx", "doc", "xlsx", "xls", "png", "jpg", "jpeg", "txt"
    );

    public static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel",
            "image/png",
            "image/jpeg",
            "text/plain"
    );

    private static final Map<String, String> EXTENSION_MIME_MAP = Map.of(
            "pdf",  "application/pdf",
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "doc",  "application/msword",
            "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "xls",  "application/vnd.ms-excel",
            "png",  "image/png",
            "jpg",  "image/jpeg",
            "jpeg", "image/jpeg",
            "txt",  "text/plain"
    );

    // ─── Init ─────────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() throws IOException {
        uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadRoot);
        Files.createDirectories(uploadRoot.resolve("documents"));
        Files.createDirectories(uploadRoot.resolve("temp"));
        log.info("File storage initialised at: {}", uploadRoot);
    }

    // ─── Store ────────────────────────────────────────────────────────────────

    @Override
    public String storeFile(MultipartFile file, String subDirectory) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Cannot store empty file");
        }

        String uniqueFileName = generateUniqueFileName(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");

        Path targetDir  = uploadRoot.resolve(subDirectory).normalize();
        Files.createDirectories(targetDir);

        Path targetPath = targetDir.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Return relative path for DB storage
        String relative = uploadRoot.relativize(targetPath).toString().replace("\\", "/");
        log.debug("Stored file: {} → {}", file.getOriginalFilename(), relative);
        return relative;
    }

    // ─── Load ─────────────────────────────────────────────────────────────────

    @Override
    public byte[] loadFileAsBytes(String filePath) throws IOException {
        Path resolved = getFilePath(filePath);
        if (!Files.exists(resolved)) {
            throw new IOException("File not found: " + filePath);
        }
        return Files.readAllBytes(resolved);
    }

    @Override
    public Path getFilePath(String filePath) {
        return uploadRoot.resolve(filePath).normalize();
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    @Override
    public void deleteFile(String filePath) throws IOException {
        Path resolved = getFilePath(filePath);
        if (Files.exists(resolved)) {
            Files.delete(resolved);
            log.debug("Deleted file: {}", filePath);
        } else {
            log.warn("deleteFile: file not found (already deleted?): {}", filePath);
        }
    }

    // ─── Checks ───────────────────────────────────────────────────────────────

    @Override
    public boolean fileExists(String filePath) {
        return Files.exists(getFilePath(filePath));
    }

    @Override
    public boolean isValidFileType(String fileName) {
        if (fileName == null || fileName.isBlank()) return false;
        return ALLOWED_EXTENSIONS.contains(getFileExtension(fileName));
    }

    @Override
    public boolean isValidFileSize(long fileSize) {
        return fileSize > 0 && fileSize <= maxFileSize;
    }

    // ─── Metadata helpers ─────────────────────────────────────────────────────

    @Override
    public String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "";
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    @Override
    public String getContentType(String fileName) {
        return EXTENSION_MIME_MAP.getOrDefault(getFileExtension(fileName), "application/octet-stream");
    }

    // ─── Name generation ──────────────────────────────────────────────────────

    @Override
    public String generateUniqueFileName(String originalFileName) {
        String ext  = getFileExtension(originalFileName);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return ext.isEmpty() ? uuid : uuid + "." + ext;
    }

    /**
     * Generates the next sequential document number: {@code DOC-YYYY-NNNNN}.
     * The sequence is based on total documents count; not transactionally locked
     * — for production, replace with a DB sequence or Redis counter.
     */
    @Override
    public String generateDocumentNumber() {
        long count = documentRepository.count() + 1;
        return String.format("DOC-%d-%05d", Year.now().getValue(), count);
    }

    // ─── Storage stats ────────────────────────────────────────────────────────

    @Override
    public long getTotalStorageSize() throws IOException {
        if (!Files.exists(uploadRoot)) return 0L;
        try (var stream = Files.walk(uploadRoot)) {
            return stream
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try { return Files.size(p); }
                        catch (IOException e) { return 0L; }
                    })
                    .sum();
        }
    }
}