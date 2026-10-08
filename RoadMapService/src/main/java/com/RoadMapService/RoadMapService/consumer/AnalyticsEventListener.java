package com.RoadMapService.RoadMapService.consumer;

import com.RoadMapService.RoadMapService.service.AnalyticsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * SRS Table 3-4 & REQ-RMP-6: Asynchronous Event Consumer for:
 * - ResumeAnalyzed
 * - InterviewCompleted
 * - SubmissionEvaluated
 * - AptitudeAttempted
 */
@Component
public class AnalyticsEventListener {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsEventListener.class);

    private final AnalyticsService analyticsService;
    private final ObjectMapper objectMapper;

    public AnalyticsEventListener(AnalyticsService analyticsService, ObjectMapper objectMapper) {
        this.analyticsService = analyticsService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "resume-analyzed-events", groupId = "analytics-roadmap-group", autoStartup = "${spring.kafka.listener.auto-startup:false}")
    public void onResumeAnalyzed(String payload) {
        processGenericEvent("RESUME", payload);
    }

    @KafkaListener(topics = "interview-completed-events", groupId = "analytics-roadmap-group", autoStartup = "${spring.kafka.listener.auto-startup:false}")
    public void onInterviewCompleted(String payload) {
        processGenericEvent("INTERVIEW", payload);
    }

    @KafkaListener(topics = "submission-evaluated-events", groupId = "analytics-roadmap-group", autoStartup = "${spring.kafka.listener.auto-startup:false}")
    public void onSubmissionEvaluated(String payload) {
        processGenericEvent("CODING", payload);
    }

    @KafkaListener(topics = "aptitude-attempted-events", groupId = "analytics-roadmap-group", autoStartup = "${spring.kafka.listener.auto-startup:false}")
    public void onAptitudeAttempted(String payload) {
        processGenericEvent("APTITUDE", payload);
    }

    private void processGenericEvent(String category, String payload) {
        try {
            log.info("[KAFKA CONSUMER] Received {} event: {}", category, payload);
            JsonNode root = objectMapper.readTree(payload);
            Long studentId = root.has("studentId") ? root.get("studentId").asLong() : null;
            Double score = root.has("score") ? root.get("score").asDouble() :
                           root.has("averageScore") ? root.get("averageScore").asDouble() :
                           root.has("atsScore") ? root.get("atsScore").asDouble() : null;

            if (studentId != null) {
                analyticsService.processModuleUpdateEvent(studentId, category, score, payload);
            }
        } catch (Exception e) {
            log.warn("Failed to process {} event payload: {}", category, e.getMessage());
        }
    }
}
