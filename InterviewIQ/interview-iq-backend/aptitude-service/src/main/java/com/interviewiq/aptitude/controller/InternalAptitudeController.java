package com.interviewiq.aptitude.controller;

import com.interviewiq.aptitude.dto.AptitudeDtos.AptitudeSummary;
import com.interviewiq.aptitude.service.AptitudeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Sirf backend services ke liye (analytics-service). Gateway pe /internal/** ka route nahi hai. */
@RestController
@RequestMapping("/internal/aptitude")
public class InternalAptitudeController {

    private final AptitudeService aptitudeService;

    public InternalAptitudeController(AptitudeService aptitudeService) {
        this.aptitudeService = aptitudeService;
    }

    @GetMapping("/{studentId}/summary")
    public AptitudeSummary summary(@PathVariable Long studentId) {
        return aptitudeService.summary(studentId);
    }
}
