package com.interviewiq.aptitude.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public final class AptitudeDtos {

    private AptitudeDtos() {}

    /** Ek question (correct answer NAHI hai). index 1 se shuru. */
    public record QuestionView(int index, int total, String category, String topic, String question,
                               List<String> options, int marks, int remainingSeconds, int secondsPerQuestion) {}

    public record CategoryScore(String category, long total, long correct, double accuracy) {}

    public record ReviewItem(int position, String category, String topic, String question, List<String> options,
                             Integer selectedIndex, int correctIndex, boolean correct, String explanation) {}

    /** review sirf test ke turant baad (kuch der tak) milta hai; DB se aaye result mein null hota hai. */
    public record ResultResponse(Long resultId, int score, int totalMarks, double percentage, int totalQuestions,
                                 int correctCount, int incorrectCount, int skippedCount, boolean autoEnded,
                                 Instant submittedAt, List<CategoryScore> categoryBreakdown,
                                 List<ReviewItem> review) {}

    /** status: IN_PROGRESS (question hai) ya COMPLETED (result hai). */
    public record TestStateResponse(String testId, String status, QuestionView question,
                                    boolean previousTimedOut, ResultResponse result) {}

    /** selectedIndex: 0-3 ya null (skip / kuch select nahi kiya). questionIndex: 1 se shuru. */
    public record AnswerRequest(@NotNull(message = "questionIndex is required") Integer questionIndex,
                                Integer selectedIndex) {}

    public record HistoryItem(Long resultId, int score, int totalMarks, double percentage, Instant submittedAt) {}

    // ---- internal (analytics-service) ----
    /** Scores 0-100. Quant mein DATA_INTERPRETATION bhi judta hai. Kisi bucket ka data na ho toh null. */
    public record AptitudeSummary(Long studentId, int totalAttempted, double overallScore,
                                  Double quantScore, Double logicalScore, Double verbalScore) {}
}
