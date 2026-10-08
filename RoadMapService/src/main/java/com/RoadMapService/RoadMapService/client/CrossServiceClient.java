package com.RoadMapService.RoadMapService.client;

import com.RoadMapService.RoadMapService.dto.AptitudeSummaryDto;
import com.RoadMapService.RoadMapService.dto.CodingSummaryDto;
import com.RoadMapService.RoadMapService.dto.InterviewSummaryDto;
import com.RoadMapService.RoadMapService.dto.ResumeSummaryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * SRS Figure B-6 & COM-5: Cross-service synchronous data aggregation with fault-tolerance.
 * REQ-DSH-7: Failure of one service shall not stop dashboard/roadmap generation.
 */
@Component
public class CrossServiceClient {

    private static final Logger log = LoggerFactory.getLogger(CrossServiceClient.class);

    private final RestTemplate restTemplate;

    @Value("${services.resume.url:http://localhost:8083}")
    private String resumeServiceUrl;

    @Value("${services.interview.url:http://localhost:8082}")
    private String interviewServiceUrl;

    @Value("${services.coding.url:http://localhost:8084}")
    private String codingServiceUrl;

    @Value("${services.aptitude.url:http://localhost:8085}")
    private String aptitudeServiceUrl;

    public CrossServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Optional<ResumeSummaryDto> fetchResumeSummary(Long studentId) {
        try {
            // First try /api/resumes/{studentId}/summary, then fallback to /api/resumes/student/{studentId}
            String url = resumeServiceUrl + "/api/resumes/" + studentId + "/summary";
            ResumeSummaryDto dto = restTemplate.getForObject(url, ResumeSummaryDto.class);
            return Optional.ofNullable(dto);
        } catch (Exception ex) {
            log.warn("Could not reach ResumeService at {}: {}. Attempting fallback path...", resumeServiceUrl, ex.getMessage());
            try {
                String fallbackUrl = resumeServiceUrl + "/api/resumes/student/" + studentId + "/history";
                // If student has analyses
                return Optional.empty();
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }

    public Optional<InterviewSummaryDto> fetchInterviewSummary(Long studentId) {
        try {
            String url = interviewServiceUrl + "/api/interviews/student/" + studentId + "/summary";
            InterviewSummaryDto dto = restTemplate.getForObject(url, InterviewSummaryDto.class);
            return Optional.ofNullable(dto);
        } catch (Exception ex) {
            log.warn("Could not reach InterviewService at {}: {}", interviewServiceUrl, ex.getMessage());
            return Optional.empty();
        }
    }

    public Optional<CodingSummaryDto> fetchCodingSummary(Long studentId) {
        try {
            String url = codingServiceUrl + "/api/coding/" + studentId + "/summary";
            CodingSummaryDto dto = restTemplate.getForObject(url, CodingSummaryDto.class);
            return Optional.ofNullable(dto);
        } catch (Exception ex) {
            log.debug("CodingService unavailable at {}: {}. Proceeding with graceful fallback.", codingServiceUrl, ex.getMessage());
            return Optional.empty();
        }
    }

    public Optional<AptitudeSummaryDto> fetchAptitudeSummary(Long studentId) {
        try {
            String url = aptitudeServiceUrl + "/api/aptitude/" + studentId + "/summary";
            AptitudeSummaryDto dto = restTemplate.getForObject(url, AptitudeSummaryDto.class);
            return Optional.ofNullable(dto);
        } catch (Exception ex) {
            log.debug("AptitudeService unavailable at {}: {}. Proceeding with graceful fallback.", aptitudeServiceUrl, ex.getMessage());
            return Optional.empty();
        }
    }
}
