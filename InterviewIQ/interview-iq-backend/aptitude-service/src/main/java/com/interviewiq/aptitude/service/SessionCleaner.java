package com.interviewiq.aptitude.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Har 30 sec: chhode hue tests band karo, purane review memory se hatao. */
@Component
public class SessionCleaner {

    private static final Logger log = LoggerFactory.getLogger(SessionCleaner.class);

    private final AptitudeService aptitudeService;

    public SessionCleaner(AptitudeService aptitudeService) {
        this.aptitudeService = aptitudeService;
    }

    @Scheduled(fixedDelayString = "${aptitude.auto-submit-check-ms:30000}")
    public void run() {
        try {
            aptitudeService.cleanupSessions();
        } catch (Exception e) {
            log.error("Session cleanup failed", e);
        }
    }
}
