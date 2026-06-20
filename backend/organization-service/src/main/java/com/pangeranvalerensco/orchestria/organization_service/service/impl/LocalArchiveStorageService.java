package com.pangeranvalerensco.orchestria.organization_service.service.impl;

import com.pangeranvalerensco.orchestria.organization_service.service.ArchiveStorageService;

import com.pangeranvalerensco.orchestria.organization_service.exception.ArchiveFileTooLargeException;
import com.pangeranvalerensco.orchestria.organization_service.exception.ArchiveInvalidFileException;
import com.pangeranvalerensco.orchestria.organization_service.exception.ArchiveStorageException;
import com.pangeranvalerensco.orchestria.organization_service.exception.ArchiveUnsupportedMediaTypeException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class LocalArchiveStorageService implements ArchiveStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "image/png",
            "image/jpeg"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "docx", "xlsx", "png", "jpg", "jpeg"
    );

    @Value("${archive.storage.path:./storage/archive}")
    private String storagePath;

    @Value("${archive.storage.max-size-bytes:10485760}")
    private long maxSizeBytes;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        rootLocation = Paths.get(storagePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
            log.info("Archive storage initialized at: {}", rootLocation);
        } catch (IOException e) {
            throw new ArchiveStorageException(
                    "Tidak dapat menginisialisasi direktori storage arsip", e);
        }
    }

    @Override
    public StorageResult store(MultipartFile file) {
        // 1. Tolak file kosong
        if (file.isEmpty() || file.getSize() == 0) {
            throw new ArchiveInvalidFileException("File tidak boleh kosong");
        }

        // 2. Cek ukuran
        if (file.getSize() > maxSizeBytes) {
            throw new ArchiveFileTooLargeException(maxSizeBytes);
        }

        // 3. Validasi content type dari header multipart
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ArchiveUnsupportedMediaTypeException(
                    contentType != null ? contentType : "unknown");
        }

        // 4. Sanitasi dan validasi extension dari original filename
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new ArchiveInvalidFileException("Nama file tidak valid");
        }

        String sanitizedExt = sanitizeExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(sanitizedExt)) {
            throw new ArchiveUnsupportedMediaTypeException("." + sanitizedExt);
        }

        // 4b. Validasi kecocokan extension dan content type
        boolean mismatch = false;
        if (sanitizedExt.equals("pdf") && !contentType.equals("application/pdf")) mismatch = true;
        if (sanitizedExt.equals("docx") && !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) mismatch = true;
        if (sanitizedExt.equals("xlsx") && !contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) mismatch = true;
        if (sanitizedExt.equals("png") && !contentType.equals("image/png")) mismatch = true;
        if ((sanitizedExt.equals("jpg") || sanitizedExt.equals("jpeg")) && !contentType.equals("image/jpeg")) mismatch = true;

        if (mismatch) {
            throw new ArchiveUnsupportedMediaTypeException("Content type tidak cocok dengan extension file");
        }

        // 5. Stored filename menggunakan UUID — jangan pakai original filename
        String storedFileName = UUID.randomUUID() + "." + sanitizedExt;

        // 6. Path traversal prevention
        Path targetPath = rootLocation.resolve(storedFileName).normalize();
        if (!targetPath.startsWith(rootLocation)) {
            throw new ArchiveStorageException("Tidak dapat menyimpan file di luar direktori storage");
        }

        // 7. Simpan file
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Archive file stored: {} -> {}", originalFilename, storedFileName);
        } catch (IOException e) {
            throw new ArchiveStorageException("Gagal menyimpan file ke storage", e);
        }

        return new StorageResult(storedFileName, storedFileName);
    }

    @Override
    public InputStream retrieve(String storageReference) {
        // Path traversal prevention — jangan percaya path dari client
        Path filePath = rootLocation.resolve(storageReference).normalize();
        if (!filePath.startsWith(rootLocation)) {
            throw new ArchiveStorageException("Akses storage tidak sah");
        }

        if (!Files.exists(filePath)) {
            throw new ArchiveStorageException("File arsip tidak ditemukan di storage");
        }

        try {
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new ArchiveStorageException("Gagal membaca file dari storage", e);
        }
    }

    @Override
    public void delete(String storageReference) {
        if (storageReference == null || storageReference.isBlank()) return;

        Path filePath = rootLocation.resolve(storageReference).normalize();
        if (!filePath.startsWith(rootLocation)) {
            log.warn("Mencoba menghapus file di luar storage root: {}", storageReference);
            return;
        }

        try {
            Files.deleteIfExists(filePath);
            log.info("File arsip dihapus dari storage: {}", storageReference);
        } catch (IOException e) {
            log.warn("Gagal menghapus file arsip: {}", storageReference, e);
        }
    }


    private String sanitizeExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1)
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");
    }
}
