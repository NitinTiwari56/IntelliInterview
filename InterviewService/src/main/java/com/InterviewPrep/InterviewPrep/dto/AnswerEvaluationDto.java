package com.InterviewPrep.InterviewPrep.dto;

import java.util.ArrayList;
import java.util.List;

public class AnswerEvaluationDto {

    private String sessionId;
    private Integer questionNumber;
    private String question;
    private String answer;
    private Integer score;
    private String feedback;
    private List<String> strengths = new ArrayList<>();
    private List<String> improvements = new ArrayList<>();
    private boolean completed;
    private Integer nextQuestionNumber;
    private String nextQuestion;
    private InterviewSessionSummaryDto sessionSummary;

    public AnswerEvaluationDto() {
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(Integer questionNumber) {
        this.questionNumber = questionNumber;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths != null ? strengths : new ArrayList<>();
    }

    public List<String> getImprovements() {
        return improvements;
    }

    public void setImprovements(List<String> improvements) {
        this.improvements = improvements != null ? improvements : new ArrayList<>();
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Integer getNextQuestionNumber() {
        return nextQuestionNumber;
    }

    public void setNextQuestionNumber(Integer nextQuestionNumber) {
        this.nextQuestionNumber = nextQuestionNumber;
    }

    public String getNextQuestion() {
        return nextQuestion;
    }

    public void setNextQuestion(String nextQuestion) {
        this.nextQuestion = nextQuestion;
    }

    public InterviewSessionSummaryDto getSessionSummary() {
        return sessionSummary;
    }

    public void setSessionSummary(InterviewSessionSummaryDto sessionSummary) {
        this.sessionSummary = sessionSummary;
    }
}
