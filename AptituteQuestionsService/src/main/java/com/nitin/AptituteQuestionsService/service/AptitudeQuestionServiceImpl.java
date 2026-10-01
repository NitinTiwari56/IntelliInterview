package com.nitin.AptituteQuestionsService.service;

import com.nitin.AptituteQuestionsService.dto.QuestionBatchResponse;
import com.nitin.AptituteQuestionsService.dto.QuestionRequest;
import com.nitin.AptituteQuestionsService.model.DifficultyLevel;
import com.nitin.AptituteQuestionsService.model.Question;
import com.nitin.AptituteQuestionsService.model.QuestionCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AptitudeQuestionServiceImpl implements AptitudeQuestionService {

    private static final Logger log = LoggerFactory.getLogger(AptitudeQuestionServiceImpl.class);

    private final QuestionBankService questionBankService;
    private final AiQuestionGeneratorService aiQuestionGeneratorService;

    public AptitudeQuestionServiceImpl(QuestionBankService questionBankService,
                                       AiQuestionGeneratorService aiQuestionGeneratorService) {
        this.questionBankService = questionBankService;
        this.aiQuestionGeneratorService = aiQuestionGeneratorService;
    }

    @Override
    public QuestionBatchResponse generateQuestions(QuestionRequest request) {
        if (request == null) {
            request = new QuestionRequest();
        }

        int count = request.getCount() != null ? request.getCount() : 20;
        if (count < 1) count = 20;
        if (count > 100) count = 100; // Protection cap

        QuestionCategory category = QuestionCategory.fromString(request.getCategory());
        DifficultyLevel difficulty = DifficultyLevel.fromString(request.getDifficulty());
        List<String> topics = request.getTopics();
        String preferredSource = request.getSource() != null ? request.getSource().toUpperCase() : "AUTO";

        List<Question> questions = null;
        String actualSource = "QUESTION_BANK";
        String statusMessage = "Successfully generated " + count + " aptitude questions.";

        // Strategy 1: AI requested explicitly
        if ("AI".equalsIgnoreCase(preferredSource)) {
            try {
                if (aiQuestionGeneratorService.isAiConfigured()) {
                    questions = aiQuestionGeneratorService.generateQuestions(count, category, difficulty, topics);
                    if (questions == null || questions.isEmpty()) {
                        log.warn("AI returned 0 questions, falling back to question bank.");
                        questions = questionBankService.getRandomQuestions(count, category, difficulty, topics);
                        actualSource = "QUESTION_BANK";
                        statusMessage = "AI returned 0 questions; returned curated random aptitude questions from bank.";
                    } else {
                        actualSource = "AI_GENERATED";
                        statusMessage = "Successfully generated " + questions.size() + " aptitude questions via Groq AI.";
                    }
                } else {
                    log.warn("AI generation requested, but Groq API key is not configured. Falling back to question bank.");
                    questions = questionBankService.getRandomQuestions(count, category, difficulty, topics);
                    statusMessage = "Groq API key not configured; returned curated random aptitude questions from bank.";
                }
            } catch (Exception ex) {
                log.warn("AI generation failed ({}), falling back to question bank.", ex.getMessage());
                questions = questionBankService.getRandomQuestions(count, category, difficulty, topics);
                statusMessage = "AI generation unavailable (" + ex.getMessage() + "); fell back to question bank.";
            }
        }
        // Strategy 2: AUTO (Use AI if configured and operational, otherwise fast bank)
        else if ("AUTO".equalsIgnoreCase(preferredSource)) {
            if (aiQuestionGeneratorService.isAiConfigured()) {
                try {
                    questions = aiQuestionGeneratorService.generateQuestions(count, category, difficulty, topics);
                    if (questions == null || questions.isEmpty()) {
                        log.warn("AI generation produced 0 questions, falling back to question bank.");
                        questions = questionBankService.getRandomQuestions(count, category, difficulty, topics);
                        actualSource = "QUESTION_BANK";
                        statusMessage = "Successfully generated " + count + " random aptitude questions from question bank (AI fallback).";
                    } else {
                        actualSource = "AI_GENERATED";
                        statusMessage = "Successfully generated " + questions.size() + " aptitude questions via Groq AI.";
                    }
                } catch (Exception ex) {
                    log.warn("Auto AI generation attempt failed, falling back to question bank: {}", ex.getMessage());
                    questions = questionBankService.getRandomQuestions(count, category, difficulty, topics);
                    statusMessage = "Generated random aptitude questions from question bank (AI fallback).";
                }
            } else {
                questions = questionBankService.getRandomQuestions(count, category, difficulty, topics);
                actualSource = "QUESTION_BANK";
                statusMessage = "Successfully generated " + count + " random aptitude questions from question bank.";
            }
        }
        // Strategy 3: Question Bank directly
        else {
            questions = questionBankService.getRandomQuestions(count, category, difficulty, topics);
            actualSource = "QUESTION_BANK";
            statusMessage = "Successfully generated " + count + " random aptitude questions from question bank.";
        }

        // Calculate category distribution
        Map<String, Integer> distribution = new LinkedHashMap<>();
        if (questions != null) {
            for (Question q : questions) {
                String catName = q.getCategory() != null ? q.getCategory().name() : "OTHER";
                distribution.put(catName, distribution.getOrDefault(catName, 0) + 1);
            }
        }

        return new QuestionBatchResponse(
                true,
                statusMessage,
                questions != null ? questions.size() : 0,
                actualSource,
                distribution,
                questions
        );
    }

    @Override
    public Map<String, Object> getServiceHealthAndStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("status", "UP");
        stats.put("service", "AptitudeQuestionsService");
        stats.put("groqConfigured", aiQuestionGeneratorService.isAiConfigured());
        stats.put("aiConfigured", aiQuestionGeneratorService.isAiConfigured());
        stats.put("openAiConfigured", aiQuestionGeneratorService.isAiConfigured());
        stats.put("questionBankTotalQuestions", questionBankService.getBankSize());
        stats.put("questionBankDistribution", questionBankService.getCategoryCounts());
        return stats;
    }

    @Override
    public Map<String, Object> getAvailableCategories() {
        Map<String, Object> response = new LinkedHashMap<>();

        List<Map<String, String>> categories = Arrays.stream(QuestionCategory.values())
                .map(c -> Map.of("key", c.name(), "displayName", c.getDisplayName()))
                .collect(Collectors.toList());

        List<String> difficulties = Arrays.stream(DifficultyLevel.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        Map<String, List<String>> popularTopics = new LinkedHashMap<>();
        popularTopics.put("QUANTITATIVE", List.of("Time and Work", "Profit and Loss", "Percentages", "Ratio and Proportion", "Speed, Time and Distance", "Probability", "Permutations and Combinations", "Simple and Compound Interest", "Averages", "Number System"));
        popularTopics.put("LOGICAL_REASONING", List.of("Blood Relations", "Coding and Decoding", "Number Series", "Direction Sense", "Syllogisms", "Seating Arrangement", "Clock and Calendar", "Analogy"));
        popularTopics.put("VERBAL_ABILITY", List.of("Synonyms", "Antonyms", "Sentence Correction", "Idioms and Phrases", "Reading Comprehension", "Para Jumbles", "Spotting Errors"));
        popularTopics.put("DATA_INTERPRETATION", List.of("Table Analysis", "Bar Graphs", "Pie Charts", "Caselet Analysis", "Line Graphs"));

        response.put("categories", categories);
        response.put("difficulties", difficulties);
        response.put("sampleTopics", popularTopics);
        return response;
    }
}
