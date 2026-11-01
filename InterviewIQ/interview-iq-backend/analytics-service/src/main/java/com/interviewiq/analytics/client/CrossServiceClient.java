package com.interviewiq.analytics.client;

import com.interviewiq.analytics.dto.AptitudeSummaryDto;
import com.interviewiq.analytics.dto.CodingSummaryDto;
import com.interviewiq.analytics.dto.InterviewSummaryDto;
import com.interviewiq.analytics.dto.ResumeSummaryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * Doosri services ke INTERNAL endpoints (/internal/**) se data laata hai. Ye gateway pe route nahi hain,
 * isliye sirf backend services hi inhe call kar sakti hain.
 * REQ-DSH-7: ek service band ho ya data na ho (404) toh baaki dashboard phir bhi banta hai.
 */
@Component
public class CrossServiceClient {

    private static final Logger log = LoggerFactory.getLogger(CrossServiceClient.class);

    private final RestTemplate restTemplate;

    @Value("${services.resume.url:http://localhost:8082}")
    private String resumeServiceUrl;

    @Value("${services.interview.url:http://localhost:8085}")
    private String interviewServiceUrl;

    @Value("${services.coding.url:http://localhost:8083}")
    private String codingServiceUrl;

    @Value("${services.aptitude.url:http://localhost:8084}")
    private String aptitudeServiceUrl;

    public CrossServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Optional<ResumeSummaryDto> fetchResumeSummary(Long studentId) {
        return get(resumeServiceUrl + "/internal/resume/" + studentId + "/summary", ResumeSummaryDto.class, "resume-service");
    }

    public Optional<InterviewSummaryDto> fetchInterviewSummary(Long studentId) {
        return get(interviewServiceUrl + "/internal/interview/" + studentId + "/summary", InterviewSummaryDto.class, "interview-service");
    }

    public Optional<CodingSummaryDto> fetchCodingSummary(Long studentId) {
        return get(codingServiceUrl + "/internal/coding/" + studentId + "/summary", CodingSummaryDto.class, "coding-service");
    }

    public Optional<AptitudeSummaryDto> fetchAptitudeSummary(Long studentId) {
        return get(aptitudeServiceUrl + "/internal/aptitude/" + studentId + "/summary", AptitudeSummaryDto.class, "aptitude-service");
    }

    private <T> Optional<T> get(String url, Class<T> type, String serviceName) {
        try {
            return Optional.ofNullable(restTemplate.getForObject(url, type));
        } catch (Exception ex) {
            // 404 = is student ka us module mein koi data nahi; baaki = service band. Dono mein aage badho.
            log.info("{} gave no data ({})", serviceName, ex.getMessage());
            return Optional.empty();
        }
    }
}
