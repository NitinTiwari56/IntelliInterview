package com.interviewiq.coding.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "problems")
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** URL mein use hota hai: /coding/problems/two-sum-indices */
    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(nullable = false, length = 200)
    private String title;

    /** Markdown: statement, input/output format, constraints, examples */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Difficulty difficulty;

    @Column(nullable = false, length = 60)
    private String topic;

    @Column(name = "time_limit_seconds", nullable = false)
    private double timeLimitSeconds = 2.0;

    @Column(name = "memory_limit_kb", nullable = false)
    private int memoryLimitKb = 256000;

    @Column(nullable = false)
    private boolean published = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<TestCase> testCases = new ArrayList<>();

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public double getTimeLimitSeconds() { return timeLimitSeconds; }
    public void setTimeLimitSeconds(double timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }
    public int getMemoryLimitKb() { return memoryLimitKb; }
    public void setMemoryLimitKb(int memoryLimitKb) { this.memoryLimitKb = memoryLimitKb; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
    public Instant getCreatedAt() { return createdAt; }
    public List<TestCase> getTestCases() { return testCases; }
}
