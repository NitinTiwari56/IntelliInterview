package com.InterviewPrep.InterviewPrep.service;

import com.InterviewPrep.InterviewPrep.dto.InterviewCompletedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class InterviewEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(InterviewEventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;

    public InterviewEventPublisher(ApplicationEventPublisher applicationEventPublisher, ObjectMapper objectMapper) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.objectMapper = objectMapper;
    }

    /**
     * Publishes InterviewCompleted event for Analytics Service (SRS REQ-INT-13 / Table 3-4).
     */
    public void publishInterviewCompletedEvent(InterviewCompletedEvent event) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            log.info("[EVENT DISPATCH] Published InterviewCompleted event for Student ID {} (Session: {}): {}",
                    event.getStudentId(), event.getSessionId(), jsonPayload);

            // In-process Spring event dispatch (can be connected to Kafka producer in distributed setup)
            applicationEventPublisher.publishEvent(event);

        } catch (Exception ex) {
            log.warn("Failed to serialize or publish InterviewCompleted event: {}", ex.getMessage());
        }
    }
}
