package com.InterviewPrep.InterviewPrep.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WsMessage {

    private WsMessageType type;
    private String sessionId;
    private Long studentId;
    private Integer questionNumber;
    private String content;           // Used for chunk text, error message, or plain answer
    private Boolean isComplete;       // For chunk streaming
    private String question;
    private String answer;
    private Integer score;
    private String feedback;
    private List<String> strengths;
    private List<String> improvements;
    private Object data;              // Optional payload data (e.g. session summary)

    public WsMessage() {
    }

    public static WsMessage of(WsMessageType type) {
        WsMessage msg = new WsMessage();
        msg.setType(type);
        return msg;
    }

    public static WsMessage error(String sessionId, String errorMessage) {
        WsMessage msg = new WsMessage();
        msg.setType(WsMessageType.ERROR);
        msg.setSessionId(sessionId);
        msg.setContent(errorMessage);
        return msg;
    }

    public static WsMessage questionChunk(String sessionId, int questionNumber, String chunk, boolean isComplete) {
        WsMessage msg = new WsMessage();
        msg.setType(WsMessageType.QUESTION_CHUNK);
        msg.setSessionId(sessionId);
        msg.setQuestionNumber(questionNumber);
        msg.setContent(chunk);
        msg.setIsComplete(isComplete);
        return msg;
    }

    public static WsMessage questionComplete(String sessionId, int questionNumber, String fullQuestion) {
        WsMessage msg = new WsMessage();
        msg.setType(WsMessageType.QUESTION_COMPLETE);
        msg.setSessionId(sessionId);
        msg.setQuestionNumber(questionNumber);
        msg.setQuestion(fullQuestion);
        return msg;
    }

    public static WsMessage evaluation(String sessionId, int questionNumber, int score, String feedback,
                                       List<String> strengths, List<String> improvements) {
        WsMessage msg = new WsMessage();
        msg.setType(WsMessageType.EVALUATION);
        msg.setSessionId(sessionId);
        msg.setQuestionNumber(questionNumber);
        msg.setScore(score);
        msg.setFeedback(feedback);
        msg.setStrengths(strengths);
        msg.setImprovements(improvements);
        return msg;
    }

    public static WsMessage sessionSummary(String sessionId, Object summaryData) {
        WsMessage msg = new WsMessage();
        msg.setType(WsMessageType.SESSION_SUMMARY);
        msg.setSessionId(sessionId);
        msg.setData(summaryData);
        return msg;
    }

    public WsMessageType getType() {
        return type;
    }

    public void setType(WsMessageType type) {
        this.type = type;
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

    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(Integer questionNumber) {
        this.questionNumber = questionNumber;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsComplete() {
        return isComplete;
    }

    public void setIsComplete(Boolean complete) {
        isComplete = complete;
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
        this.strengths = strengths;
    }

    public List<String> getImprovements() {
        return improvements;
    }

    public void setImprovements(List<String> improvements) {
        this.improvements = improvements;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
