package com.InterviewPrep.InterviewPrep.websocket;

import com.InterviewPrep.InterviewPrep.dto.*;
import com.InterviewPrep.InterviewPrep.model.InputMode;
import com.InterviewPrep.InterviewPrep.model.InterviewType;
import com.InterviewPrep.InterviewPrep.service.InterviewService;
import com.InterviewPrep.InterviewPrep.service.InterviewSessionState;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REQ-INT-2: Authenticated WebSocket session keeping per-session state.
 * REQ-INT-4: Real-time question token streaming.
 * COM-4: Exchanging question chunk, answer, evaluation/feedback, session end.
 */
@Component
public class InterviewWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(InterviewWebSocketHandler.class);

    private final InterviewService interviewService;
    private final ObjectMapper objectMapper;

    // Track active socket to sessionId mapping
    private final Map<String, String> sessionSocketMap = new ConcurrentHashMap<>();

    public InterviewWebSocketHandler(InterviewService interviewService, ObjectMapper objectMapper) {
        this.interviewService = interviewService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("[WS CONNECTED] WebSocket connection established. Socket ID: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        try {
            WsMessage incoming = objectMapper.readValue(payload, WsMessage.class);
            if (incoming.getType() == null) {
                sendError(session, null, "Message type is required.");
                return;
            }

            switch (incoming.getType()) {
                case START_SESSION -> handleStartSession(session, incoming);
                case ANSWER -> handleAnswer(session, incoming);
                case RECONNECT -> handleReconnect(session, incoming);
                case END_SESSION -> handleEndSession(session, incoming);
                case PING -> sendMessage(session, WsMessage.of(WsMessageType.PONG));
                default -> log.debug("Unhandled incoming message type: {}", incoming.getType());
            }
        } catch (Exception ex) {
            log.error("Error processing WebSocket message: {}", ex.getMessage(), ex);
            sendError(session, null, "Failed to parse or process message: " + ex.getMessage());
        }
    }

    private void handleStartSession(WebSocketSession session, WsMessage msg) {
        try {
            Long studentId = msg.getStudentId();
            if (studentId == null) {
                sendError(session, null, "studentId is required to start an interview session.");
                return;
            }

            InterviewStartRequest req = new InterviewStartRequest();
            req.setStudentId(studentId);

            // Parse optional fields from msg.getData() or root msg
            if (msg.getData() instanceof Map<?, ?> dataMap) {
                if (dataMap.containsKey("interviewType")) {
                    req.setInterviewType(InterviewType.valueOf(dataMap.get("interviewType").toString().toUpperCase()));
                }
                if (dataMap.containsKey("inputMode")) {
                    req.setInputMode(InputMode.valueOf(dataMap.get("inputMode").toString().toUpperCase()));
                }
                if (dataMap.containsKey("targetRole")) {
                    req.setTargetRole(dataMap.get("targetRole").toString());
                }
                if (dataMap.containsKey("resumeText")) {
                    req.setResumeText(dataMap.get("resumeText").toString());
                }
                if (dataMap.containsKey("maxQuestions")) {
                    req.setMaxQuestions(Integer.parseInt(dataMap.get("maxQuestions").toString()));
                }
            }

            InterviewSessionResponse sessionResp = interviewService.startSession(req);
            String sessionId = sessionResp.getSessionId();

            sessionSocketMap.put(session.getId(), sessionId);
            interviewService.registerWebSocketSession(sessionId, session);

            // 1. Send SESSION_STARTED notification
            WsMessage startedMsg = WsMessage.of(WsMessageType.SESSION_STARTED);
            startedMsg.setSessionId(sessionId);
            startedMsg.setStudentId(studentId);
            startedMsg.setQuestionNumber(1);
            startedMsg.setData(sessionResp);
            sendMessage(session, startedMsg);

            // 2. Stream Question 1 tokens (REQ-INT-4)
            streamQuestionToClient(session, sessionId, 1);

        } catch (Exception ex) {
            log.error("Failed to start session via WebSocket: {}", ex.getMessage(), ex);
            sendError(session, null, "Failed to start interview session: " + ex.getMessage());
        }
    }

    private void handleAnswer(WebSocketSession session, WsMessage msg) {
        String sessionId = msg.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = sessionSocketMap.get(session.getId());
        }

        if (sessionId == null) {
            sendError(session, null, "Missing sessionId for answer submission.");
            return;
        }

        String answerText = msg.getAnswer() != null ? msg.getAnswer() : msg.getContent();
        if (answerText == null || answerText.isBlank()) {
            sendError(session, sessionId, "Answer cannot be blank.");
            return;
        }

        try {
            InterviewAnswerRequest answerReq = new InterviewAnswerRequest(sessionId, answerText);
            AnswerEvaluationDto evaluation = interviewService.submitAnswer(answerReq);

            // 1. Send EVALUATION message
            WsMessage evalMsg = WsMessage.evaluation(
                    sessionId,
                    evaluation.getQuestionNumber(),
                    evaluation.getScore(),
                    evaluation.getFeedback(),
                    evaluation.getStrengths(),
                    evaluation.getImprovements()
            );
            sendMessage(session, evalMsg);

            // 2. Check if finished
            if (evaluation.isCompleted()) {
                WsMessage summaryMsg = WsMessage.sessionSummary(sessionId, evaluation.getSessionSummary());
                sendMessage(session, summaryMsg);
                log.info("Interview session {} completed successfully via WebSocket.", sessionId);
            } else {
                // 3. Stream next question tokens
                int nextQNum = evaluation.getNextQuestionNumber();
                streamQuestionToClient(session, sessionId, nextQNum);
            }

        } catch (Exception ex) {
            log.error("Failed to process answer for session {}: {}", sessionId, ex.getMessage(), ex);
            sendError(session, sessionId, "Failed to evaluate answer: " + ex.getMessage());
        }
    }

    private void handleReconnect(WebSocketSession session, WsMessage msg) {
        String sessionId = msg.getSessionId();
        if (sessionId == null) {
            sendError(session, null, "SessionId required for reconnection.");
            return;
        }

        try {
            InterviewSessionResponse sessionResp = interviewService.reconnectSession(sessionId);
            sessionSocketMap.put(session.getId(), sessionId);
            interviewService.registerWebSocketSession(sessionId, session);

            WsMessage reconnMsg = WsMessage.of(WsMessageType.RECONNECTED);
            reconnMsg.setSessionId(sessionId);
            reconnMsg.setData(sessionResp);
            reconnMsg.setQuestionNumber(sessionResp.getCurrentQuestionNumber());
            reconnMsg.setQuestion(sessionResp.getCurrentQuestion());
            sendMessage(session, reconnMsg);

        } catch (Exception ex) {
            log.warn("Reconnection failed for session {}: {}", sessionId, ex.getMessage());
            sendError(session, sessionId, "Reconnection failed: " + ex.getMessage());
        }
    }

    private void handleEndSession(WebSocketSession session, WsMessage msg) {
        String sessionId = msg.getSessionId();
        if (sessionId == null) {
            sessionId = sessionSocketMap.get(session.getId());
        }

        if (sessionId != null) {
            InterviewSessionResponse ended = interviewService.endSession(sessionId, true);
            WsMessage endMsg = WsMessage.of(WsMessageType.SESSION_INCOMPLETE);
            endMsg.setSessionId(sessionId);
            endMsg.setContent("Interview ended early by user.");
            endMsg.setData(ended);
            sendMessage(session, endMsg);
        }
    }

    private void streamQuestionToClient(WebSocketSession session, String sessionId, int questionNumber) {
        InterviewSessionState state = interviewService.getActiveSessionState(sessionId);
        if (state == null) return;

        Flux<String> tokenFlux = interviewService.streamQuestion(sessionId, questionNumber);
        StringBuilder fullQuestionBuilder = new StringBuilder();

        tokenFlux.subscribe(
                chunk -> {
                    fullQuestionBuilder.append(chunk);
                    WsMessage chunkMsg = WsMessage.questionChunk(sessionId, questionNumber, chunk, false);
                    sendMessage(session, chunkMsg);
                },
                error -> {
                    log.error("Streaming error for session {}: {}", sessionId, error.getMessage());
                    sendError(session, sessionId, "Streaming question failed: " + error.getMessage());
                },
                () -> {
                    // Streaming finished, send QUESTION_COMPLETE
                    String fullQ = fullQuestionBuilder.toString().trim();
                    if (fullQ.isEmpty()) {
                        fullQ = state.getCurrentQuestion();
                    }
                    WsMessage completeMsg = WsMessage.questionComplete(sessionId, questionNumber, fullQ);
                    sendMessage(session, completeMsg);
                }
        );
    }

    private void sendError(WebSocketSession session, String sessionId, String errorMsg) {
        sendMessage(session, WsMessage.error(sessionId, errorMsg));
    }

    private void sendMessage(WebSocketSession session, WsMessage msg) {
        if (session == null || !session.isOpen()) return;
        try {
            synchronized (session) {
                String json = objectMapper.writeValueAsString(msg);
                session.sendMessage(new TextMessage(json));
            }
        } catch (IOException ex) {
            log.warn("Failed to send WebSocket message to {}: {}", session.getId(), ex.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = sessionSocketMap.remove(session.getId());
        if (sessionId != null) {
            interviewService.unregisterWebSocketSession(sessionId);
            log.info("[WS CLOSED] WebSocket session {} closed. Retaining session {} state for reconnect.",
                    session.getId(), sessionId);
        }
    }
}
