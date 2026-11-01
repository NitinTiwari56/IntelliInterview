package com.interviewiq.aptitude.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Sirf FINAL result save hota hai (chhota sa record, ~200 bytes). Questions/options DB mein nahi jaate,
 * wo test ke dauran memory mein rehte hain.
 */
@Entity
@Table(name = "aptitude_results", indexes = @Index(name = "idx_aptitude_result_user", columnList = "user_id"))
public class AptitudeResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int score;

    @Column(name = "total_marks", nullable = false)
    private int totalMarks;

    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;

    @Column(name = "correct_count", nullable = false)
    private int correctCount;

    @Column(name = "skipped_count", nullable = false)
    private int skippedCount;

    /** true = student beech mein chhod gaya, server ne test band kiya (answers jitne diye the utne count hue) */
    @Column(name = "auto_ended", nullable = false)
    private boolean autoEnded;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @OneToMany(mappedBy = "result", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AptitudeResultCategory> categories = new ArrayList<>();

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }
    public int getSkippedCount() { return skippedCount; }
    public void setSkippedCount(int skippedCount) { this.skippedCount = skippedCount; }
    public boolean isAutoEnded() { return autoEnded; }
    public void setAutoEnded(boolean autoEnded) { this.autoEnded = autoEnded; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public List<AptitudeResultCategory> getCategories() { return categories; }
}
