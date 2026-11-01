package com.interviewiq.coding.entity;

public enum Verdict {
    ACCEPTED,
    WRONG_ANSWER,
    COMPILATION_ERROR,
    RUNTIME_ERROR,
    TIME_LIMIT_EXCEEDED,
    INTERNAL_ERROR      // Judge0 ki dikkat; ye DB mein save nahi hota
}
