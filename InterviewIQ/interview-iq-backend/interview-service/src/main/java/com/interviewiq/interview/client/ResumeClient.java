package com.interviewiq.interview.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

/**
 * Resume service (internal endpoint) se student ka latest resume laata hai, taaki interview ke questions
 * uske skills/projects pe based hon (REQ-INT-3). Resume na ho ya service band ho toh interview phir bhi chalta hai (REQ-INT-15).
 */
@Component
public class ResumeClient {

    private static final Logger log = LoggerFactory.getLogger(ResumeClient.class);
    private static final int MAX_RESUME_CHARS = 6000;   // prompt chhota rakhne ke liye

    private final RestClient restClient;

    public ResumeClient(@Value("${services.resume.url:http://localhost:8082}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(4000);
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public record ResumeContext(String targetRole, List<String> skills, String resumeText) {}

    public Optional<ResumeContext> fetchLatest(Long studentId) {
        try {
            ResumeResponse r = restClient.get()
                    .uri("/internal/resume/latest/{id}", studentId)
                    .retrieve()
                    .body(ResumeResponse.class);
            if (r == null) {
                return Optional.empty();
            }
            String text = r.extractedData() != null ? r.extractedData().rawText() : null;
            if (text != null && text.length() > MAX_RESUME_CHARS) {
                text = text.substring(0, MAX_RESUME_CHARS);
            }
            List<String> skills = r.extractedSkills() != null ? r.extractedSkills() : List.of();
            return Optional.of(new ResumeContext(r.targetRole(), skills, text));
        } catch (Exception e) {
            // 404 (resume nahi hai) ya service down: dono mein fallback
            log.info("No resume context for student {} ({})", studentId, e.getMessage());
            return Optional.empty();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ResumeResponse(String targetRole, List<String> extractedSkills, ExtractedData extractedData) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ExtractedData(String rawText) {}
}
