package com.interviewiq.analytics.controller;

import com.interviewiq.analytics.dto.RoadmapResponse;
import com.interviewiq.analytics.service.AiRoadmapEngineService;
import com.interviewiq.analytics.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/analytics/roadmap")
public class RoadmapController {

    private final AnalyticsService analyticsService;
    private final AiRoadmapEngineService aiEngine;

    public RoadmapController(AnalyticsService analyticsService, AiRoadmapEngineService aiEngine) {
        this.analyticsService = analyticsService;
        this.aiEngine = aiEngine;
    }

    /** Mera personalized roadmap (REQ-RMP-1..7). Purana ho gaya ho toh apne aap dobara banta hai. */
    @GetMapping
    public ResponseEntity<RoadmapResponse> getRoadmap(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(analyticsService.getStudentRoadmap(userId));
    }

    /** "Refresh" button: ab ke latest scores se roadmap turant dobara banao. */
    @PostMapping("/regenerate")
    public ResponseEntity<RoadmapResponse> regenerateRoadmap(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(analyticsService.regenerateStudentRoadmap(userId));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "analytics-service");
        health.put("aiConfigured", aiEngine.isAiConfigured());
        health.put("engineMode", aiEngine.isAiConfigured() ? "LLM_GROQ" : "RULE_BASED_ROADMAP");
        return ResponseEntity.ok(health);
    }
}
