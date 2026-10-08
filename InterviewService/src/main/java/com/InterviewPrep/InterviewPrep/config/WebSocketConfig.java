package com.InterviewPrep.InterviewPrep.config;

import com.InterviewPrep.InterviewPrep.websocket.InterviewWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final InterviewWebSocketHandler interviewWebSocketHandler;

    public WebSocketConfig(InterviewWebSocketHandler interviewWebSocketHandler) {
        this.interviewWebSocketHandler = interviewWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // SRS COM-1, COM-4, Sequence Diagram B.5: /ws/interview WebSocket endpoint
        registry.addHandler(interviewWebSocketHandler, "/ws/interview")
                .setAllowedOrigins("*");
    }
}
