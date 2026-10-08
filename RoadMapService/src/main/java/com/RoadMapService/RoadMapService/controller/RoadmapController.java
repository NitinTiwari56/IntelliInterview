package com.RoadMapService.RoadMapService.controller;

import com.RoadMapService.RoadMapService.dto.RoadmapResponse;
import com.RoadMapService.RoadMapService.service.AiRoadmapEngineService;
import com.RoadMapService.RoadMapService.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/roadmap")
public class RoadmapController {

    private final AnalyticsService analyticsService;
    private final AiRoadmapEngineService aiEngine;

    public RoadmapController(AnalyticsService analyticsService, AiRoadmapEngineService aiEngine) {
        this.analyticsService = analyticsService;
        this.aiEngine = aiEngine;
    }

    /**
     * Get or create personalized preparation roadmap.
     * SRS Section 4.7 (REQ-RMP-1 to REQ-RMP-7)
     */
    @GetMapping("/{studentId}")
    public ResponseEntity<RoadmapResponse> getRoadmap(@PathVariable Long studentId) {
        RoadmapResponse response = analyticsService.getStudentRoadmap(studentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Force re-aggregation and regeneration of roadmap.
     */
    @PostMapping("/{studentId}/regenerate")
    public ResponseEntity<RoadmapResponse> regenerateRoadmap(@PathVariable Long studentId) {
        RoadmapResponse response = analyticsService.regenerateStudentRoadmap(studentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Service health and AI status.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "RoadMapService");
        health.put("aiConfigured", aiEngine.isAiConfigured());
        health.put("engineMode", aiEngine.isAiConfigured() ? "LLM_GROQ_GEMINI" : "INTELLIGENT_RULE_BASED_ROADMAP");
        return ResponseEntity.ok(health);
    }
}
