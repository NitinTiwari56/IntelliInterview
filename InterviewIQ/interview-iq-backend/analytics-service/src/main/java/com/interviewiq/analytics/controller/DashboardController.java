package com.interviewiq.analytics.controller;

import com.interviewiq.analytics.dto.DashboardResponse;
import com.interviewiq.analytics.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** User ki pehchaan sirf gateway ke "X-User-Id" header se. Koi doosre ka dashboard nahi dekh sakta. */
@RestController
@RequestMapping("/analytics/dashboard")
public class DashboardController {

    private final AnalyticsService analyticsService;

    public DashboardController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /** Mera dashboard: readiness score, module-wise scores, chart data, weak areas, strengths (REQ-DSH-1..7). */
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(analyticsService.getStudentDashboard(userId));
    }
}
