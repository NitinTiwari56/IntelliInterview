package com.interviewiq.interview.controller;

import com.interviewiq.interview.client.ResumeClient;
import com.interviewiq.interview.client.ResumeClient.ResumeContext;
import com.interviewiq.interview.dto.*;
import com.interviewiq.interview.exception.InvalidInterviewSessionException;
import com.interviewiq.interview.exception.ResourceNotFoundException;
import com.interviewiq.interview.service.AiInterviewEngineService;
import com.interviewiq.interview.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Student-facing REST APIs (gateway ke through: /interview/**).
 * User ki pehchaan sirf "X-User-Id" header se. Session sirf uska owner hi dekh/jawab de sakta hai.
 */
@RestController
@RequestMapping("/interview")
public class InterviewController {

    private static final String DEFAULT_ROLE = "Software Development Engineer";
    private static final int MAX_ANSWER_CHARS = 5000;

    private final InterviewService interviewService;
    private final AiInterviewEngineService aiEngine;
    private final ResumeClient resumeClient;

    public InterviewController(InterviewService interviewService, AiInterviewEngineService aiEngine,
                               ResumeClient resumeClient) {
        this.interviewService = interviewService;
        this.aiEngine = aiEngine;
        this.resumeClient = resumeClient;
    }

    /**
     * Interview shuru karo (REQ-INT-1, 3, 15). Body mein sirf interviewType, inputMode, targetRole, maxQuestions chahiye.
     * Resume service se student ka latest resume (skills + text) apne aap aata hai; nahi hai toh role ke basis pe questions.
     */
    @PostMapping("/sessions/start")
    public ResponseEntity<InterviewSessionResponse> start(@RequestHeader("X-User-Id") Long userId,
                                                          @Valid @RequestBody InterviewStartRequest request) {
        request.setStudentId(userId);

        boolean noResumeGiven = (request.getResumeText() == null || request.getResumeText().isBlank())
                && (request.getResumeSkills() == null || request.getResumeSkills().isEmpty());
        if (noResumeGiven) {
            Optional<ResumeContext> ctx = resumeClient.fetchLatest(userId);
            if (ctx.isPresent()) {
                ResumeContext c = ctx.get();
                request.setResumeText(c.resumeText());
                request.setResumeSkills(c.skills());
                String role = request.getTargetRole();
                if ((role == null || role.isBlank() || DEFAULT_ROLE.equals(role))
                        && c.targetRole() != null && !c.targetRole().isBlank()) {
                    request.setTargetRole(c.targetRole());
                }
            }
        }
        return new ResponseEntity<>(interviewService.startSession(request), HttpStatus.CREATED);
    }

    /** Current question ka jawab do. Response mein score, feedback, aur agla question (ya final summary). */
    @PostMapping("/sessions/{sessionId}/answers")
    public ResponseEntity<AnswerEvaluationDto> answer(@RequestHeader("X-User-Id") Long userId,
                                                      @PathVariable String sessionId,
                                                      @RequestBody Map<String, String> body) {
        requireOwner(sessionId, userId);
        String answer = body.get("answer");
        if (answer == null || answer.isBlank()) {
            throw new InvalidInterviewSessionException("Answer cannot be empty.");
        }
        if (answer.length() > MAX_ANSWER_CHARS) {
            throw new InvalidInterviewSessionException("Answer is too long (max " + MAX_ANSWER_CHARS + " characters).");
        }
        return ResponseEntity.ok(interviewService.submitAnswer(new InterviewAnswerRequest(sessionId, answer)));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<InterviewSessionResponse> session(@RequestHeader("X-User-Id") Long userId,
                                                            @PathVariable String sessionId) {
        return ResponseEntity.ok(requireOwner(sessionId, userId));
    }

    /** Page refresh / network drop ke baad session wapas (REQ-INT-17). */
    @PostMapping("/sessions/{sessionId}/reconnect")
    public ResponseEntity<InterviewSessionResponse> reconnect(@RequestHeader("X-User-Id") Long userId,
                                                              @PathVariable String sessionId) {
        requireOwner(sessionId, userId);
        return ResponseEntity.ok(interviewService.reconnectSession(sessionId));
    }

    /** Beech mein band karo (REQ-INT-14): session INCOMPLETE mark hota hai. */
    @PostMapping("/sessions/{sessionId}/end")
    public ResponseEntity<InterviewSessionResponse> end(@RequestHeader("X-User-Id") Long userId,
                                                        @PathVariable String sessionId,
                                                        @RequestParam(defaultValue = "true") boolean markAsIncomplete) {
        requireOwner(sessionId, userId);
        return ResponseEntity.ok(interviewService.endSession(sessionId, markAsIncomplete));
    }

    /** Meri saari purani sessions. */
    @GetMapping("/sessions")
    public ResponseEntity<List<InterviewSessionResponse>> mySessions(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(interviewService.getStudentSessions(userId));
    }

    /** Mera aggregated summary (dashboard card ke liye). */
    @GetMapping("/summary")
    public ResponseEntity<InterviewSummaryResponse> mySummary(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(interviewService.getStudentInterviewSummary(userId));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "interview-service");
        health.put("aiConfigured", aiEngine.isAiConfigured());
        health.put("mode", aiEngine.isAiConfigured() ? "LLM_GROQ" : "RULE_BASED_FALLBACK");
        return ResponseEntity.ok(health);
    }

    /** Doosre user ka session = "not found" (exist karta hai ye bhi nahi batate). */
    private InterviewSessionResponse requireOwner(String sessionId, Long userId) {
        InterviewSessionResponse session = interviewService.getSession(sessionId);
        if (session.getStudentId() == null || !session.getStudentId().equals(userId)) {
            throw new ResourceNotFoundException("Interview session not found: " + sessionId);
        }
        return session;
    }
}
