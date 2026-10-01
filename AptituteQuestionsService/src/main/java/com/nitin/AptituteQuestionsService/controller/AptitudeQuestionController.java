package com.nitin.AptituteQuestionsService.controller;

import com.nitin.AptituteQuestionsService.dto.QuestionBatchResponse;
import com.nitin.AptituteQuestionsService.dto.QuestionRequest;
import com.nitin.AptituteQuestionsService.service.AptitudeQuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/aptitude")
@CrossOrigin(origins = "*")
public class AptitudeQuestionController {

    private final AptitudeQuestionService questionService;

    public AptitudeQuestionController(AptitudeQuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * POST endpoint to generate aptitude questions.
     * Ideal for backend services passing structured JSON requests.
     */
    @PostMapping("/generate")
    public ResponseEntity<QuestionBatchResponse> generateQuestionsPost(@RequestBody(required = false) QuestionRequest request) {
        if (request == null) {
            request = new QuestionRequest();
        }
        QuestionBatchResponse response = questionService.generateQuestions(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET endpoint to generate aptitude questions.
     * Enables quick querying with query parameters (defaults to 20 questions).
     * Example: /api/v1/aptitude/generate?count=20&category=ALL&difficulty=MIXED
     */
    @GetMapping("/generate")
    public ResponseEntity<QuestionBatchResponse> generateQuestionsGet(
            @RequestParam(defaultValue = "20") Integer count,
            @RequestParam(defaultValue = "ALL") String category,
            @RequestParam(defaultValue = "MIXED") String difficulty,
            @RequestParam(required = false) List<String> topics,
            @RequestParam(defaultValue = "AUTO") String source
    ) {
        QuestionRequest request = new QuestionRequest(count, category, difficulty, topics, source);
        QuestionBatchResponse response = questionService.generateQuestions(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to list all supported categories, difficulty levels, and topics.
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getCategories() {
        return ResponseEntity.ok(questionService.getAvailableCategories());
    }

    /**
     * Health and metadata check endpoint for other backend services / API gateways.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        return ResponseEntity.ok(questionService.getServiceHealthAndStats());
    }
}
