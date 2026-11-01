package com.interviewiq.aptitude.service;

import java.util.List;

/** Memory mein rehne wala question (correct answer ke saath). Kabhi frontend ko poora nahi jata. */
record SessionQuestion(String category, String topic, String question, List<String> options,
                       int correctIndex, String explanation, int marks) {}
