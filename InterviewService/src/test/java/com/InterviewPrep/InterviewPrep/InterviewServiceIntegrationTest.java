package com.InterviewPrep.InterviewPrep;

import com.InterviewPrep.InterviewPrep.controller.InterviewController;
import com.InterviewPrep.InterviewPrep.dto.*;
import com.InterviewPrep.InterviewPrep.model.InputMode;
import com.InterviewPrep.InterviewPrep.model.InterviewType;
import com.InterviewPrep.InterviewPrep.model.SessionStatus;
import com.InterviewPrep.InterviewPrep.service.InterviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class InterviewServiceIntegrationTest {

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private InterviewController interviewController;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(interviewController).build();
    }

    @Test
    void testFullInterviewLifecycleViaService() {
        // 1. Start session
        InterviewStartRequest startReq = new InterviewStartRequest();
        startReq.setStudentId(101L);
        startReq.setInterviewType(InterviewType.TECHNICAL);
        startReq.setInputMode(InputMode.TEXT);
        startReq.setTargetRole("Java Backend Developer");
        startReq.setResumeSkills(List.of("Java", "Spring Boot", "MySQL", "Docker"));
        startReq.setMaxQuestions(2);

        InterviewSessionResponse session = interviewService.startSession(startReq);
        assertThat(session).isNotNull();
        assertThat(session.getSessionId()).isNotBlank();
        assertThat(session.getCurrentQuestionNumber()).isEqualTo(1);
        assertThat(session.getCurrentQuestion()).isNotBlank();
        assertThat(session.getStatus()).isEqualTo(SessionStatus.IN_PROGRESS);

        String sessionId = session.getSessionId();

        // 2. Submit Answer 1
        InterviewAnswerRequest ans1 = new InterviewAnswerRequest(
                sessionId,
                "HashMap uses an array of Node buckets. It calculates index via hash(key) & (n-1). Collisions are handled via linked list chaining, and converted to Red-Black trees if a bucket exceeds 8 nodes for O(log n) lookup."
        );
        AnswerEvaluationDto eval1 = interviewService.submitAnswer(ans1);

        assertThat(eval1).isNotNull();
        assertThat(eval1.getQuestionNumber()).isEqualTo(1);
        assertThat(eval1.getScore()).isGreaterThanOrEqualTo(50);
        assertThat(eval1.getFeedback()).isNotBlank();
        assertThat(eval1.isCompleted()).isFalse();
        assertThat(eval1.getNextQuestionNumber()).isEqualTo(2);
        assertThat(eval1.getNextQuestion()).isNotBlank();

        // 3. Submit Answer 2 (Final question, maxQuestions=2)
        InterviewAnswerRequest ans2 = new InterviewAnswerRequest(
                sessionId,
                "Spring @Transactional uses AOP proxies around methods to manage database transactions. When an exception occurs, it automatically rolls back on unchecked RuntimeExceptions by default. Propagation REQUIRED joins an existing transaction or starts a new one, while REQUIRES_NEW suspends the current transaction and executes in an isolated new transaction."
        );
        AnswerEvaluationDto eval2 = interviewService.submitAnswer(ans2);

        assertThat(eval2).isNotNull();
        assertThat(eval2.getQuestionNumber()).isEqualTo(2);
        assertThat(eval2.getScore()).isGreaterThanOrEqualTo(50);
        assertThat(eval2.isCompleted()).isTrue();
        assertThat(eval2.getSessionSummary()).isNotNull();
        assertThat(eval2.getSessionSummary().getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(eval2.getSessionSummary().getConsolidatedFeedback()).isNotBlank();
        assertThat(eval2.getSessionSummary().getAverageScore()).isGreaterThan(0.0);

        // 4. Retrieve Completed Session
        InterviewSessionResponse completedSession = interviewService.getSession(sessionId);
        assertThat(completedSession.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(completedSession.getQuestions()).hasSize(2);

        // 5. Check Summary for Student (SRS Figure B-6)
        InterviewSummaryResponse summary = interviewService.getStudentInterviewSummary(101L);
        assertThat(summary.getStudentId()).isEqualTo(101L);
        assertThat(summary.getCompletedSessions()).isGreaterThanOrEqualTo(1);
        assertThat(summary.getAverageTechnicalScore()).isGreaterThan(0.0);
    }

    @Test
    void testEarlyExitSessionMarkedIncomplete() {
        // REQ-INT-14: If the student exits early, mark as incomplete
        InterviewStartRequest startReq = new InterviewStartRequest();
        startReq.setStudentId(102L);
        startReq.setInterviewType(InterviewType.HR);
        startReq.setTargetRole("Frontend Developer");
        startReq.setMaxQuestions(3);

        InterviewSessionResponse session = interviewService.startSession(startReq);
        String sessionId = session.getSessionId();

        // Exit session midway
        InterviewSessionResponse ended = interviewService.endSession(sessionId, true);
        assertThat(ended.getStatus()).isEqualTo(SessionStatus.INCOMPLETE);

        InterviewSessionResponse fetched = interviewService.getSession(sessionId);
        assertThat(fetched.getStatus()).isEqualTo(SessionStatus.INCOMPLETE);
    }

    @Test
    void testReconnectSession() {
        // REQ-INT-17: Session reconnection within window
        InterviewStartRequest startReq = new InterviewStartRequest();
        startReq.setStudentId(103L);
        startReq.setInterviewType(InterviewType.TECHNICAL);
        startReq.setMaxQuestions(3);

        InterviewSessionResponse session = interviewService.startSession(startReq);
        String sessionId = session.getSessionId();

        InterviewSessionResponse reconnected = interviewService.reconnectSession(sessionId);
        assertThat(reconnected.getSessionId()).isEqualTo(sessionId);
        assertThat(reconnected.getStatus()).isEqualTo(SessionStatus.IN_PROGRESS);
        assertThat(reconnected.getCurrentQuestionNumber()).isEqualTo(1);
    }

    @Test
    void testRestEndpoints() throws Exception {
        // Test health endpoint
        mockMvc.perform(get("/api/interviews/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("InterviewService"));

        // Test start session via REST
        InterviewStartRequest startReq = new InterviewStartRequest();
        startReq.setStudentId(104L);
        startReq.setInterviewType(InterviewType.TECHNICAL);
        startReq.setInputMode(InputMode.TEXT);
        startReq.setTargetRole("DevOps Engineer");
        startReq.setMaxQuestions(2);

        String startJson = objectMapper.writeValueAsString(startReq);

        String responseBody = mockMvc.perform(post("/api/interviews/sessions/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(startJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId").isNotEmpty())
                .andExpect(jsonPath("$.currentQuestionNumber").value(1))
                .andReturn().getResponse().getContentAsString();

        InterviewSessionResponse sessionResp = objectMapper.readValue(responseBody, InterviewSessionResponse.class);
        String sessionId = sessionResp.getSessionId();

        // Test submit answer via REST
        String answerJson = objectMapper.writeValueAsString(Map.of("answer", "Docker packages applications and dependencies into lightweight containers, using kernel namespaces for isolation."));

        mockMvc.perform(post("/api/interviews/sessions/" + sessionId + "/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answerJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionNumber").value(1))
                .andExpect(jsonPath("$.score").isNumber())
                .andExpect(jsonPath("$.feedback").isNotEmpty());

        // Test student summary endpoint (Figure B-6)
        mockMvc.perform(get("/api/interviews/student/104/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(104));
    }
}
