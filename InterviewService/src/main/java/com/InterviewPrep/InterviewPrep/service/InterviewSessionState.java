package com.InterviewPrep.InterviewPrep.service;

import com.InterviewPrep.InterviewPrep.model.InputMode;
import com.InterviewPrep.InterviewPrep.model.InterviewType;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * REQ-INT-2: Keeps session state (current question number and conversation history).
 * REQ-INT-17: Supports reconnect within session timeout window.
 */
public class InterviewSessionState {

    private final String sessionId;
    private final Long studentId;
    private final InterviewType interviewType;
    private final InputMode inputMode;
    private final String targetRole;
    private final String resumeSummary;
    private final List<String> resumeSkills;
    private final int maxQuestions;

    private int currentQuestionNumber = 0;
    private String currentQuestion;
    private Instant lastActiveAt;
    private boolean completed = false;

    private final List<QnaRecord> conversationHistory = Collections.synchronizedList(new ArrayList<>());
    private volatile WebSocketSession webSocketSession;

    public static class QnaRecord {
        private final int questionNumber;
        private final String question;
        private String answer;
        private Integer score;
        private String feedback;
        private List<String> strengths = new ArrayList<>();
        private List<String> improvements = new ArrayList<>();

        public QnaRecord(int questionNumber, String question) {
            this.questionNumber = questionNumber;
            this.question = question;
        }

        public int getQuestionNumber() { return questionNumber; }
        public String getQuestion() { return question; }
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }
        public List<String> getStrengths() { return strengths; }
        public void setStrengths(List<String> strengths) { this.strengths = strengths != null ? strengths : new ArrayList<>(); }
        public List<String> getImprovements() { return improvements; }
        public void setImprovements(List<String> improvements) { this.improvements = improvements != null ? improvements : new ArrayList<>(); }
    }

    public InterviewSessionState(String sessionId, Long studentId, InterviewType interviewType,
                                 InputMode inputMode, String targetRole, String resumeSummary,
                                 List<String> resumeSkills, int maxQuestions) {
        this.sessionId = sessionId;
        this.studentId = studentId;
        this.interviewType = interviewType != null ? interviewType : InterviewType.TECHNICAL;
        this.inputMode = inputMode != null ? inputMode : InputMode.TEXT;
        this.targetRole = targetRole != null && !targetRole.isBlank() ? targetRole : "Software Development Engineer";
        this.resumeSummary = resumeSummary;
        this.resumeSkills = resumeSkills != null ? resumeSkills : new ArrayList<>();
        this.maxQuestions = maxQuestions > 0 ? maxQuestions : 5;
        this.lastActiveAt = Instant.now();
    }

    public void touch() {
        this.lastActiveAt = Instant.now();
    }

    public String getSessionId() { return sessionId; }
    public Long getStudentId() { return studentId; }
    public InterviewType getInterviewType() { return interviewType; }
    public InputMode getInputMode() { return inputMode; }
    public String getTargetRole() { return targetRole; }
    public String getResumeSummary() { return resumeSummary; }
    public List<String> getResumeSkills() { return resumeSkills; }
    public int getMaxQuestions() { return maxQuestions; }

    public int getCurrentQuestionNumber() { return currentQuestionNumber; }
    public void setCurrentQuestionNumber(int currentQuestionNumber) { this.currentQuestionNumber = currentQuestionNumber; }

    public String getCurrentQuestion() { return currentQuestion; }
    public void setCurrentQuestion(String currentQuestion) { this.currentQuestion = currentQuestion; }

    public Instant getLastActiveAt() { return lastActiveAt; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public List<QnaRecord> getConversationHistory() { return conversationHistory; }

    public void recordAnswer(int qNum, String answer, int score, String feedback, List<String> strengths, List<String> improvements) {
        synchronized (conversationHistory) {
            for (QnaRecord r : conversationHistory) {
                if (r.getQuestionNumber() == qNum) {
                    r.setAnswer(answer);
                    r.setScore(score);
                    r.setFeedback(feedback);
                    r.setStrengths(strengths);
                    r.setImprovements(improvements);
                    touch();
                    return;
                }
            }
            QnaRecord rec = new QnaRecord(qNum, this.currentQuestion);
            rec.setAnswer(answer);
            rec.setScore(score);
            rec.setFeedback(feedback);
            rec.setStrengths(strengths);
            rec.setImprovements(improvements);
            conversationHistory.add(rec);
            touch();
        }
    }

    public WebSocketSession getWebSocketSession() { return webSocketSession; }
    public void setWebSocketSession(WebSocketSession webSocketSession) { this.webSocketSession = webSocketSession; }
}
