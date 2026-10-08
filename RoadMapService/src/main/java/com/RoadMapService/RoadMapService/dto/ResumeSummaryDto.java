package com.RoadMapService.RoadMapService.dto;

import java.util.ArrayList;
import java.util.List;

public class ResumeSummaryDto {

    private Long studentId;
    private Integer atsScore;
    private String targetRole;
    private List<String> extractedSkills = new ArrayList<>();
    private List<String> missingSkills = new ArrayList<>();

    public ResumeSummaryDto() {
    }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Integer getAtsScore() { return atsScore; }
    public void setAtsScore(Integer atsScore) { this.atsScore = atsScore; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public List<String> getExtractedSkills() { return extractedSkills; }
    public void setExtractedSkills(List<String> extractedSkills) { this.extractedSkills = extractedSkills != null ? extractedSkills : new ArrayList<>(); }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills != null ? missingSkills : new ArrayList<>(); }
}
