package com.RoadMapService.RoadMapService.controller;

import com.RoadMapService.RoadMapService.dto.DashboardResponse;
import com.RoadMapService.RoadMapService.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final AnalyticsService analyticsService;

    public DashboardController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Get student performance dashboard with charts, readiness score, and weak areas.
     * SRS Section 4.6 (REQ-DSH-1 to REQ-DSH-7)
     */
    @GetMapping("/{studentId}")
    public ResponseEntity<DashboardResponse> getDashboard(@PathVariable Long studentId) {
        DashboardResponse response = analyticsService.getStudentDashboard(studentId);
        return ResponseEntity.ok(response);
    }
}
