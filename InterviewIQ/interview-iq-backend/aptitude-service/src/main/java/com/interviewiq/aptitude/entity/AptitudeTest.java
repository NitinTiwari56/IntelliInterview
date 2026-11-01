package com.interviewiq.aptitude.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Ek test attempt (ek student ka ek session). */
@Entity
@Table(name = "aptitude_tests", indexes = @Index(name = "idx_aptitude_test_user", columnList = "user_id"))
public class AptitudeTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TestStatus status = TestStatus.IN_PROGRESS;

    /** AI_GENERATED ya QUESTION_BANK (question service ne bataya) */
    @Column(length = 30)
    private String source;

    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;

    @Column(name = "total_marks", nullable = false)
    private int totalMarks;

    private Integer score;

    @Column(name = "time_limit_seconds", nullable = false)
    private int timeLimitSeconds;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    /** started_at + time limit + grace. Iske baad submit reject hota hai. */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    /** true = timer khatam hone pe server ne khud submit kiya (student ne submit nahi dabaya) */
    @Column(name = "auto_submitted", nullable = false)
    private boolean autoSubmitted;

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<AptitudeTestQuestion> questions = new ArrayList<>();

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public TestStatus getStatus() { return status; }
    public void setStatus(TestStatus status) { this.status = status; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public int getTimeLimitSeconds() { return timeLimitSeconds; }
    public void setTimeLimitSeconds(int timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public boolean isAutoSubmitted() { return autoSubmitted; }
    public void setAutoSubmitted(boolean autoSubmitted) { this.autoSubmitted = autoSubmitted; }
    public List<AptitudeTestQuestion> getQuestions() { return questions; }
}
