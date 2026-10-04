package com.nitin.ResumeService.dto;

import java.util.ArrayList;
import java.util.List;

public class ResumeHistoryResponse {

    private Long studentId;
    private int totalAnalyses;
    private Double averageAtsScore;
    private Integer latestScore;
    private List<ResumeHistoryItem> history = new ArrayList<>();

    public ResumeHistoryResponse() {
    }

    public ResumeHistoryResponse(Long studentId, int totalAnalyses, Double averageAtsScore,
                                 Integer latestScore, List<ResumeHistoryItem> history) {
        this.studentId = studentId;
        this.totalAnalyses = totalAnalyses;
        this.averageAtsScore = averageAtsScore;
        this.latestScore = latestScore;
        this.history = history != null ? history : new ArrayList<>();
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public int getTotalAnalyses() {
        return totalAnalyses;
    }

    public void setTotalAnalyses(int totalAnalyses) {
        this.totalAnalyses = totalAnalyses;
    }

    public Double getAverageAtsScore() {
        return averageAtsScore;
    }

    public void setAverageAtsScore(Double averageAtsScore) {
        this.averageAtsScore = averageAtsScore;
    }

    public Integer getLatestScore() {
        return latestScore;
    }

    public void setLatestScore(Integer latestScore) {
        this.latestScore = latestScore;
    }

    public List<ResumeHistoryItem> getHistory() {
        return history;
    }

    public void setHistory(List<ResumeHistoryItem> history) {
        this.history = history != null ? history : new ArrayList<>();
    }
}
