package com.interviewiq.aptitude.service;

import com.interviewiq.aptitude.dto.AptitudeDtos.ResultResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Ek chalta hua test (memory mein). Sab fields ko access karte waqt `synchronized (test)` use karo.
 */
final class ActiveTest {

    final String id = UUID.randomUUID().toString();
    final Long userId;
    final Instant startedAt;
    final List<SessionQuestion> questions;
    final Integer[] selected;         // har question ka chuna hua option (null = skip)
    final boolean[] correct;

    int current = 0;                  // abhi kaunsa question (0-based)
    Instant currentStartedAt;         // is question ka timer kab shuru hua
    int score = 0;                    // marks counter

    volatile boolean finished = false;
    Instant finishedAt;
    ResultResponse finalResult;

    ActiveTest(Long userId, List<SessionQuestion> questions, Instant now) {
        this.userId = userId;
        this.questions = questions;
        this.startedAt = now;
        this.currentStartedAt = now;
        this.selected = new Integer[questions.size()];
        this.correct = new boolean[questions.size()];
    }
}
