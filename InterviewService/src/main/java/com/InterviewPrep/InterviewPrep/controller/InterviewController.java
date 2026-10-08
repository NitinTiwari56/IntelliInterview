package com.InterviewPrep.InterviewPrep.controller;

import com.InterviewPrep.InterviewPrep.dto.*;
import com.InterviewPrep.InterviewPrep.service.AiInterviewEngineService;
import com.InterviewPrep.InterviewPrep.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService interviewService;
    private final AiInterviewEngineService aiEngine;

    public InterviewController(InterviewService interviewService, AiInterviewEngineService aiEngine) {
        this.interviewService = interviewService;
        this.aiEngine = aiEngine;
    }

    /**
     * Start a new Technical or HR interview session (REQ-INT-1, REQ-INT-3, REQ-INT-15).
     */
    @PostMapping("/sessions/start")
    public ResponseEntity<InterviewSessionResponse> startSession(@Valid @RequestBody InterviewStartRequest request) {
        InterviewSessionResponse response = interviewService.startSession(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Submit an answer to the current question (REST alternative to WebSocket).
     */
    @PostMapping("/sessions/{sessionId}/answers")
    public ResponseEntity<AnswerEvaluationDto> submitAnswer(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> body) {
        String answer = body.get("answer");
        InterviewAnswerRequest req = new InterviewAnswerRequest(sessionId, answer != null ? answer : "");
        AnswerEvaluationDto evaluation = interviewService.submitAnswer(req);
        return ResponseEntity.ok(evaluation);
    }

    /**
     * Get interview session details and full question history.
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<InterviewSessionResponse> getSession(@PathVariable String sessionId) {
        InterviewSessionResponse session = interviewService.getSession(sessionId);
        return ResponseEntity.ok(session);
    }

    /**
     * Reconnect to an in-progress session (REQ-INT-17 / TBD-17).
     */
    @PostMapping("/sessions/{sessionId}/reconnect")
    public ResponseEntity<InterviewSessionResponse> reconnectSession(@PathVariable String sessionId) {
        InterviewSessionResponse session = interviewService.reconnectSession(sessionId);
        return ResponseEntity.ok(session);
    }

    /**
     * End session early or manually (REQ-INT-14).
     */
    @PostMapping("/sessions/{sessionId}/end")
    public ResponseEntity<InterviewSessionResponse> endSession(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "true") boolean markAsIncomplete) {
        InterviewSessionResponse response = interviewService.endSession(sessionId, markAsIncomplete);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all interview sessions for a specific student.
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<InterviewSessionResponse>> getStudentSessions(@PathVariable Long studentId) {
        List<InterviewSessionResponse> sessions = interviewService.getStudentSessions(studentId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Summary endpoint for Analytics / Roadmap service and student dashboard (SRS Figure B-6).
     */
    @GetMapping("/student/{studentId}/summary")
    public ResponseEntity<InterviewSummaryResponse> getStudentInterviewSummary(@PathVariable Long studentId) {
        InterviewSummaryResponse summary = interviewService.getStudentInterviewSummary(studentId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Alias endpoint for Analytics service as depicted in Figure B-6 sequence diagram:
     * GET /api/interviews/{studentId}/summary
     */
    @GetMapping("/{studentId}/summary")
    public ResponseEntity<InterviewSummaryResponse> getStudentInterviewSummaryAlias(@PathVariable Long studentId) {
        InterviewSummaryResponse summary = interviewService.getStudentInterviewSummary(studentId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Health and AI status check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "InterviewService");
        health.put("aiConfigured", aiEngine.isAiConfigured());
        health.put("mode", aiEngine.isAiConfigured() ? "LLM_GROQ_GEMINI" : "INTELLIGENT_RULE_BASED_FALLBACK");
        return ResponseEntity.ok(health);
    }
}
