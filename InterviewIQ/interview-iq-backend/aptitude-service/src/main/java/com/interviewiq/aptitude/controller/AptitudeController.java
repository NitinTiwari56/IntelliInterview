package com.interviewiq.aptitude.controller;

import com.interviewiq.aptitude.dto.AptitudeDtos.*;
import com.interviewiq.aptitude.service.AptitudeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** User ki pehchaan gateway ke "X-User-Id" header se aati hai. */
@RestController
@RequestMapping("/aptitude")
public class AptitudeController {

    private final AptitudeService aptitudeService;

    public AptitudeController(AptitudeService aptitudeService) {
        this.aptitudeService = aptitudeService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "aptitude-service");
    }

    /** Test shuru karo (ya chal rahe test ko resume). Pehla question milta hai. */
    @PostMapping("/tests")
    public TestStateResponse start(@RequestHeader("X-User-Id") Long userId) {
        return aptitudeService.start(userId);
    }

    /** Page refresh pe: abhi ka question aur bacha hua time. Test nahi hai toh 404. */
    @GetMapping("/tests/current")
    public TestStateResponse current(@RequestHeader("X-User-Id") Long userId) {
        return aptitudeService.current(userId);
    }

    /** Ek question ka jawab (ya timer 0 pe selectedIndex=null). Response mein agla question ya final result. */
    @PostMapping("/tests/{testId}/answer")
    public TestStateResponse answer(@RequestHeader("X-User-Id") Long userId,
                                    @PathVariable String testId,
                                    @Valid @RequestBody AnswerRequest request) {
        return aptitudeService.answer(userId, testId, request);
    }

    /** Test ke baad kuch der tak: correct answers + explanations ke saath review. */
    @GetMapping("/tests/{testId}/result")
    public ResultResponse review(@RequestHeader("X-User-Id") Long userId, @PathVariable String testId) {
        return aptitudeService.reviewResult(userId, testId);
    }

    @GetMapping("/results")
    public List<HistoryItem> history(@RequestHeader("X-User-Id") Long userId) {
        return aptitudeService.history(userId);
    }

    @GetMapping("/results/{resultId}")
    public ResultResponse savedResult(@RequestHeader("X-User-Id") Long userId, @PathVariable Long resultId) {
        return aptitudeService.savedResult(userId, resultId);
    }

    @GetMapping("/stats")
    public List<CategoryScore> stats(@RequestHeader("X-User-Id") Long userId) {
        return aptitudeService.stats(userId);
    }
}
