package com.nitin.AptituteQuestionsService.dto;

import com.nitin.AptituteQuestionsService.model.Question;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class QuestionBatchResponse {

    private boolean success;
    private String message;
    private int count;
    private String source;
    private String timestamp;
    private Map<String, Integer> categoryDistribution;
    private List<Question> questions;

    public QuestionBatchResponse() {
        this.timestamp = Instant.now().toString();
    }

    public QuestionBatchResponse(boolean success, String message, int count, String source,
                                 Map<String, Integer> categoryDistribution, List<Question> questions) {
        this.success = success;
        this.message = message;
        this.count = count;
        this.source = source;
        this.timestamp = Instant.now().toString();
        this.categoryDistribution = categoryDistribution;
        this.questions = questions;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Integer> getCategoryDistribution() {
        return categoryDistribution;
    }

    public void setCategoryDistribution(Map<String, Integer> categoryDistribution) {
        this.categoryDistribution = categoryDistribution;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
