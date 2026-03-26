package com.bookvault.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
            log.info("File storage initialized at: {}", uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public String storePdf(MultipartFile file) {
        validatePdfFile(file);

        String originalName = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = ".pdf";
        String fileName = UUID.randomUUID().toString() + extension;

        try {
            Path targetPath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("PDF stored: {}", fileName);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store PDF file", e);
        }
    }

    public Path getPdfPath(String fileName) {
        // Prevent path traversal
        String cleanFileName = Paths.get(fileName).getFileName().toString();
        return uploadPath.resolve(cleanFileName).normalize();
    }

    public void deleteFile(String fileName) {
        try {
            String cleanFileName = Paths.get(fileName).getFileName().toString();
            Path filePath = uploadPath.resolve(cleanFileName).normalize();
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", cleanFileName);
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", fileName, e);
        }
    }

    private void validatePdfFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String contentType = file.getContentType();
        if (!"application/pdf".equals(contentType)) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }
        if (file.getSize() > 52_428_800L) { // 50 MB
            throw new IllegalArgumentException("PDF file size must be under 50MB");
        }
        String name = StringUtils.cleanPath(file.getOriginalFilename());
        if (name.contains("..") || name.contains("/") || name.contains("\\")) {
            throw new IllegalArgumentException("Invalid file name");
        }
    }
}
