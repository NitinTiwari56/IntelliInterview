package com.interviewiq.aptitude.entity;

import jakarta.persistence.*;

import java.util.List;

/**
 * Test ka ek question. Correct answer yahin (server pe) rehta hai,
 * student ko test ke dauran kabhi nahi bheja jata.
 */
@Entity
@Table(name = "aptitude_test_questions")
public class AptitudeTestQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_id", nullable = false)
    private AptitudeTest test;

    @Column(name = "question_order", nullable = false)
    private int position;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(length = 100)
    private String topic;

    @Column(length = 20)
    private String difficulty;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Convert(converter = StringListConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT")
    private List<String> options;

    @Column(name = "correct_index", nullable = false)
    private int correctIndex;

    @Column(name = "correct_answer", length = 500)
    private String correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(nullable = false)
    private int marks = 1;

    /** null = student ne skip kiya */
    @Column(name = "selected_index")
    private Integer selectedIndex;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    public Long getId() { return id; }
    public AptitudeTest getTest() { return test; }
    public void setTest(AptitudeTest test) { this.test = test; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public int getCorrectIndex() { return correctIndex; }
    public void setCorrectIndex(int correctIndex) { this.correctIndex = correctIndex; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }
    public Integer getSelectedIndex() { return selectedIndex; }
    public void setSelectedIndex(Integer selectedIndex) { this.selectedIndex = selectedIndex; }
    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
}
