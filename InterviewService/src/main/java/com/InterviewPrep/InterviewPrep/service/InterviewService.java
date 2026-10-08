package com.InterviewPrep.InterviewPrep.service;

import com.InterviewPrep.InterviewPrep.dto.*;
import reactor.core.publisher.Flux;

import java.util.List;

public interface InterviewService {

    InterviewSessionResponse startSession(InterviewStartRequest request);

    Flux<String> streamQuestion(String sessionId, int questionNumber);

    AnswerEvaluationDto submitAnswer(InterviewAnswerRequest request);

    InterviewSessionResponse endSession(String sessionId, boolean markAsIncomplete);

    InterviewSessionResponse getSession(String sessionId);

    InterviewSessionResponse reconnectSession(String sessionId);

    List<InterviewSessionResponse> getStudentSessions(Long studentId);

    InterviewSummaryResponse getStudentInterviewSummary(Long studentId);

    InterviewSessionState getActiveSessionState(String sessionId);

    void registerWebSocketSession(String sessionId, org.springframework.web.socket.WebSocketSession wsSession);

    void unregisterWebSocketSession(String sessionId);
}
