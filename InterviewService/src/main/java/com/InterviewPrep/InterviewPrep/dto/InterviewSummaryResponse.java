package com.InterviewPrep.InterviewPrep.dto;

import java.util.ArrayList;
import java.util.List;

public class InterviewSummaryResponse {

    private Long studentId;
    private long totalSessions;
    private long completedSessions;
    private long totalQuestionsAnswered;
    private Double averageTechnicalScore;
    private Double averageHrScore;
    private Double overallAverageScore;
    private List<InterviewSessionSummaryDto> recentSessions = new ArrayList<>();

    public InterviewSummaryResponse() {
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public long getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(long totalSessions) {
        this.totalSessions = totalSessions;
    }

    public long getCompletedSessions() {
        return completedSessions;
    }

    public void setCompletedSessions(long completedSessions) {
        this.completedSessions = completedSessions;
    }

    public long getTotalQuestionsAnswered() {
        return totalQuestionsAnswered;
    }

    public void setTotalQuestionsAnswered(long totalQuestionsAnswered) {
        this.totalQuestionsAnswered = totalQuestionsAnswered;
    }

    public Double getAverageTechnicalScore() {
        return averageTechnicalScore;
    }

    public void setAverageTechnicalScore(Double averageTechnicalScore) {
        this.averageTechnicalScore = averageTechnicalScore;
    }

    public Double getAverageHrScore() {
        return averageHrScore;
    }

    public void setAverageHrScore(Double averageHrScore) {
        this.averageHrScore = averageHrScore;
    }

    public Double getOverallAverageScore() {
        return overallAverageScore;
    }

    public void setOverallAverageScore(Double overallAverageScore) {
        this.overallAverageScore = overallAverageScore;
    }

    public List<InterviewSessionSummaryDto> getRecentSessions() {
        return recentSessions;
    }

    public void setRecentSessions(List<InterviewSessionSummaryDto> recentSessions) {
        this.recentSessions = recentSessions != null ? recentSessions : new ArrayList<>();
    }
}
