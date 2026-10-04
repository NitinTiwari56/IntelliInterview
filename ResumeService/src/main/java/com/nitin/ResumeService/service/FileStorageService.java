package com.nitin.ResumeService.service;

import com.nitin.ResumeService.exception.ResumeProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final Path storageLocation;

    public FileStorageService(@Value("${file.upload-dir:uploads/resumes}") String uploadDir) {
        this.storageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageLocation);
            log.info("Resume storage directory initialized at: {}", this.storageLocation);
        } catch (IOException e) {
            log.error("Could not create upload directory at {}: {}", this.storageLocation, e.getMessage());
            throw new ResumeProcessingException("Could not create file storage directory.", e);
        }
    }

    /**
     * Stores the uploaded PDF resume safely and returns the relative stored path.
     */
    public String storeFile(MultipartFile file, Long studentId) {
        String rawFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume.pdf";
        String originalFilename = StringUtils.cleanPath(rawFilename);

        // Strip any directory path passed in filename
        int lastSeparator = Math.max(originalFilename.lastIndexOf('/'), originalFilename.lastIndexOf('\\'));
        if (lastSeparator >= 0) {
            originalFilename = originalFilename.substring(lastSeparator + 1);
        }

        // Sanitize characters and collapse consecutive dots
        String sanitizedBaseName = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_").replaceAll("\\.{2,}", ".");

        String uniqueFileName = "student_" + studentId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "_" + sanitizedBaseName;

        try {
            Path targetLocation = this.storageLocation.resolve(uniqueFileName).normalize();

            // Standard security check: ensure destination resides inside storage directory
            if (!targetLocation.startsWith(this.storageLocation)) {
                throw new ResumeProcessingException("Cannot store file outside designated directory: " + uniqueFileName);
            }

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored resume file at: {}", targetLocation);

            return targetLocation.toString();
        } catch (IOException ex) {
            log.error("Failed to store file {}: {}", uniqueFileName, ex.getMessage());
            throw new ResumeProcessingException("Failed to store uploaded file: " + ex.getMessage(), ex);
        }
    }

    public Path getStorageLocation() {
        return storageLocation;
    }
}
