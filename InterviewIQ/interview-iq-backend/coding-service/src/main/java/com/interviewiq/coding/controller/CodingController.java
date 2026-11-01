package com.interviewiq.coding.controller;

import com.interviewiq.coding.dto.CodingDtos.*;
import com.interviewiq.coding.entity.Difficulty;
import com.interviewiq.coding.service.CodingService;
import com.interviewiq.coding.service.ProblemService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** User ki pehchaan gateway ke "X-User-Id" header se aati hai. */
@RestController
@RequestMapping("/coding")
public class CodingController {

    private final CodingService codingService;
    private final ProblemService problemService;

    public CodingController(CodingService codingService, ProblemService problemService) {
        this.codingService = codingService;
        this.problemService = problemService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "coding-service");
    }

    @GetMapping("/languages")
    public List<LanguageInfo> languages() {
        return codingService.languages();
    }

    @GetMapping("/problems")
    public List<ProblemListItem> problems(@RequestHeader("X-User-Id") Long userId,
                                          @RequestParam(required = false) Difficulty difficulty,
                                          @RequestParam(required = false) String topic) {
        return problemService.list(userId, difficulty, topic);
    }

    @GetMapping("/problems/{slug}")
    public ProblemDetail problem(@PathVariable String slug) {
        return problemService.detail(slug);
    }

    @PostMapping("/problems/{slug}/run")
    public RunResponse run(@RequestHeader("X-User-Id") Long userId, @PathVariable String slug,
                           @Valid @RequestBody RunRequest request) {
        return codingService.run(userId, slug, request);
    }

    @PostMapping("/problems/{slug}/submit")
    public SubmitResponse submit(@RequestHeader("X-User-Id") Long userId, @PathVariable String slug,
                                 @Valid @RequestBody SubmitRequest request) {
        return codingService.submit(userId, slug, request);
    }

    @GetMapping("/problems/{slug}/submissions")
    public List<SubmissionItem> problemSubmissions(@RequestHeader("X-User-Id") Long userId,
                                                   @PathVariable String slug) {
        return codingService.mySubmissionsForProblem(userId, slug);
    }

    @GetMapping("/submissions")
    public List<SubmissionItem> submissions(@RequestHeader("X-User-Id") Long userId) {
        return codingService.mySubmissions(userId);
    }

    @GetMapping("/submissions/{id}")
    public SubmissionDetail submission(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        return codingService.submissionDetail(userId, id);
    }

    @GetMapping("/stats")
    public StatsResponse stats(@RequestHeader("X-User-Id") Long userId) {
        return codingService.stats(userId);
    }
}
