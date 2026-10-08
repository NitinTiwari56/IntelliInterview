package com.InterviewPrep.InterviewPrep.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class InterviewCompletedEvent {

    private String eventType = "InterviewCompleted";
    private String sessionId;
    private Long studentId;
    private String interviewType;
    private String targetRole;
    private Integer totalScore;
    private Double averageScore;
    private Integer questionCount;
    private List<String> identifiedWeakAreas = new ArrayList<>();
    private List<String> identifiedStrengths = new ArrayList<>();
    private Instant timestamp;

    public InterviewCompletedEvent() {
        this.timestamp = Instant.now();
    }

    public InterviewCompletedEvent(String sessionId, Long studentId, String interviewType, String targetRole,
                                   Integer totalScore, Double averageScore, Integer questionCount) {
        this.eventType = "InterviewCompleted";
        this.sessionId = sessionId;
        this.studentId = studentId;
        this.interviewType = interviewType;
        this.targetRole = targetRole;
        this.totalScore = totalScore;
        this.averageScore = averageScore;
        this.questionCount = questionCount;
        this.timestamp = Instant.now();
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
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

    public List<String> getIdentifiedWeakAreas() {
        return identifiedWeakAreas;
    }

    public void setIdentifiedWeakAreas(List<String> identifiedWeakAreas) {
        this.identifiedWeakAreas = identifiedWeakAreas != null ? identifiedWeakAreas : new ArrayList<>();
    }

    public List<String> getIdentifiedStrengths() {
        return identifiedStrengths;
    }

    public void setIdentifiedStrengths(List<String> identifiedStrengths) {
        this.identifiedStrengths = identifiedStrengths != null ? identifiedStrengths : new ArrayList<>();
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
