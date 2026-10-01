package com.nitin.AptituteQuestionsService.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private QuestionCategory category;
    private String topic;
    private DifficultyLevel difficulty;
    private String question;
    private List<String> options;
    private int correctOptionIndex;
    private String correctAnswer;
    private String explanation;
    private int marks = 1;

    public Question() {
        this.options = new ArrayList<>();
    }

    public Question(String id, QuestionCategory category, String topic, DifficultyLevel difficulty,
                    String question, List<String> options, int correctOptionIndex,
                    String correctAnswer, String explanation, int marks) {
        this.id = id;
        this.category = category;
        this.topic = topic;
        this.difficulty = difficulty;
        this.question = question;
        this.options = options != null ? options : new ArrayList<>();
        this.correctOptionIndex = correctOptionIndex;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.marks = marks;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public QuestionCategory getCategory() {
        return category;
    }

    public void setCategory(QuestionCategory category) {
        this.category = category;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public int getCorrectOptionIndex() {
        return correctOptionIndex;
    }

    public void setCorrectOptionIndex(int correctOptionIndex) {
        this.correctOptionIndex = correctOptionIndex;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public int getMarks() {
        return marks;
    }

    public void setMarks(int marks) {
        this.marks = marks;
    }
}
