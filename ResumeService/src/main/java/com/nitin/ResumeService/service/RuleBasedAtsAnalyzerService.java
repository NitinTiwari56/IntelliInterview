package com.nitin.ResumeService.service;

import com.nitin.ResumeService.dto.ResumeAnalysisResponse;
import com.nitin.ResumeService.model.RoleSkillProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class RuleBasedAtsAnalyzerService {

    private static final Logger log = LoggerFactory.getLogger(RuleBasedAtsAnalyzerService.class);

    private final TargetRoleService targetRoleService;

    // Common technical skills library for universal extraction
    private static final List<String> ALL_TECH_SKILLS = List.of(
            "Java", "Python", "C++", "C#", "C", "JavaScript", "TypeScript", "Kotlin", "Swift", "Go", "Rust", "PHP", "Ruby", "Scala",
            "Data Structures", "Algorithms", "Object Oriented Programming", "OOP", "DBMS", "Operating Systems", "Computer Networks",
            "SQL", "MySQL", "PostgreSQL", "MongoDB", "Redis", "Cassandra", "Oracle", "SQLite",
            "Spring Boot", "Spring", "Hibernate", "JPA", "Node.js", "Express", "React", "React.js", "Next.js", "Angular", "Vue.js",
            "HTML5", "CSS3", "Tailwind CSS", "Bootstrap", "Redux", "REST", "RESTful APIs", "GraphQL", "WebSockets", "Microservices",
            "Docker", "Kubernetes", "AWS", "Azure", "GCP", "Linux", "Git", "GitHub", "GitLab", "CI/CD", "Jenkins", "GitHub Actions",
            "Kafka", "RabbitMQ", "Pandas", "NumPy", "Scikit-Learn", "TensorFlow", "PyTorch", "OpenCV", "NLP",
            "Machine Learning", "Deep Learning", "Unit Testing", "JUnit", "Selenium", "Postman", "Maven", "Gradle"
    );

    // Strong resume action verbs
    private static final List<String> ACTION_VERBS = List.of(
            "developed", "built", "implemented", "designed", "architected", "optimized",
            "reduced", "increased", "created", "engineered", "scaled", "migrated",
            "automated", "led", "delivered", "deployed", "integrated", "spearheaded"
    );

    public RuleBasedAtsAnalyzerService(TargetRoleService targetRoleService) {
        this.targetRoleService = targetRoleService;
    }

    public ResumeAnalysisResponse analyzeResume(String resumeText, String targetRoleName) {
        log.info("Running Rule-Based ATS Analyzer for target role: '{}'", targetRoleName);

        RoleSkillProfile roleProfile = targetRoleService.getRoleProfile(targetRoleName);
        String actualRoleName = roleProfile.getRoleName();

        // 1. Extract Skills
        Set<String> extractedSkills = extractSkillsFromText(resumeText);

        // 2. Compute Match against Role Profile
        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (String coreSkill : roleProfile.getCoreSkills()) {
            if (isSkillPresent(resumeText, extractedSkills, coreSkill)) {
                matchedSkills.add(coreSkill);
            } else {
                missingSkills.add(coreSkill);
            }
        }

        // Secondary skills check
        for (String secSkill : roleProfile.getSecondarySkills()) {
            if (isSkillPresent(resumeText, extractedSkills, secSkill)) {
                if (!matchedSkills.contains(secSkill)) {
                    matchedSkills.add(secSkill);
                }
            } else if (missingSkills.size() < 6 && !missingSkills.contains(secSkill)) {
                missingSkills.add(secSkill);
            }
        }

        // 3. Section and Formatting Score (0-100)
        int formatScore = calculateFormatScore(resumeText);

        // 4. Experience and Action Verbs / Metrics Score (0-100)
        int experienceScore = calculateExperienceScore(resumeText);

        // 5. Skill Match Percentage
        double totalCore = Math.max(1, roleProfile.getCoreSkills().size());
        double coreMatches = roleProfile.getCoreSkills().stream().filter(s -> isSkillPresent(resumeText, extractedSkills, s)).count();
        double skillScore = (coreMatches / totalCore) * 100.0;

        // 6. Overall ATS Weighted Score (0-100)
        // 50% Skill Match, 25% Format & Structure, 25% Experience & Measurable Impact
        int atsScore = (int) Math.round((skillScore * 0.50) + (formatScore * 0.25) + (experienceScore * 0.25));
        atsScore = Math.min(98, Math.max(20, atsScore));

        // 7. Generate Targeted Improvement Suggestions
        List<String> suggestions = generateSuggestions(missingSkills, formatScore, experienceScore, actualRoleName);

        // 8. Generate Summary Feedback
        String summaryFeedback = generateSummary(atsScore, actualRoleName, matchedSkills, missingSkills);

        // 9. Extract Complete Structured Resume Data (Candidate info, education, experience, projects, skills, raw text)
        com.nitin.ResumeService.dto.extracted.ExtractedResumeData extractedData = extractComprehensiveResumeData(resumeText, extractedSkills);

        ResumeAnalysisResponse response = new ResumeAnalysisResponse();
        response.setTargetRole(actualRoleName);
        response.setAtsScore(atsScore);
        response.setFormatScore(formatScore);
        response.setExperienceScore(experienceScore);
        response.setExtractedSkills(new ArrayList<>(extractedSkills));
        response.setMatchedSkills(matchedSkills);
        response.setMissingSkills(missingSkills);
        response.setSuggestions(suggestions);
        response.setSummaryFeedback(summaryFeedback);
        response.setAnalysisSource("RULE_BASED_ATS");
        response.setExtractedData(extractedData);

        return response;
    }

    public com.nitin.ResumeService.dto.extracted.ExtractedResumeData extractComprehensiveResumeData(String text, Set<String> allSkills) {
        com.nitin.ResumeService.dto.extracted.ExtractedResumeData data = new com.nitin.ResumeService.dto.extracted.ExtractedResumeData();
        data.setRawText(text);

        String[] rawLines = text.split("\\r?\\n");
        List<String> lines = new ArrayList<>();
        for (String l : rawLines) {
            String trimmed = l.trim();
            if (!trimmed.isEmpty()) {
                lines.add(trimmed);
            }
        }

        // 1. Candidate Info
        com.nitin.ResumeService.dto.extracted.CandidateInfo info = new com.nitin.ResumeService.dto.extracted.CandidateInfo();
        if (!lines.isEmpty()) {
            String firstLine = lines.get(0).replaceAll("[|•●,].*", "").trim();
            info.setName(firstLine);
        }

        // Email regex
        java.util.regex.Matcher emailMatcher = java.util.regex.Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}").matcher(text);
        if (emailMatcher.find()) {
            info.setEmail(emailMatcher.group());
        }

        // Phone regex
        java.util.regex.Matcher phoneMatcher = java.util.regex.Pattern.compile("(\\+?\\d{1,3}[- ]?)?\\(?\\d{3,5}\\)?[- ]?\\d{3,5}[- ]?\\d{3,5}").matcher(text);
        if (phoneMatcher.find()) {
            info.setPhone(phoneMatcher.group());
        }

        // Links / Profiles
        if (text.toLowerCase().contains("github.com/")) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("github\\.com/[a-zA-Z0-9_-]+", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(text);
            if (m.find()) info.setGithub("https://" + m.group());
        } else if (text.toLowerCase().contains("github")) {
            info.setGithub("GitHub Profile");
        }

        if (text.toLowerCase().contains("linkedin.com/in/")) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("linkedin\\.com/in/[a-zA-Z0-9_-]+", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(text);
            if (m.find()) info.setLinkedin("https://" + m.group());
        } else if (text.toLowerCase().contains("linkedin")) {
            info.setLinkedin("LinkedIn Profile");
        }

        data.setCandidateInfo(info);

        // 2. Categorized Skills
        com.nitin.ResumeService.dto.extracted.CategorizedSkills catSkills = new com.nitin.ResumeService.dto.extracted.CategorizedSkills();
        for (String skill : allSkills) {
            String sLower = skill.toLowerCase();
            if (sLower.equals("java") || sLower.equals("python") || sLower.equals("c++") || sLower.equals("c") || sLower.equals("javascript") || sLower.equals("typescript") || sLower.equals("kotlin") || sLower.equals("swift") || sLower.equals("go") || sLower.equals("rust") || sLower.equals("sql")) {
                catSkills.getLanguages().add(skill);
            } else if (sLower.contains("spring") || sLower.contains("react") || sLower.contains("express") || sLower.contains("node") || sLower.contains("next") || sLower.contains("angular") || sLower.contains("django") || sLower.contains("flask")) {
                catSkills.getFrameworks().add(skill);
            } else if (sLower.contains("mysql") || sLower.contains("postgres") || sLower.contains("mongodb") || sLower.contains("redis") || sLower.contains("docker") || sLower.contains("kubernetes") || sLower.contains("git") || sLower.contains("aws") || sLower.contains("linux") || sLower.contains("kafka")) {
                catSkills.getToolsAndDatabases().add(skill);
            } else {
                catSkills.getCoreSubjects().add(skill);
            }
        }
        data.setSkills(catSkills);

        // 3. Segment Document into Logical Sections
        Map<String, List<String>> sections = new LinkedHashMap<>();
        String currentSection = "HEADER";
        sections.put(currentSection, new ArrayList<>());

        for (String line : lines) {
            String norm = line.toUpperCase().replaceAll("[^A-Z ]", "").trim();

            if (norm.equals("EDUCATION") || norm.equals("ACADEMIC BACKGROUND") || norm.startsWith("EDUCATION")) {
                currentSection = "EDUCATION";
                sections.putIfAbsent(currentSection, new ArrayList<>());
            } else if (norm.equals("PROJECTS") || norm.equals("ACADEMIC PROJECTS") || norm.equals("KEY PROJECTS") || norm.startsWith("PROJECTS")) {
                currentSection = "PROJECTS";
                sections.putIfAbsent(currentSection, new ArrayList<>());
            } else if (norm.equals("EXPERIENCE") || norm.equals("WORK EXPERIENCE") || norm.equals("INTERNSHIPS") || norm.startsWith("EXPERIENCE")) {
                currentSection = "EXPERIENCE";
                sections.putIfAbsent(currentSection, new ArrayList<>());
            } else if (norm.contains("CERTIFICATION") || norm.contains("ACHIEVEMENT")) {
                currentSection = "CERTIFICATIONS";
                sections.putIfAbsent(currentSection, new ArrayList<>());
            } else if (norm.contains("SKILLS") || norm.contains("TECHNICAL SKILLS")) {
                currentSection = "SKILLS";
                sections.putIfAbsent(currentSection, new ArrayList<>());
            } else if (norm.contains("SUMMARY") || norm.contains("OBJECTIVE")) {
                currentSection = "SUMMARY";
                sections.putIfAbsent(currentSection, new ArrayList<>());
            } else {
                sections.computeIfAbsent(currentSection, k -> new ArrayList<>()).add(line);
            }
        }

        // 4. Parse EDUCATION Section
        List<String> eduLines = sections.getOrDefault("EDUCATION", Collections.emptyList());
        for (String l : eduLines) {
            String[] parts = l.split("[—–\\-]");
            com.nitin.ResumeService.dto.extracted.EducationItem edu = new com.nitin.ResumeService.dto.extracted.EducationItem();
            if (parts.length >= 2) {
                edu.setInstitution(parts[0].trim());
                String details = parts[1].trim();
                String[] subParts = details.split("\\|");
                if (subParts.length >= 1) edu.setDegree(subParts[0].trim());
                if (subParts.length >= 2) edu.setYear(subParts[1].trim());
                if (subParts.length >= 3) edu.setScore(subParts[2].trim());
            } else if (l.contains("|")) {
                String[] pipeParts = l.split("\\|");
                edu.setInstitution(pipeParts[0].trim());
                if (pipeParts.length > 1) edu.setDegree(pipeParts[1].trim());
                if (pipeParts.length > 2) edu.setScore(pipeParts[2].trim());
            } else {
                edu.setInstitution(l.trim());
                edu.setDegree(l.trim());
            }
            data.getEducation().add(edu);
        }

        // 5. Parse PROJECTS Section
        List<String> projLines = sections.getOrDefault("PROJECTS", Collections.emptyList());
        com.nitin.ResumeService.dto.extracted.ProjectItem currentProj = null;

        for (String l : projLines) {
            boolean isBullet = l.startsWith("●") || l.startsWith("- ") || l.startsWith("*") || l.startsWith("•");
            if (isBullet) {
                if (currentProj != null) {
                    String cleanBullet = l.replaceFirst("^[●\\-*•]\\s*", "").trim();
                    String desc = currentProj.getDescription() == null || currentProj.getDescription().isEmpty()
                            ? cleanBullet
                            : currentProj.getDescription() + " " + cleanBullet;
                    currentProj.setDescription(desc);
                }
            } else if (l.contains("—") || l.contains("–") || l.contains(" - ") || l.contains("|") || l.toLowerCase().contains("github")) {
                currentProj = new com.nitin.ResumeService.dto.extracted.ProjectItem();
                String[] pParts = l.split("\\|");
                String titlePart = pParts[0].replaceAll("(?i)github", "").trim();
                currentProj.setTitle(titlePart);

                if (pParts.length > 1) {
                    String techPart = pParts[1].replaceAll("(?i)github", "").trim();
                    String[] stack = techPart.split("[,·•]");
                    for (String st : stack) {
                        String s = st.trim();
                        if (!s.isEmpty()) currentProj.getTechStack().add(s);
                    }
                }
                data.getProjects().add(currentProj);
            }
        }

        // 6. Parse CERTIFICATIONS & ACHIEVEMENTS Section
        List<String> certLines = sections.getOrDefault("CERTIFICATIONS", Collections.emptyList());
        for (String l : certLines) {
            String clean = l.replaceFirst("^[●\\-*•]\\s*", "").trim();
            if (!clean.isEmpty() && clean.length() > 3) {
                data.getCertifications().add(clean);
            }
        }

        // 7. Parse EXPERIENCE Section
        List<String> expLines = sections.getOrDefault("EXPERIENCE", Collections.emptyList());
        com.nitin.ResumeService.dto.extracted.ExperienceItem currentExp = null;
        for (String l : expLines) {
            boolean isBullet = l.startsWith("●") || l.startsWith("-") || l.startsWith("*") || l.startsWith("•");
            if (isBullet) {
                if (currentExp != null) {
                    currentExp.getHighlights().add(l.replaceFirst("^[●\\-*•]\\s*", "").trim());
                }
            } else if (l.contains("—") || l.contains("–") || l.contains("-") || l.toLowerCase().contains("intern") || l.toLowerCase().contains("developer") || l.toLowerCase().contains("engineer")) {
                currentExp = new com.nitin.ResumeService.dto.extracted.ExperienceItem();
                currentExp.setRole(l.trim());
                currentExp.setCompany("Company / Organization");
                data.getExperience().add(currentExp);
            }
        }

        return data;
    }

    private Set<String> extractSkillsFromText(String text) {
        Set<String> found = new LinkedHashSet<>();
        String lowerText = " " + text.toLowerCase() + " ";

        for (String skill : ALL_TECH_SKILLS) {
            String lowerSkill = skill.toLowerCase();
            // Handle single-letter or short skills like "C" or "Go" with word boundary
            if (skill.length() <= 2) {
                if (Pattern.compile("\\b" + Pattern.quote(lowerSkill) + "\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()) {
                    found.add(skill);
                }
            } else {
                if (lowerText.contains(lowerSkill)) {
                    found.add(skill);
                }
            }
        }
        return found;
    }

    private boolean isSkillPresent(String fullText, Set<String> extractedSkills, String skill) {
        String lowerSkill = skill.toLowerCase();
        if (extractedSkills.stream().anyMatch(s -> s.equalsIgnoreCase(skill))) {
            return true;
        }

        // Check common aliases
        if (lowerSkill.contains("react") && fullText.toLowerCase().contains("react")) return true;
        if (lowerSkill.contains("spring") && fullText.toLowerCase().contains("spring")) return true;
        if (lowerSkill.contains("node") && fullText.toLowerCase().contains("node")) return true;
        if (lowerSkill.contains("oop") && (fullText.toLowerCase().contains("oop") || fullText.toLowerCase().contains("object oriented"))) return true;
        if (lowerSkill.contains("sql") && (fullText.toLowerCase().contains("sql") || fullText.toLowerCase().contains("mysql") || fullText.toLowerCase().contains("postgres"))) return true;
        if (lowerSkill.contains("dsa") || lowerSkill.contains("data structure")) {
            if (fullText.toLowerCase().contains("data structure") || fullText.toLowerCase().contains("dsa") || fullText.toLowerCase().contains("algorithms")) return true;
        }

        return fullText.toLowerCase().contains(lowerSkill);
    }

    private int calculateFormatScore(String text) {
        int score = 40; // Base score for readable text
        String lower = text.toLowerCase();

        if (lower.contains("education") || lower.contains("b.tech") || lower.contains("bachelor") || lower.contains("degree")) score += 12;
        if (lower.contains("experience") || lower.contains("internship") || lower.contains("work history") || lower.contains("employment")) score += 15;
        if (lower.contains("projects") || lower.contains("project work") || lower.contains("key projects")) score += 15;
        if (lower.contains("skills") || lower.contains("technical competencies") || lower.contains("technologies")) score += 10;
        if (lower.contains("@") && (lower.contains("github.com") || lower.contains("linkedin.com") || lower.contains("portfolio"))) score += 8;

        return Math.min(100, score);
    }

    private int calculateExperienceScore(String text) {
        int score = 40;
        String lower = text.toLowerCase();

        // Check action verbs
        long actionVerbCount = ACTION_VERBS.stream().filter(lower::contains).count();
        score += (int) Math.min(30, actionVerbCount * 5);

        // Check for metrics/quantified results (numbers with %, ms, k, etc.)
        boolean hasPercentages = text.contains("%");
        boolean hasMetrics = Pattern.compile("\\b(\\d+(\\.\\d+)?%|\\d+\\+?\\s*(users|clients|ms|seconds|fps|requests|stars))\\b", Pattern.CASE_INSENSITIVE).matcher(text).find();

        if (hasPercentages) score += 15;
        if (hasMetrics) score += 15;

        return Math.min(100, score);
    }

    private List<String> generateSuggestions(List<String> missingSkills, int formatScore, int experienceScore, String roleName) {
        List<String> suggestions = new ArrayList<>();

        if (!missingSkills.isEmpty()) {
            suggestions.add("Add target role keywords: Include essential skills for " + roleName + ", specifically: " + String.join(", ", missingSkills.subList(0, Math.min(4, missingSkills.size()))) + ".");
        }

        if (experienceScore < 70) {
            suggestions.add("Quantify your achievements: Use the X-Y-Z formula (Accomplished [X] measured by [Y] by doing [Z]). For example, instead of 'Optimized API', write 'Optimized database queries, reducing response latency by 35%'.");
            suggestions.add("Begin bullet points with high-impact action verbs like Architected, Engineered, Optimized, Spearheaded, or Streamlined rather than passive phrases.");
        }

        if (formatScore < 75) {
            suggestions.add("Standardize resume section headers into clear, ATS-parseable titles: 'Education', 'Technical Skills', 'Experience / Internships', and 'Projects'.");
            suggestions.add("Ensure professional contact details are prominently placed at the top, including your GitHub and LinkedIn profile URLs.");
        }

        suggestions.add("Tailor your project descriptions to mirror requirements commonly found in " + roleName + " campus placement and hiring assessments.");

        return suggestions;
    }

    private String generateSummary(int atsScore, String roleName, List<String> matched, List<String> missing) {
        StringBuilder sb = new StringBuilder();
        if (atsScore >= 75) {
            sb.append("Strong resume profile for ").append(roleName).append(" with a high ATS compatibility score of ").append(atsScore).append("/100. ");
        } else if (atsScore >= 55) {
            sb.append("Moderate match for ").append(roleName).append(" (Score: ").append(atsScore).append("/100). The resume demonstrates foundational competencies but requires targeted skill keywords and quantified impact statements. ");
        } else {
            sb.append("Early stage preparation for ").append(roleName).append(" (Score: ").append(atsScore).append("/100). Essential core skills and ATS-friendly section structuring are needed to pass screening algorithms. ");
        }

        if (!matched.isEmpty()) {
            sb.append("Key matching strengths detected: ").append(String.join(", ", matched.subList(0, Math.min(5, matched.size())))).append(". ");
        }
        if (!missing.isEmpty()) {
            sb.append("Recommended additions: ").append(String.join(", ", missing.subList(0, Math.min(4, missing.size())))).append(".");
        }
        return sb.toString();
    }
}
