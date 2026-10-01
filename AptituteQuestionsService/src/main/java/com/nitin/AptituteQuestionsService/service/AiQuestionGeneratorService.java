package com.nitin.AptituteQuestionsService.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nitin.AptituteQuestionsService.model.DifficultyLevel;
import com.nitin.AptituteQuestionsService.model.Question;
import com.nitin.AptituteQuestionsService.model.QuestionCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AiQuestionGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(AiQuestionGeneratorService.class);

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    public AiQuestionGeneratorService(@Autowired(required = false) ChatModel chatModel,
                                      @Autowired(required = false) ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    public boolean isAiConfigured() {
        return chatModel != null &&
               apiKey != null &&
               !apiKey.isBlank() &&
               !apiKey.startsWith("dummy-");
    }

    public List<Question> generateQuestions(int count, QuestionCategory categoryFilter,
                                            DifficultyLevel difficultyFilter, List<String> topicFilters) {
        if (!isAiConfigured()) {
            throw new IllegalStateException("Groq API key is not configured or is set to dummy placeholder. Please set GROQ_API_KEY.");
        }

        String prompt = buildPrompt(count, categoryFilter, difficultyFilter, topicFilters);
        log.info("Sending prompt to Groq AI for generating {} aptitude questions...", count);

        try {
            String rawResponse = chatModel.call(prompt);
            String jsonContent = extractJson(rawResponse);
            return parseQuestions(jsonContent);
        } catch (Exception ex) {
            log.error("Failed to generate questions via Groq AI: {}", ex.getMessage());
            throw new RuntimeException("Groq AI generation failed: " + ex.getMessage(), ex);
        }
    }

    private String buildPrompt(int count, QuestionCategory categoryFilter,
                               DifficultyLevel difficultyFilter, List<String> topicFilters) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert interview assessment evaluator for high-growth tech and finance companies.\n");
        sb.append("Generate exactly ").append(count).append(" high-quality multiple choice aptitude questions for a candidate screening test.\n\n");

        if (categoryFilter != null) {
            sb.append("Category requirement: Only generate questions for ").append(categoryFilter.name()).append(".\n");
        } else {
            sb.append("Category requirement: Provide a balanced mix across QUANTITATIVE, LOGICAL_REASONING, VERBAL_ABILITY, and DATA_INTERPRETATION.\n");
        }

        if (difficultyFilter != null) {
            sb.append("Difficulty requirement: All questions should be ").append(difficultyFilter.name()).append(".\n");
        } else {
            sb.append("Difficulty requirement: A healthy mix of EASY, MEDIUM, and HARD.\n");
        }

        if (topicFilters != null && !topicFilters.isEmpty()) {
            sb.append("Focus topics: ").append(String.join(", ", topicFilters)).append(".\n");
        }

        sb.append("\nCRITICAL FORMAT INSTRUCTIONS:\n");
        sb.append("Respond ONLY with a valid JSON array of objects. Do not include introductory text, markdown explanations, or code blocks outside the JSON.\n");
        sb.append("Keep each explanation concise (1-2 sentences) so all questions fit cleanly.\n");
        sb.append("Each object MUST contain the following fields:\n");
        sb.append("{\n");
        sb.append("  \"id\": \"APT-Q1\",\n");
        sb.append("  \"category\": \"QUANTITATIVE | LOGICAL_REASONING | VERBAL_ABILITY | DATA_INTERPRETATION\",\n");
        sb.append("  \"topic\": \"Specific Topic Name (e.g. Percentages, Blood Relations)\",\n");
        sb.append("  \"difficulty\": \"EASY | MEDIUM | HARD\",\n");
        sb.append("  \"question\": \"The problem statement or puzzle\",\n");
        sb.append("  \"options\": [\"Option 1 text\", \"Option 2 text\", \"Option 3 text\", \"Option 4 text\"],\n");
        sb.append("  \"correctOptionIndex\": 0,\n");
        sb.append("  \"correctAnswer\": \"Exact text of the correct option\",\n");
        sb.append("  \"explanation\": \"Step-by-step solution and mathematical/logical justification\",\n");
        sb.append("  \"marks\": 1\n");
        sb.append("}\n");

        return sb.toString();
    }

    private String extractJson(String text) {
        if (text == null) return "[]";
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

        int arrayStart = trimmed.indexOf('[');
        if (arrayStart != -1) {
            int arrayEnd = trimmed.lastIndexOf(']');
            if (arrayEnd > arrayStart) {
                return trimmed.substring(arrayStart, arrayEnd + 1);
            }
            // If response was cut off before closing ']', close at last complete object
            int lastObjectEnd = trimmed.lastIndexOf('}');
            if (lastObjectEnd > arrayStart) {
                return trimmed.substring(arrayStart, lastObjectEnd + 1) + "]";
            }
        }
        return trimmed;
    }

    private List<Question> parseQuestions(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        List<Question> list = new ArrayList<>();

        if (root.isArray()) {
            for (JsonNode node : root) {
                Question q = new Question();
                q.setId(node.has("id") ? node.get("id").asText("APT-" + UUID.randomUUID().toString().substring(0, 8)) : "APT-" + UUID.randomUUID().toString().substring(0, 8));

                String catStr = node.has("category") ? node.get("category").asText() : "QUANTITATIVE";
                QuestionCategory cat = QuestionCategory.fromString(catStr);
                q.setCategory(cat != null ? cat : QuestionCategory.QUANTITATIVE);

                q.setTopic(node.has("topic") ? node.get("topic").asText("General Aptitude") : "General Aptitude");

                String diffStr = node.has("difficulty") ? node.get("difficulty").asText() : "MEDIUM";
                DifficultyLevel diff = DifficultyLevel.fromString(diffStr);
                q.setDifficulty(diff != null ? diff : DifficultyLevel.MEDIUM);

                q.setQuestion(node.has("question") ? node.get("question").asText() : "");

                List<String> options = new ArrayList<>();
                if (node.has("options") && node.get("options").isArray()) {
                    for (JsonNode opt : node.get("options")) {
                        options.add(opt.asText());
                    }
                }
                q.setOptions(options);

                int correctIdx = node.has("correctOptionIndex") ? node.get("correctOptionIndex").asInt(0) : 0;
                if (correctIdx < 0 || correctIdx >= options.size()) {
                    correctIdx = 0;
                }
                q.setCorrectOptionIndex(correctIdx);

                if (node.has("correctAnswer")) {
                    q.setCorrectAnswer(node.get("correctAnswer").asText());
                } else if (!options.isEmpty()) {
                    q.setCorrectAnswer(options.get(correctIdx));
                }

                q.setExplanation(node.has("explanation") ? node.get("explanation").asText() : "No explanation provided.");
                q.setMarks(node.has("marks") ? node.get("marks").asInt(1) : 1);

                list.add(q);
            }
        }
        return list;
    }
}
