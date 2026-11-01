package com.interviewiq.coding.dto;

import com.interviewiq.coding.entity.Difficulty;
import com.interviewiq.coding.entity.Verdict;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class CodingDtos {

    private CodingDtos() {}

    // ===================== student: read =====================
    public record LanguageInfo(String key, String label, String starterCode) {}

    /** status: SOLVED | ATTEMPTED | TODO (is user ke liye) */
    public record ProblemListItem(Long id, String slug, String title, Difficulty difficulty,
                                  String topic, String status) {}

    public record SampleCase(String input, String expectedOutput) {}

    public record ProblemDetail(Long id, String slug, String title, String description, Difficulty difficulty,
                                String topic, double timeLimitSeconds, int memoryLimitKb,
                                List<SampleCase> samples) {}

    // ===================== run / submit =====================
    public record RunRequest(
            @NotBlank(message = "language is required") String language,
            @NotBlank(message = "sourceCode is required")
            @Size(max = 50000, message = "sourceCode must be at most 50000 characters") String sourceCode,
            @Size(max = 10000, message = "customInput must be at most 10000 characters") String customInput) {}

    public record SubmitRequest(
            @NotBlank(message = "language is required") String language,
            @NotBlank(message = "sourceCode is required")
            @Size(max = 50000, message = "sourceCode must be at most 50000 characters") String sourceCode) {}

    /** status: PASSED | FAILED | TIME_LIMIT_EXCEEDED | RUNTIME_ERROR | COMPILATION_ERROR | RAN (custom input) */
    public record CaseResult(int caseNumber, String status, String input, String expectedOutput,
                             String actualOutput, String stderr, Integer timeMs) {}

    /** verdict: ACCEPTED/WRONG_ANSWER/... ya custom input run pe COMPLETED */
    public record RunResponse(String verdict, String compileOutput, List<CaseResult> results) {}

    public record FailedCase(String input, String expectedOutput, String actualOutput) {}

    /** failedCase sirf SAMPLE test fail hone pe aata hai; hidden pe failedOnHiddenTest = true. */
    public record SubmitResponse(Long submissionId, Verdict verdict, int passedTests, int totalTests,
                                 Integer maxTimeMs, Integer maxMemoryKb, String message,
                                 FailedCase failedCase, boolean failedOnHiddenTest) {}

    // ===================== submissions / stats =====================
    public record SubmissionItem(Long id, String problemSlug, String problemTitle, String language,
                                 Verdict verdict, int passedTests, int totalTests, Integer maxTimeMs,
                                 Integer maxMemoryKb, Instant createdAt) {}

    public record SubmissionDetail(Long id, String problemSlug, String problemTitle, String language,
                                   Verdict verdict, int passedTests, int totalTests, Integer maxTimeMs,
                                   Integer maxMemoryKb, String message, String sourceCode, Instant createdAt) {}

    public record DifficultyStat(Difficulty difficulty, long solved, long total) {}

    public record TopicStat(String topic, long solved, long attempted) {}

    public record StatsResponse(long totalSubmissions, long acceptedSubmissions, double acceptanceRate,
                                long problemsAttempted, long problemsSolved,
                                List<DifficultyStat> byDifficulty, List<TopicStat> byTopic) {}

    // ===================== admin =====================
    public record TestCaseRequest(
            @NotNull(message = "input is required") String input,
            @NotNull(message = "expectedOutput is required") String expectedOutput,
            boolean sample) {}

    public record ProblemUpsertRequest(
            @NotBlank(message = "slug is required")
            @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "slug must be lowercase letters, digits and hyphens")
            @Size(max = 120) String slug,
            @NotBlank(message = "title is required") @Size(max = 200) String title,
            @NotBlank(message = "description is required") String description,
            @NotNull(message = "difficulty is required") Difficulty difficulty,
            @NotBlank(message = "topic is required") @Size(max = 60) String topic,
            @DecimalMin(value = "0.5", message = "timeLimitSeconds must be at least 0.5")
            @DecimalMax(value = "15", message = "timeLimitSeconds must be at most 15") Double timeLimitSeconds,
            @Min(value = 16000, message = "memoryLimitKb must be at least 16000")
            @Max(value = 512000, message = "memoryLimitKb must be at most 512000") Integer memoryLimitKb,
            Boolean published,
            @NotEmpty(message = "at least one test case is required")
            @Size(max = 20, message = "at most 20 test cases allowed") List<@Valid TestCaseRequest> testCases) {}

    public record AdminTestCase(Long id, int position, String input, String expectedOutput, boolean sample) {}

    public record AdminProblemResponse(Long id, String slug, String title, String description,
                                       Difficulty difficulty, String topic, double timeLimitSeconds,
                                       int memoryLimitKb, boolean published, List<AdminTestCase> testCases) {}

    public record AdminProblemSummary(Long id, String slug, String title, Difficulty difficulty, String topic,
                                      boolean published, int testCaseCount) {}

    /** Bulk import: file/sheet se aaye saare problems ek saath. Slug pehle se hai toh update, nahi toh create. */
    public record ImportRequest(
            @NotEmpty(message = "problems list is required")
            @Size(max = 100, message = "at most 100 problems per import")
            List<@Valid ProblemUpsertRequest> problems) {}

    public record ImportResponse(int created, int updated, List<String> createdSlugs, List<String> updatedSlugs) {}

    // ===================== internal (analytics-service) =====================
    /** averageScore = coding readiness (0-100): 70% difficulty-weighted problems solved + 30% acceptance rate. */
    public record CodingSummary(Long studentId, int problemsSolved, int totalSubmissions, double accuracy,
                                double averageScore, Map<String, Double> topicAccuracy) {}
}
