package com.InterviewPrep.InterviewPrep.dto;

import com.InterviewPrep.InterviewPrep.model.InputMode;
import com.InterviewPrep.InterviewPrep.model.InterviewType;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class InterviewStartRequest {

    @NotNull(message = "studentId is required")
    private Long studentId;

    private InterviewType interviewType = InterviewType.TECHNICAL;

    private InputMode inputMode = InputMode.TEXT;

    private String targetRole = "Software Development Engineer";

    private String resumeText;

    private List<String> resumeSkills = new ArrayList<>();

    private Integer maxQuestions = 5;

    public InterviewStartRequest() {
    }

    public InterviewStartRequest(Long studentId, InterviewType interviewType, InputMode inputMode, String targetRole) {
        this.studentId = studentId;
        this.interviewType = interviewType;
        this.inputMode = inputMode;
        this.targetRole = targetRole;
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

    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }

    public List<String> getResumeSkills() {
        return resumeSkills;
    }

    public void setResumeSkills(List<String> resumeSkills) {
        this.resumeSkills = resumeSkills != null ? resumeSkills : new ArrayList<>();
    }

    public Integer getMaxQuestions() {
        return maxQuestions;
    }

    public void setMaxQuestions(Integer maxQuestions) {
        this.maxQuestions = maxQuestions != null && maxQuestions > 0 ? maxQuestions : 5;
    }
}
