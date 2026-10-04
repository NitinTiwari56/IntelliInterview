package com.nitin.ResumeService.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class FileStorageServiceTest {

    @Test
    @DisplayName("Should successfully store file with multiple dots like Resume1.1...pdf")
    void testStoreFileWithMultipleDots() throws Exception {
        FileStorageService service = new FileStorageService("target/test-storage");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Resume1.1...pdf",
                "application/pdf",
                "%PDF-1.4 test dummy content".getBytes()
        );

        String storedPath = service.storeFile(file, 101L);
        assertThat(storedPath).isNotBlank();
        assertThat(Files.exists(Path.of(storedPath))).isTrue();
    }
}
