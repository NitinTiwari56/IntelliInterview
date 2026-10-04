package com.nitin.ResumeService.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nitin.ResumeService.dto.ResumeAnalyzedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class ResumeEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ResumeEventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;

    public ResumeEventPublisher(ApplicationEventPublisher applicationEventPublisher, ObjectMapper objectMapper) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.objectMapper = objectMapper;
    }

    /**
     * Publishes ResumeAnalyzed event for Analytics Service (SRS REQ-RES-8 / Table 3-4).
     */
    public void publishResumeAnalyzedEvent(ResumeAnalyzedEvent event) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            log.info("[EVENT DISPATCH] Published ResumeAnalyzed event for Student ID {}: {}", event.getStudentId(), jsonPayload);

            // Publish Spring in-process event
            applicationEventPublisher.publishEvent(event);

        } catch (Exception ex) {
            log.warn("Failed to serialize or publish ResumeAnalyzed event: {}", ex.getMessage());
        }
    }
}
