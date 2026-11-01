package com.interviewiq.coding.entity;

import jakarta.persistence.*;

/** Sample test cases student ko dikhte hain (Run ke liye). Hidden wale sirf Submit mein use hote hain. */
@Entity
@Table(name = "problem_test_cases")
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "case_order", nullable = false)
    private int position;

    @Column(name = "input_data", nullable = false, columnDefinition = "TEXT")
    private String input;

    @Column(name = "expected_output", nullable = false, columnDefinition = "TEXT")
    private String expectedOutput;

    @Column(name = "is_sample", nullable = false)
    private boolean sample;

    public Long getId() { return id; }
    public Problem getProblem() { return problem; }
    public void setProblem(Problem problem) { this.problem = problem; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
    public boolean isSample() { return sample; }
    public void setSample(boolean sample) { this.sample = sample; }
}
