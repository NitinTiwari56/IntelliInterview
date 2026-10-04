package com.nitin.ResumeService.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nitin.ResumeService.dto.ResumeAnalysisResponse;
import com.nitin.ResumeService.model.RoleSkillProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiResumeAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AiResumeAnalysisService.class);

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final TargetRoleService targetRoleService;
    private final RuleBasedAtsAnalyzerService fallbackService;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    public AiResumeAnalysisService(@Autowired(required = false) ChatModel chatModel,
                                   @Autowired(required = false) ObjectMapper objectMapper,
                                   TargetRoleService targetRoleService,
                                   RuleBasedAtsAnalyzerService fallbackService) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.targetRoleService = targetRoleService;
        this.fallbackService = fallbackService;
    }

    public boolean isAiConfigured() {
        return chatModel != null &&
               apiKey != null &&
               !apiKey.isBlank() &&
               !apiKey.startsWith("dummy-") &&
               !apiKey.equalsIgnoreCase("gsk_your_groq_api_key_here");
    }

    public ResumeAnalysisResponse analyzeResume(String resumeText, String targetRole) {
        if (!isAiConfigured()) {
            log.info("AI (Groq/OpenAI) is not configured or API key is missing. Using high-accuracy Rule-Based ATS Analyzer.");
            return fallbackService.analyzeResume(resumeText, targetRole);
        }

        RoleSkillProfile roleProfile = targetRoleService.getRoleProfile(targetRole);
        String actualRoleName = roleProfile.getRoleName();
        String prompt = buildPrompt(resumeText, actualRoleName, roleProfile);

        log.info("Sending resume analysis request to AI model for role: '{}'...", actualRoleName);

        try {
            String rawResponse = chatModel.call(prompt);
            String jsonText = extractJson(rawResponse);
            ResumeAnalysisResponse response = parseAiResponse(jsonText, actualRoleName, resumeText);
            response.setAnalysisSource("AI_POWERED");
            log.info("AI Analysis completed successfully. ATS Score: {}", response.getAtsScore());
            return response;
        } catch (Exception ex) {
            log.warn("AI generation encountered error: {}. Falling back to Rule-Based ATS Analyzer...", ex.getMessage());
            ResumeAnalysisResponse fallback = fallbackService.analyzeResume(resumeText, targetRole);
            fallback.setSummaryFeedback(fallback.getSummaryFeedback() + " (Note: Generated via ATS fallback engine due to AI service timeout/rate limit).");
            return fallback;
        }
    }

    private String buildPrompt(String resumeText, String roleName, RoleSkillProfile profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert Technical Recruiter and modern ATS (Applicant Tracking System) Evaluation Engine for campus placement hiring.\n");
        sb.append("Analyze the following candidate resume text against the Target Job Role: '").append(roleName).append("'.\n\n");

        sb.append("Role Description: ").append(profile.getDescription()).append("\n");
        sb.append("Core Expected Skills for this role: ").append(String.join(", ", profile.getCoreSkills())).append("\n");
        sb.append("Secondary / Nice-to-have Skills: ").append(String.join(", ", profile.getSecondarySkills())).append("\n\n");

        sb.append("--- CANDIDATE RESUME TEXT START ---\n");
        // Limit text length to prevent context explosion if resume has unexpected padding
        if (resumeText.length() > 8000) {
            sb.append(resumeText, 0, 8000).append("\n[...text truncated...]\n");
        } else {
            sb.append(resumeText).append("\n");
        }
        sb.append("--- CANDIDATE RESUME TEXT END ---\n\n");

        sb.append("EVALUATION CRITERIA:\n");
        sb.append("1. 'atsScore' (Integer, 0-100): Overall ATS compatibility rating.\n");
        sb.append("2. 'formatScore' (Integer, 0-100): Clear section layout, professional structure, readability.\n");
        sb.append("3. 'experienceScore' (Integer, 0-100): Measurable metrics, action verbs, project depth.\n");
        sb.append("4. 'extractedSkills': Array of all relevant technical skills found in the resume.\n");
        sb.append("5. 'matchedSkills': Skills found in the resume that align directly with the target role.\n");
        sb.append("6. 'missingSkills': Critical skills for this role that are missing or poorly represented.\n");
        sb.append("7. 'suggestions': Array of 3-5 concise, actionable bullet points to improve the resume (e.g., specific missing tech keywords, quantifiable bullet points rewrites).\n");
        sb.append("8. 'summaryFeedback': A 2-3 sentence executive recruiter verdict on the candidate's readiness.\n\n");

        sb.append("CRITICAL FORMAT INSTRUCTIONS:\n");
        sb.append("Return ONLY a valid JSON object. Do not include markdown preamble, conversational text, or explanation outside the JSON.\n");
        sb.append("You MUST extract and populate the candidate's actual education, projects, and certifications from the resume text.\n");
        sb.append("{\n");
        sb.append("  \"atsScore\": 84,\n");
        sb.append("  \"formatScore\": 85,\n");
        sb.append("  \"experienceScore\": 80,\n");
        sb.append("  \"extractedSkills\": [\"Java\", \"Spring Boot\", \"React.js\", \"MySQL\", \"Docker\", \"Kafka\"],\n");
        sb.append("  \"matchedSkills\": [\"Java\", \"Spring Boot\", \"MySQL\", \"Docker\"],\n");
        sb.append("  \"missingSkills\": [\"C++\", \"System Design\", \"Multithreading\"],\n");
        sb.append("  \"suggestions\": [\"Add quantifiable metrics to project outcomes\", \"Include multithreading examples\"],\n");
        sb.append("  \"summaryFeedback\": \"Strong full-stack and Java foundation. Enhancing system design and concurrency will boost placement odds.\",\n");
        sb.append("  \"extractedData\": {\n");
        sb.append("    \"candidateInfo\": {\"name\": \"Candidate Full Name\", \"email\": \"email@example.com\", \"phone\": \"+1234567890\", \"linkedin\": \"LinkedIn\", \"github\": \"GitHub\", \"portfolio\": null},\n");
        sb.append("    \"education\": [\n");
        sb.append("      {\"institution\": \"College/University Name\", \"degree\": \"Degree Title\", \"year\": \"2023-2027\", \"score\": \"8.5 CGPA\"}\n");
        sb.append("    ],\n");
        sb.append("    \"experience\": [],\n");
        sb.append("    \"projects\": [\n");
        sb.append("      {\"title\": \"Project Name\", \"techStack\": [\"React.js\", \"Spring Boot\"], \"description\": \"Summary of features built\", \"link\": null}\n");
        sb.append("    ],\n");
        sb.append("    \"certifications\": [\n");
        sb.append("      \"Hackathon or Course Certification Title\"\n");
        sb.append("    ]\n");
        sb.append("  }\n");
        sb.append("}\n");

        return sb.toString();
    }

    private String extractJson(String text) {
        if (text == null) return "{}";
        String trimmed = text.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        trimmed = trimmed.trim();

        int objStart = trimmed.indexOf('{');
        int objEnd = trimmed.lastIndexOf('}');
        if (objStart != -1 && objEnd > objStart) {
            return trimmed.substring(objStart, objEnd + 1);
        }
        return trimmed;
    }

    private ResumeAnalysisResponse parseAiResponse(String json, String targetRole, String rawResumeText) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        ResumeAnalysisResponse response = new ResumeAnalysisResponse();

        response.setTargetRole(targetRole);
        response.setAtsScore(root.has("atsScore") ? root.get("atsScore").asInt(70) : 70);
        response.setFormatScore(root.has("formatScore") ? root.get("formatScore").asInt(75) : 75);
        response.setExperienceScore(root.has("experienceScore") ? root.get("experienceScore").asInt(70) : 70);

        response.setExtractedSkills(extractStringList(root, "extractedSkills"));
        response.setMatchedSkills(extractStringList(root, "matchedSkills"));
        response.setMissingSkills(extractStringList(root, "missingSkills"));
        response.setSuggestions(extractStringList(root, "suggestions"));

        if (root.has("summaryFeedback")) {
            response.setSummaryFeedback(root.get("summaryFeedback").asText());
        } else {
            response.setSummaryFeedback("Analysis completed for role " + targetRole + ".");
        }

        // Build and merge extractedData
        com.nitin.ResumeService.dto.extracted.ExtractedResumeData ruleData =
                fallbackService.extractComprehensiveResumeData(rawResumeText, new java.util.HashSet<>(response.getExtractedSkills()));

        com.nitin.ResumeService.dto.extracted.ExtractedResumeData extractedData = null;
        if (root.has("extractedData") && root.get("extractedData").isObject()) {
            try {
                extractedData = objectMapper.treeToValue(root.get("extractedData"), com.nitin.ResumeService.dto.extracted.ExtractedResumeData.class);
            } catch (Exception e) {
                log.warn("Failed to map extractedData from AI output: {}", e.getMessage());
            }
        }

        if (extractedData == null) {
            extractedData = ruleData;
        } else {
            // Merge: If AI missed education, projects, or certifications, fill from rule-based section parser!
            if (extractedData.getEducation() == null || extractedData.getEducation().isEmpty()) {
                extractedData.setEducation(ruleData.getEducation());
            }
            if (extractedData.getProjects() == null || extractedData.getProjects().isEmpty()) {
                extractedData.setProjects(ruleData.getProjects());
            }
            if (extractedData.getCertifications() == null || extractedData.getCertifications().isEmpty()) {
                extractedData.setCertifications(ruleData.getCertifications());
            }
            if (extractedData.getCandidateInfo() == null || extractedData.getCandidateInfo().getName() == null) {
                extractedData.setCandidateInfo(ruleData.getCandidateInfo());
            }
            if (extractedData.getSkills() == null || (extractedData.getSkills().getLanguages().isEmpty() && extractedData.getSkills().getFrameworks().isEmpty())) {
                extractedData.setSkills(ruleData.getSkills());
            }
        }

        extractedData.setRawText(rawResumeText);
        response.setExtractedData(extractedData);

        return response;
    }

    private List<String> extractStringList(JsonNode root, String fieldName) {
        List<String> list = new ArrayList<>();
        if (root.has(fieldName) && root.get(fieldName).isArray()) {
            for (JsonNode item : root.get(fieldName)) {
                list.add(item.asText());
            }
        }
        return list;
    }
}
