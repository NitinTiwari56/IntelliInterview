package com.InterviewPrep.InterviewPrep.dto;

import jakarta.validation.constraints.NotBlank;

public class InterviewAnswerRequest {

    @NotBlank(message = "sessionId is required")
    private String sessionId;

    @NotBlank(message = "answer is required")
    private String answer;

    public InterviewAnswerRequest() {
    }

    public InterviewAnswerRequest(String sessionId, String answer) {
        this.sessionId = sessionId;
        this.answer = answer;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
