package com.nitin.AptituteQuestionsService.dto;

import java.util.List;

public class QuestionRequest {

    /**
     * Number of questions to generate (default: 20)
     */
    private Integer count = 20;

    /**
     * Category filter: "ALL", "QUANTITATIVE", "LOGICAL_REASONING", "VERBAL_ABILITY", "DATA_INTERPRETATION"
     */
    private String category = "ALL";

    /**
     * Difficulty level: "MIXED", "EASY", "MEDIUM", "HARD"
     */
    private String difficulty = "MIXED";

    /**
     * Specific topics to focus on (e.g. ["Percentages", "Time and Work"])
     */
    private List<String> topics;

    /**
     * Generation source: "AUTO", "AI", "BANK" (default: "AUTO")
     */
    private String source = "AUTO";

    public QuestionRequest() {
    }

    public QuestionRequest(Integer count, String category, String difficulty, List<String> topics, String source) {
        this.count = (count != null && count > 0) ? count : 20;
        this.category = category != null ? category : "ALL";
        this.difficulty = difficulty != null ? difficulty : "MIXED";
        this.topics = topics;
        this.source = source != null ? source : "AUTO";
    }

    public Integer getCount() {
        return (count != null && count > 0) ? count : 20;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
