package com.interviewiq.interview.controller;

import com.interviewiq.interview.dto.InterviewSummaryResponse;
import com.interviewiq.interview.service.InterviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Sirf backend services ke liye (analytics-service). Gateway pe /internal/** ka route nahi hai. */
@RestController
@RequestMapping("/internal/interview")
public class InternalInterviewController {

    private final InterviewService interviewService;

    public InternalInterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @GetMapping("/{studentId}/summary")
    public ResponseEntity<InterviewSummaryResponse> summary(@PathVariable Long studentId) {
        return ResponseEntity.ok(interviewService.getStudentInterviewSummary(studentId));
    }
}
