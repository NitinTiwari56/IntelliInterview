package com.nitin.ResumeService.dto.extracted;

import java.util.ArrayList;
import java.util.List;

public class ExtractedResumeData {

    private CandidateInfo candidateInfo = new CandidateInfo();
    private List<EducationItem> education = new ArrayList<>();
    private List<ExperienceItem> experience = new ArrayList<>();
    private List<ProjectItem> projects = new ArrayList<>();
    private CategorizedSkills skills = new CategorizedSkills();
    private List<String> certifications = new ArrayList<>();
    private String rawText;

    public ExtractedResumeData() {
    }

    public CandidateInfo getCandidateInfo() {
        return candidateInfo;
    }

    public void setCandidateInfo(CandidateInfo candidateInfo) {
        this.candidateInfo = candidateInfo != null ? candidateInfo : new CandidateInfo();
    }

    public List<EducationItem> getEducation() {
        return education;
    }

    public void setEducation(List<EducationItem> education) {
        this.education = education != null ? education : new ArrayList<>();
    }

    public List<ExperienceItem> getExperience() {
        return experience;
    }

    public void setExperience(List<ExperienceItem> experience) {
        this.experience = experience != null ? experience : new ArrayList<>();
    }

    public List<ProjectItem> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectItem> projects) {
        this.projects = projects != null ? projects : new ArrayList<>();
    }

    public CategorizedSkills getSkills() {
        return skills;
    }

    public void setSkills(CategorizedSkills skills) {
        this.skills = skills != null ? skills : new CategorizedSkills();
    }

    public List<String> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<String> certifications) {
        this.certifications = certifications != null ? certifications : new ArrayList<>();
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }
}
