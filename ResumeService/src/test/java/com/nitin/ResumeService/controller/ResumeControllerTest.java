package com.nitin.ResumeService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.ai.openai.api-key=dummy-test-key",
        "file.upload-dir=target/test-uploads"
})
public class ResumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/resumes/health should report UP")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/resumes/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("ResumeService"));
    }

    @Test
    @DisplayName("GET /api/v1/resumes/target-roles should return role profiles")
    void testTargetRolesEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/resumes/target-roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].roleName").isString())
                .andExpect(jsonPath("$[0].coreSkills").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/resumes/analyze should reject non-PDF file with HTTP 400 (REQ-RES-2)")
    void testRejectNonPdf() throws Exception {
        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello, I am a software engineer.".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/resumes/analyze")
                        .file(textFile)
                        .param("studentId", "101")
                        .param("targetRole", "Backend Engineer"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported file format"));
    }

    @Test
    @DisplayName("POST /api/v1/resumes/analyze should parse PDF, evaluate ATS score, save to DB, and return history")
    void testUploadAndAnalyzePdfEndToEnd() throws Exception {
        byte[] pdfBytes = createTestPdf("""
                Nitin Tiwari
                Email: nitin@iet.ac.in | GitHub: github.com/nitin | LinkedIn: linkedin.com/in/nitin
                Education: B.Tech Information Technology, IET
                Skills: Java, Spring Boot, MySQL, Data Structures, Algorithms, Docker, Git, REST APIs
                Experience: Developed scalable backend microservices reducing response time by 35%.
                """);

        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "nitin_resume.pdf",
                "application/pdf",
                pdfBytes
        );

        // 1. Analyze Resume
        mockMvc.perform(multipart("/api/v1/resumes/analyze")
                        .file(pdfFile)
                        .param("studentId", "202")
                        .param("targetRole", "Software Development Engineer (SDE)"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumeId").isNumber())
                .andExpect(jsonPath("$.studentId").value(202))
                .andExpect(jsonPath("$.targetRole").value("Software Development Engineer (SDE)"))
                .andExpect(jsonPath("$.atsScore").isNumber())
                .andExpect(jsonPath("$.formatScore").isNumber())
                .andExpect(jsonPath("$.extractedSkills").isArray())
                .andExpect(jsonPath("$.matchedSkills").isArray())
                .andExpect(jsonPath("$.suggestions").isArray())
                .andExpect(jsonPath("$.summaryFeedback").isString())
                .andExpect(jsonPath("$.extractedData").isMap())
                .andExpect(jsonPath("$.extractedData.candidateInfo.email").value("nitin@iet.ac.in"))
                .andExpect(jsonPath("$.extractedData.rawText").isString());

        // 2. Query History
        mockMvc.perform(get("/api/v1/resumes/history/202"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(202))
                .andExpect(jsonPath("$.totalAnalyses").value(1))
                .andExpect(jsonPath("$.history[0].fileName").value("nitin_resume.pdf"));

        // 3. Query Latest
        mockMvc.perform(get("/api/v1/resumes/latest/202"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(202))
                .andExpect(jsonPath("$.atsScore").isNumber());
    }

    private byte[] createTestPdf(String text) throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);

                for (String line : text.split("\n")) {
                    stream.showText(line.trim());
                    stream.newLineAtOffset(0, -18);
                }
                stream.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }
}
