package com.interviewiq.aptitude.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.interviewiq.aptitude.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * AI member ke aptitude-question-service (port 8091) ko call karta hai.
 * Locked decision: sirf count bhejte hain, baaki sab default (ALL categories, MIXED difficulty).
 */
@Component
public class QuestionServiceClient {

    private static final Logger log = LoggerFactory.getLogger(QuestionServiceClient.class);

    private final RestClient restClient;

    public QuestionServiceClient(@Value("${question-service.url}") String baseUrl,
                                 @Value("${question-service.read-timeout-ms}") int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(readTimeoutMs);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    public GeneratedQuestionsResponse generate(int count) {
        try {
            GeneratedQuestionsResponse response = restClient.post()
                    .uri("/api/v1/aptitude/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("count", count))
                    .retrieve()
                    .body(GeneratedQuestionsResponse.class);
            if (response == null || response.questions() == null) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "Question service returned an empty response.");
            }
            return response;
        } catch (RestClientException e) {
            log.error("Question service call failed: {}", e.getMessage());
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Question service is unavailable. Please try again.");
        }
    }

    // ---- question service ke response ki shape (sirf jo fields chahiye) ----
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeneratedQuestionsResponse(boolean success, int count, String source,
                                             List<GeneratedQuestion> questions) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeneratedQuestion(String id, String category, String topic, String difficulty,
                                    String question, List<String> options, Integer correctOptionIndex,
                                    String correctAnswer, String explanation, Integer marks) {}
}
