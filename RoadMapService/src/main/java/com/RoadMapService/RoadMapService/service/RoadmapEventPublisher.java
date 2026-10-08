package com.RoadMapService.RoadMapService.service;

import com.RoadMapService.RoadMapService.dto.RoadmapUpdatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class RoadmapEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RoadmapEventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Value("${kafka.dispatch.enabled:false}")
    private boolean kafkaDispatchEnabled;

    public RoadmapEventPublisher(ApplicationEventPublisher applicationEventPublisher,
                                 @Autowired(required = false) KafkaTemplate<String, String> kafkaTemplate,
                                 ObjectMapper objectMapper) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Publishes RoadmapUpdated event for Notification Service (SRS Table 3-4 / REQ-RMP-8).
     */
    public void publishRoadmapUpdatedEvent(RoadmapUpdatedEvent event) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            log.info("[EVENT DISPATCH] Published RoadmapUpdated event for Student ID {} (Roadmap ID: {}): {}",
                    event.getStudentId(), event.getRoadmapId(), jsonPayload);

            // In-process Spring event dispatch
            applicationEventPublisher.publishEvent(event);

            // Dispatch to Kafka topic if enabled and template is available
            if (kafkaDispatchEnabled && kafkaTemplate != null) {
                kafkaTemplate.send("roadmap-updated-events", String.valueOf(event.getStudentId()), jsonPayload)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.debug("Kafka dispatch to 'roadmap-updated-events' skipped: {}", ex.getMessage());
                            } else {
                                log.info("Successfully sent RoadmapUpdated event to Kafka topic 'roadmap-updated-events'");
                            }
                        });
            }
        } catch (Exception ex) {
            log.warn("Failed to serialize or publish RoadmapUpdated event: {}", ex.getMessage());
        }
    }
}
