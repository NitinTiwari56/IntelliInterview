package com.interviewiq.coding.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "code_submissions", indexes = {
        @Index(name = "idx_code_sub_user", columnList = "user_id"),
        @Index(name = "idx_code_sub_user_problem", columnList = "user_id, problem_id")
})
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false, length = 20)
    private String language;

    @Column(name = "source_code", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String sourceCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Verdict verdict;

    @Column(name = "passed_tests", nullable = false)
    private int passedTests;

    @Column(name = "total_tests", nullable = false)
    private int totalTests;

    @Column(name = "max_time_ms")
    private Integer maxTimeMs;

    @Column(name = "max_memory_kb")
    private Integer maxMemoryKb;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Problem getProblem() { return problem; }
    public void setProblem(Problem problem) { this.problem = problem; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
    public Verdict getVerdict() { return verdict; }
    public void setVerdict(Verdict verdict) { this.verdict = verdict; }
    public int getPassedTests() { return passedTests; }
    public void setPassedTests(int passedTests) { this.passedTests = passedTests; }
    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }
    public Integer getMaxTimeMs() { return maxTimeMs; }
    public void setMaxTimeMs(Integer maxTimeMs) { this.maxTimeMs = maxTimeMs; }
    public Integer getMaxMemoryKb() { return maxMemoryKb; }
    public void setMaxMemoryKb(Integer maxMemoryKb) { this.maxMemoryKb = maxMemoryKb; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getCreatedAt() { return createdAt; }
}
