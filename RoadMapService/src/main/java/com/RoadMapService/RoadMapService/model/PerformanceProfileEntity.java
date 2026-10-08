package com.RoadMapService.RoadMapService.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "performance_profile", indexes = {
        @Index(name = "idx_perf_profile_student_id", columnList = "student_id", unique = true)
})
public class PerformanceProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @Column(name = "student_id", nullable = false, unique = true)
    private Long studentId;

    @Column(name = "overall_readiness_score")
    private Double overallReadinessScore;

    @Column(name = "resume_score")
    private Double resumeScore;

    @Column(name = "technical_interview_score")
    private Double technicalInterviewScore;

    @Column(name = "hr_interview_score")
    private Double hrInterviewScore;

    @Column(name = "coding_score")
    private Double codingScore;

    @Column(name = "aptitude_score")
    private Double aptitudeScore;

    @Convert(converter = WeakAreaListJsonConverter.class)
    @Column(name = "weak_areas", columnDefinition = "TEXT")
    private List<WeakAreaItem> weakAreas = new ArrayList<>();

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "strengths", columnDefinition = "TEXT")
    private List<String> strengths = new ArrayList<>();

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    public PerformanceProfileEntity() {
    }

    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        this.lastUpdated = Instant.now();
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Double getOverallReadinessScore() {
        return overallReadinessScore;
    }

    public void setOverallReadinessScore(Double overallReadinessScore) {
        this.overallReadinessScore = overallReadinessScore;
    }

    public Double getResumeScore() {
        return resumeScore;
    }

    public void setResumeScore(Double resumeScore) {
        this.resumeScore = resumeScore;
    }

    public Double getTechnicalInterviewScore() {
        return technicalInterviewScore;
    }

    public void setTechnicalInterviewScore(Double technicalInterviewScore) {
        this.technicalInterviewScore = technicalInterviewScore;
    }

    public Double getHrInterviewScore() {
        return hrInterviewScore;
    }

    public void setHrInterviewScore(Double hrInterviewScore) {
        this.hrInterviewScore = hrInterviewScore;
    }

    public Double getCodingScore() {
        return codingScore;
    }

    public void setCodingScore(Double codingScore) {
        this.codingScore = codingScore;
    }

    public Double getAptitudeScore() {
        return aptitudeScore;
    }

    public void setAptitudeScore(Double aptitudeScore) {
        this.aptitudeScore = aptitudeScore;
    }

    public List<WeakAreaItem> getWeakAreas() {
        return weakAreas;
    }

    public void setWeakAreas(List<WeakAreaItem> weakAreas) {
        this.weakAreas = weakAreas != null ? weakAreas : new ArrayList<>();
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths != null ? strengths : new ArrayList<>();
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
