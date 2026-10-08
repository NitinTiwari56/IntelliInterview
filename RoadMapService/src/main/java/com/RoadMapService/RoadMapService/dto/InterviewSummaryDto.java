package com.RoadMapService.RoadMapService.dto;

public class InterviewSummaryDto {

    private Long studentId;
    private long totalSessions;
    private long completedSessions;
    private Double averageTechnicalScore;
    private Double averageHrScore;
    private Double overallAverageScore;

    public InterviewSummaryDto() {
    }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public long getTotalSessions() { return totalSessions; }
    public void setTotalSessions(long totalSessions) { this.totalSessions = totalSessions; }

    public long getCompletedSessions() { return completedSessions; }
    public void setCompletedSessions(long completedSessions) { this.completedSessions = completedSessions; }

    public Double getAverageTechnicalScore() { return averageTechnicalScore; }
    public void setAverageTechnicalScore(Double averageTechnicalScore) { this.averageTechnicalScore = averageTechnicalScore; }

    public Double getAverageHrScore() { return averageHrScore; }
    public void setAverageHrScore(Double averageHrScore) { this.averageHrScore = averageHrScore; }

    public Double getOverallAverageScore() { return overallAverageScore; }
    public void setOverallAverageScore(Double overallAverageScore) { this.overallAverageScore = overallAverageScore; }
}
