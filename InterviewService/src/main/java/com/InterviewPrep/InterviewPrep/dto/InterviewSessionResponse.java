package com.InterviewPrep.InterviewPrep.dto;

import com.InterviewPrep.InterviewPrep.model.InputMode;
import com.InterviewPrep.InterviewPrep.model.InterviewType;
import com.InterviewPrep.InterviewPrep.model.SessionStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class InterviewSessionResponse {

    private String sessionId;
    private Long studentId;
    private InterviewType interviewType;
    private InputMode inputMode;
    private String targetRole;
    private SessionStatus status;
    private Integer currentQuestionNumber;
    private String currentQuestion;
    private Integer maxQuestions;
    private Integer questionCount;
    private Integer totalScore;
    private Double averageScore;
    private String consolidatedFeedback;
    private List<InterviewQuestionItemDto> questions = new ArrayList<>();
    private Instant startedAt;
    private Instant completedAt;

    public InterviewSessionResponse() {
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

    public InterviewType getInterviewType() {
        return interviewType;
    }

    public void setInterviewType(InterviewType interviewType) {
        this.interviewType = interviewType;
    }

    public InputMode getInputMode() {
        return inputMode;
    }

    public void setInputMode(InputMode inputMode) {
        this.inputMode = inputMode;
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

    public Integer getCurrentQuestionNumber() {
        return currentQuestionNumber;
    }

    public void setCurrentQuestionNumber(Integer currentQuestionNumber) {
        this.currentQuestionNumber = currentQuestionNumber;
    }

    public String getCurrentQuestion() {
        return currentQuestion;
    }

    public void setCurrentQuestion(String currentQuestion) {
        this.currentQuestion = currentQuestion;
    }

    public Integer getMaxQuestions() {
        return maxQuestions;
    }

    public void setMaxQuestions(Integer maxQuestions) {
        this.maxQuestions = maxQuestions;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
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

    public String getConsolidatedFeedback() {
        return consolidatedFeedback;
    }

    public void setConsolidatedFeedback(String consolidatedFeedback) {
        this.consolidatedFeedback = consolidatedFeedback;
    }

    public List<InterviewQuestionItemDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<InterviewQuestionItemDto> questions) {
        this.questions = questions != null ? questions : new ArrayList<>();
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
