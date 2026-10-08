package com.RoadMapService.RoadMapService.service;

import com.RoadMapService.RoadMapService.dto.RoadmapResponse;
import com.RoadMapService.RoadMapService.model.PerformanceProfileEntity;
import com.RoadMapService.RoadMapService.model.RoadmapActionItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AiRoadmapEngineService {

    private static final Logger log = LoggerFactory.getLogger(AiRoadmapEngineService.class);

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final RuleBasedRoadmapEngineService fallbackEngine;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    public AiRoadmapEngineService(@Autowired(required = false) ChatModel chatModel,
                                 @Autowired(required = false) ObjectMapper objectMapper,
                                 RuleBasedRoadmapEngineService fallbackEngine) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.fallbackEngine = fallbackEngine;
    }

    public boolean isAiConfigured() {
        return chatModel != null &&
               apiKey != null &&
               !apiKey.isBlank() &&
               !apiKey.startsWith("dummy-") &&
               !apiKey.equalsIgnoreCase("your_api_key_here");
    }

    public RoadmapResponse generateRoadmap(Long studentId, String targetRole,
                                          PerformanceProfileEntity profile,
                                          List<String> missingSkills) {
        if (!isAiConfigured()) {
            return fallbackEngine.generatePersonalizedRoadmap(studentId, targetRole, profile, missingSkills);
        }

        try {
            String prompt = buildPrompt(targetRole, profile, missingSkills);
            String rawJson = chatModel.call(prompt);
            String jsonText = extractJson(rawJson);
            return parseAiRoadmap(jsonText, studentId, targetRole, profile);
        } catch (Exception ex) {
            log.warn("AI roadmap generation failed: {}. Falling back to Rule-Based Roadmap Engine.", ex.getMessage());
            return fallbackEngine.generatePersonalizedRoadmap(studentId, targetRole, profile, missingSkills);
        }
    }

    private String buildPrompt(String targetRole, PerformanceProfileEntity profile, List<String> missingSkills) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert Placement Preparation Director and Career Coach for computer science students.\n");
        sb.append("Generate a personalized, prioritized, step-by-step preparation roadmap for a student aiming for: '").append(targetRole).append("'.\n\n");

        sb.append("CURRENT PERFORMANCE METRICS (0-100):\n");
        sb.append("- Overall Readiness: ").append(profile.getOverallReadinessScore()).append("\n");
        sb.append("- Resume ATS Score: ").append(profile.getResumeScore() != null ? profile.getResumeScore() : "N/A").append("\n");
        sb.append("- Technical Mock Interview Score: ").append(profile.getTechnicalInterviewScore() != null ? profile.getTechnicalInterviewScore() : "N/A").append("\n");
        sb.append("- HR Mock Interview Score: ").append(profile.getHrInterviewScore() != null ? profile.getHrInterviewScore() : "N/A").append("\n");
        sb.append("- Coding Practice Score: ").append(profile.getCodingScore() != null ? profile.getCodingScore() : "N/A").append("\n");
        sb.append("- Aptitude Score: ").append(profile.getAptitudeScore() != null ? profile.getAptitudeScore() : "N/A").append("\n\n");

        if (missingSkills != null && !missingSkills.isEmpty()) {
            sb.append("Missing Resume Skills: ").append(String.join(", ", missingSkills)).append("\n\n");
        }

        sb.append("INSTRUCTIONS:\n");
        sb.append("1. Identify the 3 to 5 most urgent and high-yield action steps ordered by priority.\n");
        sb.append("2. Output MUST be valid JSON matching this exact structure, with no markdown outside:\n");
        sb.append("{\n");
        sb.append("  \"summaryVerdict\": \"Strategic advice paragraph synthesizing candidate readiness.\",\n");
        sb.append("  \"actions\": [\n");
        sb.append("    {\n");
        sb.append("      \"stepNumber\": 1,\n");
        sb.append("      \"title\": \"Action Title\",\n");
        sb.append("      \"category\": \"RESUME | TECHNICAL_INTERVIEW | HR_INTERVIEW | CODING | APTITUDE\",\n");
        sb.append("      \"priority\": \"HIGH | MEDIUM | LOW\",\n");
        sb.append("      \"actionType\": \"PRACTICE_CODING | RETAKE_MOCK_INTERVIEW | UPDATE_RESUME | PRACTICE_APTITUDE\",\n");
        sb.append("      \"description\": \"Specific practical steps and topics to practice\",\n");
        sb.append("      \"estimatedHours\": 3,\n");
        sb.append("      \"suggestedTarget\": \"Measurable goal for completion\",\n");
        sb.append("      \"status\": \"PENDING\"\n");
        sb.append("    }\n");
        sb.append("  ]\n");
        sb.append("}\n");

        return sb.toString();
    }

    private RoadmapResponse parseAiRoadmap(String json, Long studentId, String targetRole, PerformanceProfileEntity profile) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        RoadmapResponse resp = new RoadmapResponse();
        resp.setStudentId(studentId);
        resp.setTargetRole(targetRole);
        resp.setGenericStarter(false);
        resp.setOverallReadinessScore(profile.getOverallReadinessScore());
        resp.setIdentifiedWeakAreas(profile.getWeakAreas());
        resp.setGeneratedAt(Instant.now());

        if (root.has("summaryVerdict")) {
            resp.setSummaryVerdict(root.get("summaryVerdict").asText());
        } else {
            resp.setSummaryVerdict("Prioritized roadmap generated based on current cross-module assessments.");
        }

        List<RoadmapActionItem> actionItems = new ArrayList<>();
        if (root.has("actions") && root.get("actions").isArray()) {
            for (JsonNode node : root.get("actions")) {
                RoadmapActionItem item = objectMapper.treeToValue(node, RoadmapActionItem.class);
                actionItems.add(item);
            }
        }

        if (actionItems.isEmpty()) {
            return fallbackEngine.generatePersonalizedRoadmap(studentId, targetRole, profile, null);
        }

        resp.setRecommendedActions(actionItems);
        resp.setTotalEstimatedHours(actionItems.stream().mapToInt(RoadmapActionItem::getEstimatedHours).sum());

        return resp;
    }

    private String extractJson(String text) {
        if (text == null) return "{}";
        String trimmed = text.trim();
        if (trimmed.startsWith("```json")) trimmed = trimmed.substring(7);
        else if (trimmed.startsWith("```")) trimmed = trimmed.substring(3);
        if (trimmed.endsWith("```")) trimmed = trimmed.substring(0, trimmed.length() - 3);
        trimmed = trimmed.trim();

        int objStart = trimmed.indexOf('{');
        int objEnd = trimmed.lastIndexOf('}');
        if (objStart != -1 && objEnd > objStart) {
            return trimmed.substring(objStart, objEnd + 1);
        }
        return trimmed;
    }
}
