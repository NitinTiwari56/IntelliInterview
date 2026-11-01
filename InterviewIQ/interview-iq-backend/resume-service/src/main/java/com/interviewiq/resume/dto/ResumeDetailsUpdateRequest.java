package com.interviewiq.resume.dto;

import com.interviewiq.resume.dto.extracted.*;

import java.util.List;

/**
 * Student apni resume ki details edit karta hai. Jo field null hai wo jaisi hai waisi rehti hai (partial update).
 * Jo field bheji (jaise skills ya projects) wo POORI replace hoti hai, isliye frontend poori list bheje.
 * rawText aur ATS scores yahan se kabhi nahi badalte (wo PDF se aaye the).
 */
public record ResumeDetailsUpdateRequest(
        CandidateInfo candidateInfo,
        List<EducationItem> education,
        List<ExperienceItem> experience,
        List<ProjectItem> projects,
        CategorizedSkills skills,
        List<String> certifications,
        String targetRole) {}
