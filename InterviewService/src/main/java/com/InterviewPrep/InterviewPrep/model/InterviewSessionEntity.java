package com.InterviewPrep.InterviewPrep.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "interview_sessions", indexes = {
        @Index(name = "idx_interview_sessions_student_id", columnList = "student_id"),
        @Index(name = "idx_interview_sessions_status", columnList = "status")
})
public class InterviewSessionEntity {

    @Id
    @Column(name = "session_id", length = 64, nullable = false)
    private String sessionId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_type", length = 30, nullable = false)
    private InterviewType interviewType;

    @Enumerated(EnumType.STRING)
    @Column(name = "input_mode", length = 30, nullable = false)
    private InputMode inputMode;

    @Column(name = "target_role", length = 150)
    private String targetRole;

    @Column(name = "resume_summary", columnDefinition = "TEXT")
    private String resumeSummary;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private SessionStatus status;

    @Column(name = "total_score")
    private Integer totalScore;

    @Column(name = "average_score")
    private Double averageScore;

    @Column(name = "question_count")
    private Integer questionCount = 0;

    @Column(name = "max_questions")
    private Integer maxQuestions = 5;

    @Column(name = "consolidated_feedback", columnDefinition = "TEXT")
    private String consolidatedFeedback;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "last_active_at")
    private Instant lastActiveAt;

    public InterviewSessionEntity() {
    }

    @PrePersist
    public void prePersist() {
        if (this.startedAt == null) {
            this.startedAt = Instant.now();
        }
        if (this.lastActiveAt == null) {
            this.lastActiveAt = this.startedAt;
        }
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public InterviewType getInterviewType() {
        return interviewType;
    }

    public void setInterviewType(InterviewType interviewType) {
        this.interviewType = interviewType;
    }

    public InputMode getInputMode() {
        return inputMode;
    }

    public void setInputMode(InputMode inputMode) {
        this.inputMode = inputMode;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getResumeSummary() {
        return resumeSummary;
    }

    public void setResumeSummary(String resumeSummary) {
        this.resumeSummary = resumeSummary;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public Integer getMaxQuestions() {
        return maxQuestions;
    }

    public void setMaxQuestions(Integer maxQuestions) {
        this.maxQuestions = maxQuestions;
    }

    public String getConsolidatedFeedback() {
        return consolidatedFeedback;
    }

    public void setConsolidatedFeedback(String consolidatedFeedback) {
        this.consolidatedFeedback = consolidatedFeedback;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(Instant lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }
}
