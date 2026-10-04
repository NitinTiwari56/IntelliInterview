package com.nitin.ResumeService.service;

import com.nitin.ResumeService.dto.ResumeAnalysisResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RuleBasedAtsAnalyzerServiceTest {

    private RuleBasedAtsAnalyzerService analyzerService;

    @BeforeEach
    void setUp() {
        TargetRoleService targetRoleService = new TargetRoleService();
        analyzerService = new RuleBasedAtsAnalyzerService(targetRoleService);
    }

    @Test
    @DisplayName("Should extract technical skills, match target role, and calculate ATS score")
    void testAnalyzeSdeResume() {
        String sampleResume = """
                Nitin Tiwari
                Email: nitin@example.com | Phone: +91 9876543210
                LinkedIn: linkedin.com/in/nitintiwari | GitHub: github.com/nitintiwari
                
                EDUCATION
                B.Tech in Information Technology - 2026
                Institute of Engineering and Technology (IET)
                
                TECHNICAL SKILLS
                Languages: Java, Python, C++, SQL
                Core Concepts: Data Structures, Algorithms, Object Oriented Programming, DBMS, Operating Systems
                Frameworks & Tools: Spring Boot, Git, Docker, MySQL, REST APIs
                
                EXPERIENCE & INTERNSHIPS
                Software Development Intern - TechCorp (Jun 2025 - Aug 2025)
                - Architected and implemented high-throughput REST APIs using Spring Boot and MySQL.
                - Optimized database queries, reducing query latency by 42% across 50,000+ daily requests.
                - Automated CI/CD pipeline using GitHub Actions and Docker.
                
                PROJECTS
                Interview IQ - AI Placement Preparation Platform
                - Developed microservices architecture using Java and Spring Boot.
                - Built sandboxed execution engine and integrated with external APIs.
                - Scaled system to handle 100+ concurrent students with sub-second response times.
                """;

        ResumeAnalysisResponse response = analyzerService.analyzeResume(sampleResume, "Software Development Engineer (SDE)");

        assertThat(response).isNotNull();
        assertThat(response.getTargetRole()).isEqualTo("Software Development Engineer (SDE)");
        assertThat(response.getAtsScore()).isGreaterThanOrEqualTo(75);
        assertThat(response.getExtractedSkills()).contains("Java", "Python", "SQL", "Spring Boot", "Git", "Docker");
        assertThat(response.getMatchedSkills()).contains("Java", "Data Structures", "Algorithms", "SQL");
        assertThat(response.getSuggestions()).isNotEmpty();
        assertThat(response.getSummaryFeedback()).isNotBlank();
        assertThat(response.getFormatScore()).isGreaterThanOrEqualTo(80);
        assertThat(response.getExperienceScore()).isGreaterThanOrEqualTo(70);

        // Verify comprehensive extractedData
        assertThat(response.getExtractedData()).isNotNull();
        assertThat(response.getExtractedData().getCandidateInfo().getName()).contains("Nitin Tiwari");
        assertThat(response.getExtractedData().getCandidateInfo().getEmail()).isEqualTo("nitin@example.com");
        assertThat(response.getExtractedData().getCandidateInfo().getGithub()).contains("github.com/nitintiwari");
        assertThat(response.getExtractedData().getSkills().getLanguages()).contains("Java", "Python");
        assertThat(response.getExtractedData().getEducation()).isNotEmpty();
        assertThat(response.getExtractedData().getProjects()).isNotEmpty();
        assertThat(response.getExtractedData().getRawText()).isNotBlank();
    }

    @Test
    @DisplayName("Should detect missing core skills when resume lacks role requirements")
    void testDetectMissingSkills() {
        String basicResume = """
                John Doe
                Email: john@example.com
                
                EDUCATION
                Bachelor of Arts
                
                SKILLS
                HTML5, CSS3, Microsoft Word
                
                PROJECTS
                Personal Portfolio Website
                - Created simple static website using HTML and CSS.
                """;

        ResumeAnalysisResponse response = analyzerService.analyzeResume(basicResume, "Backend Engineer");

        assertThat(response).isNotNull();
        assertThat(response.getAtsScore()).isLessThan(60);
        assertThat(response.getMissingSkills()).contains("Java", "Spring Boot", "SQL");
        assertThat(response.getSuggestions()).anyMatch(s -> s.contains("essential skills"));
    }
}
