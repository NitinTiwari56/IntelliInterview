package com.nitin.ResumeService.service;

import com.nitin.ResumeService.exception.FileSizeLimitExceededException;
import com.nitin.ResumeService.exception.ResumeProcessingException;
import com.nitin.ResumeService.exception.UnsupportedFileException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PdfParserService {

    private static final Logger log = LoggerFactory.getLogger(PdfParserService.class);
    public static final long MAX_FILE_SIZE_BYTES = 2 * 1024 * 1024; // 2 MB (REQ-RES-2)

    public void validatePdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new UnsupportedFileException("Unsupported file format: File is empty or missing.");
        }

        // Check file size (REQ-RES-2)
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new FileSizeLimitExceededException("File too large (maximum 2 MB)");
        }

        // Check file extension and content type (REQ-RES-2)
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new UnsupportedFileException("Unsupported file format");
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.equalsIgnoreCase("application/pdf")
                && !contentType.equalsIgnoreCase("application/octet-stream")) {
            throw new UnsupportedFileException("Unsupported file format");
        }
    }

    /**
     * Extracts plain text from the uploaded PDF resume.
     * Uses Apache PDFBox 3.x Loader.loadPDF()
     */
    public String extractTextFromPdf(MultipartFile file) {
        validatePdfFile(file);

        try {
            byte[] bytes = file.getBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                if (document.isEncrypted()) {
                    throw new ResumeProcessingException("The uploaded PDF is password protected. Please upload an unlocked PDF.");
                }

                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setSortByPosition(true);
                String text = stripper.getText(document);

                if (text == null || text.trim().isEmpty()) {
                    throw new ResumeProcessingException("No readable text found in PDF. Please ensure the resume is a text-based PDF and not a scanned image.");
                }

                String cleanText = text.replaceAll("[\\r\\n]+", "\n").trim();
                log.info("Successfully extracted {} characters from PDF resume '{}'", cleanText.length(), file.getOriginalFilename());
                return cleanText;
            }
        } catch (UnsupportedFileException | FileSizeLimitExceededException | ResumeProcessingException e) {
            throw e;
        } catch (IOException e) {
            log.error("Failed to parse PDF file: {}", e.getMessage(), e);
            throw new ResumeProcessingException("Failed to read or parse PDF file: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error parsing PDF file: {}", e.getMessage(), e);
            throw new ResumeProcessingException("Error processing PDF resume: " + e.getMessage(), e);
        }
    }
}
