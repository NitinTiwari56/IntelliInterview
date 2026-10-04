package com.nitin.ResumeService.service;

import com.nitin.ResumeService.exception.FileSizeLimitExceededException;
import com.nitin.ResumeService.exception.UnsupportedFileException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PdfParserServiceTest {

    private PdfParserService pdfParserService;

    @BeforeEach
    void setUp() {
        pdfParserService = new PdfParserService();
    }

    @Test
    @DisplayName("Should throw UnsupportedFileException when file is not PDF (REQ-RES-2)")
    void testRejectNonPdfFile() {
        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "Sample Word Resume".getBytes()
        );

        assertThatThrownBy(() -> pdfParserService.extractTextFromPdf(textFile))
                .isInstanceOf(UnsupportedFileException.class)
                .hasMessageContaining("Unsupported file format");
    }

    @Test
    @DisplayName("Should throw FileSizeLimitExceededException when file exceeds 2 MB (REQ-RES-2)")
    void testRejectLargeFile() {
        byte[] largeBytes = new byte[3 * 1024 * 1024]; // 3 MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large_resume.pdf",
                "application/pdf",
                largeBytes
        );

        assertThatThrownBy(() -> pdfParserService.extractTextFromPdf(largeFile))
                .isInstanceOf(FileSizeLimitExceededException.class)
                .hasMessageContaining("File too large (maximum 2 MB)");
    }

    @Test
    @DisplayName("Should successfully extract text from valid text-based PDF (REQ-RES-3)")
    void testExtractTextFromValidPdf() throws Exception {
        byte[] pdfBytes = createSamplePdf("John Doe - Software Engineer with Java and Spring Boot experience.");

        MockMultipartFile validPdf = new MockMultipartFile(
                "file",
                "john_doe_resume.pdf",
                "application/pdf",
                pdfBytes
        );

        String extractedText = pdfParserService.extractTextFromPdf(validPdf);
        assertThat(extractedText).contains("John Doe");
        assertThat(extractedText).contains("Software Engineer");
        assertThat(extractedText).contains("Java");
        assertThat(extractedText).contains("Spring Boot");
    }

    private byte[] createSamplePdf(String content) throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);
                stream.showText(content);
                stream.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }
}
