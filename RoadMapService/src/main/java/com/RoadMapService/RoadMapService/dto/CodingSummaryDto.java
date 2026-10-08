package com.RoadMapService.RoadMapService.dto;

import java.util.HashMap;
import java.util.Map;

public class CodingSummaryDto {

    private Long studentId;
    private int problemsSolved = 0;
    private int totalSubmissions = 0;
    private Double accuracy = 0.0;
    private Double averageScore = 0.0;
    private Map<String, Double> topicAccuracy = new HashMap<>();

    public CodingSummaryDto() {
    }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public int getProblemsSolved() { return problemsSolved; }
    public void setProblemsSolved(int problemsSolved) { this.problemsSolved = problemsSolved; }

    public int getTotalSubmissions() { return totalSubmissions; }
    public void setTotalSubmissions(int totalSubmissions) { this.totalSubmissions = totalSubmissions; }

    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }

    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }

    public Map<String, Double> getTopicAccuracy() { return topicAccuracy; }
    public void setTopicAccuracy(Map<String, Double> topicAccuracy) { this.topicAccuracy = topicAccuracy != null ? topicAccuracy : new HashMap<>(); }
}
