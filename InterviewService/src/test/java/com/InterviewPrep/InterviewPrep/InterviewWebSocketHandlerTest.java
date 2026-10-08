package com.InterviewPrep.InterviewPrep;

import com.InterviewPrep.InterviewPrep.dto.WsMessage;
import com.InterviewPrep.InterviewPrep.dto.WsMessageType;
import com.InterviewPrep.InterviewPrep.websocket.InterviewWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class InterviewWebSocketHandlerTest {

    @Autowired
    private InterviewWebSocketHandler webSocketHandler;

    @Autowired
    private ObjectMapper objectMapper;

    private WebSocketSession mockSession;

    @BeforeEach
    void setUp() {
        mockSession = mock(WebSocketSession.class);
        when(mockSession.getId()).thenReturn("ws-test-session-123");
        when(mockSession.isOpen()).thenReturn(true);
    }

    @Test
    void testPingPong() throws Exception {
        WsMessage ping = WsMessage.of(WsMessageType.PING);
        String pingJson = objectMapper.writeValueAsString(ping);

        webSocketHandler.afterConnectionEstablished(mockSession);
        webSocketHandler.handleMessage(mockSession, new TextMessage(pingJson));

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(mockSession).sendMessage(captor.capture());

        WsMessage reply = objectMapper.readValue(captor.getValue().getPayload(), WsMessage.class);
        assertThat(reply.getType()).isEqualTo(WsMessageType.PONG);
    }

    @Test
    void testStartSessionAndAnswerFlowOverWebSocket() throws Exception {
        webSocketHandler.afterConnectionEstablished(mockSession);

        // 1. Send START_SESSION
        WsMessage startMsg = WsMessage.of(WsMessageType.START_SESSION);
        startMsg.setStudentId(200L);
        startMsg.setData(Map.of(
                "interviewType", "TECHNICAL",
                "inputMode", "TEXT",
                "targetRole", "Java Backend Developer",
                "maxQuestions", 1
        ));

        String startJson = objectMapper.writeValueAsString(startMsg);
        webSocketHandler.handleMessage(mockSession, new TextMessage(startJson));

        // Verify messages were sent to socket (SESSION_STARTED, QUESTION_CHUNKs, QUESTION_COMPLETE)
        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(mockSession, atLeast(2)).sendMessage(captor.capture());

        List<TextMessage> sentMessages = captor.getAllValues();
        assertThat(sentMessages).isNotEmpty();

        // Check first message is SESSION_STARTED
        WsMessage firstMsg = objectMapper.readValue(sentMessages.get(0).getPayload(), WsMessage.class);
        assertThat(firstMsg.getType()).isEqualTo(WsMessageType.SESSION_STARTED);
        assertThat(firstMsg.getSessionId()).isNotBlank();
        String sessionId = firstMsg.getSessionId();

        // 2. Submit ANSWER over socket
        reset(mockSession);
        when(mockSession.getId()).thenReturn("ws-test-session-123");
        when(mockSession.isOpen()).thenReturn(true);

        WsMessage answerMsg = WsMessage.of(WsMessageType.ANSWER);
        answerMsg.setSessionId(sessionId);
        answerMsg.setContent("HashMap uses an array of linked list buckets with key hashing. In Java 8, long buckets become balanced trees.");

        String answerJson = objectMapper.writeValueAsString(answerMsg);
        webSocketHandler.handleMessage(mockSession, new TextMessage(answerJson));

        ArgumentCaptor<TextMessage> answerCaptor = ArgumentCaptor.forClass(TextMessage.class);
        verify(mockSession, atLeast(1)).sendMessage(answerCaptor.capture());

        // Check evaluation was sent back
        boolean hasEvaluation = answerCaptor.getAllValues().stream().anyMatch(tm -> {
            try {
                WsMessage m = objectMapper.readValue(tm.getPayload(), WsMessage.class);
                return m.getType() == WsMessageType.EVALUATION;
            } catch (Exception e) {
                return false;
            }
        });
        assertThat(hasEvaluation).isTrue();
    }
}
