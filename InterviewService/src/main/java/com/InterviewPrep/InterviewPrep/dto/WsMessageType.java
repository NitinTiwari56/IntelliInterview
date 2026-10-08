package com.InterviewPrep.InterviewPrep.dto;

public enum WsMessageType {
    // Client to Server
    START_SESSION,
    ANSWER,
    RECONNECT,
    END_SESSION,
    PING,

    // Server to Client
    SESSION_STARTED,
    QUESTION_CHUNK,
    QUESTION_COMPLETE,
    EVALUATION,
    SESSION_SUMMARY,
    SESSION_INCOMPLETE,
    RECONNECTED,
    ERROR,
    PONG
}
