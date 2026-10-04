package com.nitin.ResumeService.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resumes", indexes = {
        @Index(name = "idx_resumes_student_id", columnList = "student_id")
})
public class ResumeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resume_id")
    private Long resumeId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "file_path", length = 500, nullable = false)
    private String filePath;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "target_role", length = 150, nullable = false)
    private String targetRole;

    @Column(name = "ats_score", nullable = false)
    private Integer atsScore;

    @Column(name = "format_score")
    private Integer formatScore;

    @Column(name = "experience_score")
    private Integer experienceScore;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "extracted_skills", columnDefinition = "TEXT")
    private List<String> extractedSkills = new ArrayList<>();

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "missing_skills", columnDefinition = "TEXT")
    private List<String> missingSkills = new ArrayList<>();

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "matched_skills", columnDefinition = "TEXT")
    private List<String> matchedSkills = new ArrayList<>();

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "suggestions", columnDefinition = "TEXT")
    private List<String> suggestions = new ArrayList<>();

    @Column(name = "summary_feedback", columnDefinition = "TEXT")
    private String summaryFeedback;

    @Column(name = "analysis_source", length = 50)
    private String analysisSource;

    @Convert(converter = ExtractedResumeDataJsonConverter.class)
    @Column(name = "extracted_data", columnDefinition = "LONGTEXT")
    private com.nitin.ResumeService.dto.extracted.ExtractedResumeData extractedData;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    public ResumeEntity() {
    }

    @PrePersist
    public void prePersist() {
        if (this.uploadedAt == null) {
            this.uploadedAt = Instant.now();
        }
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public Integer getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(Integer atsScore) {
        this.atsScore = atsScore;
    }

    public Integer getFormatScore() {
        return formatScore;
    }

    public void setFormatScore(Integer formatScore) {
        this.formatScore = formatScore;
    }

    public Integer getExperienceScore() {
        return experienceScore;
    }

    public void setExperienceScore(Integer experienceScore) {
        this.experienceScore = experienceScore;
    }

    public List<String> getExtractedSkills() {
        return extractedSkills;
    }

    public void setExtractedSkills(List<String> extractedSkills) {
        this.extractedSkills = extractedSkills != null ? extractedSkills : new ArrayList<>();
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills != null ? missingSkills : new ArrayList<>();
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills != null ? matchedSkills : new ArrayList<>();
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
    }

    public String getSummaryFeedback() {
        return summaryFeedback;
    }

    public void setSummaryFeedback(String summaryFeedback) {
        this.summaryFeedback = summaryFeedback;
    }

    public String getAnalysisSource() {
        return analysisSource;
    }

    public void setAnalysisSource(String analysisSource) {
        this.analysisSource = analysisSource;
    }

    public com.nitin.ResumeService.dto.extracted.ExtractedResumeData getExtractedData() {
        return extractedData;
    }

    public void setExtractedData(com.nitin.ResumeService.dto.extracted.ExtractedResumeData extractedData) {
        this.extractedData = extractedData;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
