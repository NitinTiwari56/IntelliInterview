package com.interviewiq.aptitude.entity;

import jakarta.persistence.*;

/** Ek result ka category-wise hisaab (4 rows per test). Weak-area stats isi se banta hai. */
@Entity
@Table(name = "aptitude_result_categories")
public class AptitudeResultCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "result_id", nullable = false)
    private AptitudeResult result;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;

    @Column(name = "correct_count", nullable = false)
    private int correctCount;

    public Long getId() { return id; }
    public AptitudeResult getResult() { return result; }
    public void setResult(AptitudeResult result) { this.result = result; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }
}
