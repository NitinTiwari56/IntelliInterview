package com.InterviewPrep.InterviewPrep.service;

import com.InterviewPrep.InterviewPrep.dto.AnswerEvaluationDto;
import com.InterviewPrep.InterviewPrep.model.InterviewType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiInterviewEngineService {

    private static final Logger log = LoggerFactory.getLogger(AiInterviewEngineService.class);

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final RuleBasedInterviewEngineService fallbackEngine;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    public AiInterviewEngineService(@Autowired(required = false) ChatModel chatModel,
                                   @Autowired(required = false) ObjectMapper objectMapper,
                                   RuleBasedInterviewEngineService fallbackEngine) {
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

    public String generateQuestion(InterviewSessionState sessionState) {
        if (!isAiConfigured()) {
            return fallbackEngine.getNextQuestion(sessionState);
        }

        try {
            String promptText = buildQuestionPrompt(sessionState);
            String question = chatModel.call(promptText);
            if (question != null && !question.isBlank()) {
                return cleanQuestionText(question);
            }
        } catch (Exception ex) {
            log.warn("AI question generation encountered error: {}. Using fallback engine.", ex.getMessage());
        }

        return fallbackEngine.getNextQuestion(sessionState);
    }

    /**
     * REQ-INT-4: Questions shall be streamed to the client as they are generated.
     */
    public Flux<String> generateQuestionStream(InterviewSessionState sessionState) {
        if (!isAiConfigured()) {
            String q = fallbackEngine.getNextQuestion(sessionState);
            // Simulate smooth streaming for the fallback engine so the client gets a real streaming experience
            return simulateStream(q);
        }

        try {
            String promptText = buildQuestionPrompt(sessionState);
            Flux<ChatResponse> responseFlux = chatModel.stream(new Prompt(promptText));
            return responseFlux
                    .map(chatResponse -> {
                        if (chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null) {
                            String token = chatResponse.getResult().getOutput().getText();
                            return token != null ? token : "";
                        }
                        return "";
                    })
                    .filter(token -> !token.isEmpty())
                    .onErrorResume(ex -> {
                        log.warn("Streaming error from AI model: {}. Falling back to rule-based question stream.", ex.getMessage());
                        String q = fallbackEngine.getNextQuestion(sessionState);
                        return simulateStream(q);
                    });
        } catch (Exception ex) {
            log.warn("AI stream initialization failed: {}. Using fallback stream.", ex.getMessage());
            String q = fallbackEngine.getNextQuestion(sessionState);
            return simulateStream(q);
        }
    }

    public AnswerEvaluationDto evaluateAnswer(InterviewSessionState sessionState, String question, String answer) {
        if (!isAiConfigured()) {
            return fallbackEngine.evaluateAnswer(sessionState, question, answer);
        }

        try {
            String promptText = buildEvaluationPrompt(sessionState, question, answer);
            String rawJson = chatModel.call(promptText);
            String jsonText = extractJson(rawJson);
            return parseEvaluationJson(jsonText, sessionState, question, answer);
        } catch (Exception ex) {
            log.warn("AI answer evaluation failed: {}. Using fallback evaluator.", ex.getMessage());
            return fallbackEngine.evaluateAnswer(sessionState, question, answer);
        }
    }

    public String generateConsolidatedFeedback(InterviewSessionState sessionState, double avgScore, int totalQuestions) {
        if (!isAiConfigured()) {
            return fallbackEngine.generateConsolidatedFeedback(sessionState, avgScore, totalQuestions);
        }

        try {
            String promptText = buildConsolidatedFeedbackPrompt(sessionState, avgScore, totalQuestions);
            String feedback = chatModel.call(promptText);
            if (feedback != null && !feedback.isBlank()) {
                return feedback.trim();
            }
        } catch (Exception ex) {
            log.warn("AI consolidated feedback failed: {}. Using fallback summary.", ex.getMessage());
        }

        return fallbackEngine.generateConsolidatedFeedback(sessionState, avgScore, totalQuestions);
    }

    private String buildQuestionPrompt(InterviewSessionState state) {
        int nextQNumber = state.getCurrentQuestionNumber() + 1;
        InterviewType type = state.getInterviewType();

        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert technical interviewer conducting a mock campus placement interview.\n");
        sb.append("Interview Type: ").append(type).append("\n");
        sb.append("Candidate Target Role: ").append(state.getTargetRole()).append("\n");
        sb.append("Current Question Number: ").append(nextQNumber).append(" of ").append(state.getMaxQuestions()).append(".\n");

        if (state.getResumeSkills() != null && !state.getResumeSkills().isEmpty()) {
            sb.append("Candidate Resume Skills: ").append(String.join(", ", state.getResumeSkills())).append("\n");
        }
        if (state.getResumeSummary() != null && !state.getResumeSummary().isBlank()) {
            sb.append("Resume Context: ").append(state.getResumeSummary()).append("\n");
        }

        List<InterviewSessionState.QnaRecord> history = state.getConversationHistory();
        if (!history.isEmpty()) {
            sb.append("\nPrevious Questions & Candidate Answers:\n");
            for (InterviewSessionState.QnaRecord q : history) {
                sb.append("Q").append(q.getQuestionNumber()).append(": ").append(q.getQuestion()).append("\n");
                sb.append("A").append(q.getQuestionNumber()).append(": ").append(q.getAnswer() != null ? q.getAnswer() : "No answer").append("\n");
            }
        }

        sb.append("\nInstructions:\n");
        if (type == InterviewType.HR) {
            sb.append("- Ask an insightful behavioral or situational HR question (using the STAR framework context).\n");
            sb.append("- Focus on teamwork, conflict resolution, deadlines, career vision, or learning from failures.\n");
        } else {
            sb.append("- Ask a realistic, focused technical question directly relevant to the target role (algorithms, system design, core language internals, or databases).\n");
            sb.append("- Make the question challenging but clear. Progress naturally from fundamentals to scenario-based thinking.\n");
        }
        sb.append("- Output ONLY the single interview question text. Do not include conversational filler like 'Great, now let's move to question 2', 'Question:', or quotes.\n");

        return sb.toString();
    }

    private String buildEvaluationPrompt(InterviewSessionState state, String question, String answer) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert interviewer evaluating a candidate's response in a campus placement interview.\n");
        sb.append("Interview Type: ").append(state.getInterviewType()).append("\n");
        sb.append("Role: ").append(state.getTargetRole()).append("\n");
        sb.append("Question: ").append(question).append("\n");
        sb.append("Candidate Answer: ").append(answer).append("\n\n");

        sb.append("Evaluate this answer according to strict industry hiring standards.\n");
        sb.append("Score criteria: 0-100 rubric. Provide constructive feedback, 2-3 specific strengths, and 2-3 areas for improvement.\n");
        sb.append("CRITICAL: Respond ONLY with a valid JSON object in this exact schema, without any markdown formatting:\n");
        sb.append("{\n");
        sb.append("  \"score\": 82,\n");
        sb.append("  \"feedback\": \"Clear and structured answer covering key architectural elements.\",\n");
        sb.append("  \"strengths\": [\"Accurate use of terminology\", \"Good real-world context\"],\n");
        sb.append("  \"improvements\": [\"Mention concurrency edge cases\", \"Quantify performance gains\"]\n");
        sb.append("}\n");

        return sb.toString();
    }

    private String buildConsolidatedFeedbackPrompt(InterviewSessionState state, double avgScore, int totalQuestions) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are the lead interview evaluator. Synthesize an executive placement readiness assessment for this candidate.\n");
        sb.append("Interview Type: ").append(state.getInterviewType()).append("\n");
        sb.append("Target Role: ").append(state.getTargetRole()).append("\n");
        sb.append("Questions Completed: ").append(totalQuestions).append("\n");
        sb.append("Average Score: ").append(String.format("%.1f", avgScore)).append("/100\n\n");

        sb.append("Q&A History:\n");
        for (InterviewSessionState.QnaRecord q : state.getConversationHistory()) {
            sb.append("- Q: ").append(q.getQuestion()).append(" | Score: ").append(q.getScore()).append("\n");
        }

        sb.append("\nWrite a concise 3-4 paragraph assessment including overall hiring recommendation, primary technical/behavioral strengths, and clear actionable preparation advice.");
        return sb.toString();
    }

    private AnswerEvaluationDto parseEvaluationJson(String json, InterviewSessionState state, String question, String answer) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        AnswerEvaluationDto dto = new AnswerEvaluationDto();
        dto.setSessionId(state.getSessionId());
        dto.setQuestionNumber(state.getCurrentQuestionNumber());
        dto.setQuestion(question);
        dto.setAnswer(answer);

        int score = root.has("score") ? root.get("score").asInt(70) : 70;
        dto.setScore(Math.max(0, Math.min(100, score)));

        dto.setFeedback(root.has("feedback") ? root.get("feedback").asText() : "Answer recorded and evaluated.");

        List<String> strengths = new ArrayList<>();
        if (root.has("strengths") && root.get("strengths").isArray()) {
            for (JsonNode n : root.get("strengths")) strengths.add(n.asText());
        }
        if (strengths.isEmpty()) strengths.add("Addressed the prompt directly.");
        dto.setStrengths(strengths);

        List<String> improvements = new ArrayList<>();
        if (root.has("improvements") && root.get("improvements").isArray()) {
            for (JsonNode n : root.get("improvements")) improvements.add(n.asText());
        }
        if (improvements.isEmpty()) improvements.add("Expand further with concrete technical examples.");
        dto.setImprovements(improvements);

        return dto;
    }

    private String cleanQuestionText(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }
        if (trimmed.toLowerCase().startsWith("question:") || trimmed.toLowerCase().startsWith("question 1:") || trimmed.toLowerCase().startsWith("question 2:")) {
            int colonIndex = trimmed.indexOf(':');
            if (colonIndex != -1) {
                trimmed = trimmed.substring(colonIndex + 1).trim();
            }
        }
        return trimmed;
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

    private Flux<String> simulateStream(String fullText) {
        if (fullText == null || fullText.isEmpty()) return Flux.empty();
        // Break into small natural token chunks (words + spaces)
        String[] words = fullText.split("(?<=\\s+)");
        return Flux.fromArray(words);
    }
}
