package com.InterviewPrep.InterviewPrep.dto;

import com.InterviewPrep.InterviewPrep.model.SessionStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class InterviewSessionSummaryDto {

    private String sessionId;
    private Long studentId;
    private String interviewType;
    private String targetRole;
    private SessionStatus status;
    private Integer totalScore;
    private Double averageScore;
    private Integer questionCount;
    private String consolidatedFeedback;
    private List<String> strengthsSummary = new ArrayList<>();
    private List<String> areasForImprovement = new ArrayList<>();
    private Instant completedAt;

    public InterviewSessionSummaryDto() {
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

    public String getInterviewType() {
        return interviewType;
    }

    public void setInterviewType(String interviewType) {
        this.interviewType = interviewType;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
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

    public String getConsolidatedFeedback() {
        return consolidatedFeedback;
    }

    public void setConsolidatedFeedback(String consolidatedFeedback) {
        this.consolidatedFeedback = consolidatedFeedback;
    }

    public List<String> getStrengthsSummary() {
        return strengthsSummary;
    }

    public void setStrengthsSummary(List<String> strengthsSummary) {
        this.strengthsSummary = strengthsSummary != null ? strengthsSummary : new ArrayList<>();
    }

    public List<String> getAreasForImprovement() {
        return areasForImprovement;
    }

    public void setAreasForImprovement(List<String> areasForImprovement) {
        this.areasForImprovement = areasForImprovement != null ? areasForImprovement : new ArrayList<>();
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
