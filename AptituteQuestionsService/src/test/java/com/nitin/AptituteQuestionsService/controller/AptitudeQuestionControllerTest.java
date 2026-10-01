package com.nitin.AptituteQuestionsService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nitin.AptituteQuestionsService.dto.QuestionBatchResponse;
import com.nitin.AptituteQuestionsService.dto.QuestionRequest;
import com.nitin.AptituteQuestionsService.model.Question;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.ai.openai.api-key=dummy-test-key"})
public class AptitudeQuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/aptitude/generate should return exactly 20 questions by default")
    void testGetDefault20Questions() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/aptitude/generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(20))
                .andExpect(jsonPath("$.questions.length()").value(20))
                .andReturn();

        QuestionBatchResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                QuestionBatchResponse.class
        );

        assertThat(response.getQuestions()).hasSize(20);
        for (Question q : response.getQuestions()) {
            assertThat(q.getId()).isNotBlank();
            assertThat(q.getQuestion()).isNotBlank();
            assertThat(q.getOptions()).hasSize(4);
            assertThat(q.getCorrectAnswer()).isNotBlank();
            assertThat(q.getExplanation()).isNotBlank();
            assertThat(q.getOptions().get(q.getCorrectOptionIndex())).isEqualTo(q.getCorrectAnswer());
        }
    }

    @Test
    @DisplayName("POST /api/v1/aptitude/generate should accept JSON request and return structured response")
    void testPostGenerateQuestions() throws Exception {
        QuestionRequest request = new QuestionRequest();
        request.setCount(20);
        request.setCategory("ALL");
        request.setDifficulty("MIXED");

        MvcResult result = mockMvc.perform(post("/api/v1/aptitude/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(20))
                .andExpect(jsonPath("$.questions.length()").value(20))
                .andReturn();

        QuestionBatchResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                QuestionBatchResponse.class
        );

        assertThat(response.getQuestions()).hasSize(20);
        assertThat(response.getCategoryDistribution()).isNotEmpty();
    }

    @Test
    @DisplayName("GET /api/v1/aptitude/generate with category filter")
    void testGetWithCategoryFilter() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/aptitude/generate?category=QUANTITATIVE&count=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(10))
                .andReturn();

        QuestionBatchResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                QuestionBatchResponse.class
        );

        assertThat(response.getQuestions()).hasSize(10);
        assertThat(response.getQuestions()).allMatch(q -> "QUANTITATIVE".equals(q.getCategory().name()));
    }

    @Test
    @DisplayName("GET /api/v1/aptitude/categories should list supported categories and topics")
    void testGetCategories() throws Exception {
        mockMvc.perform(get("/api/v1/aptitude/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.difficulties").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/aptitude/health should report UP status")
    void testGetHealth() throws Exception {
        mockMvc.perform(get("/api/v1/aptitude/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.questionBankTotalQuestions").isNumber());
    }
}
