package com.interviewiq.aptitudequestions.service;

import com.interviewiq.aptitudequestions.dto.QuestionBatchResponse;
import com.interviewiq.aptitudequestions.dto.QuestionRequest;
import java.util.Map;

public interface AptitudeQuestionService {

    QuestionBatchResponse generateQuestions(QuestionRequest request);

    Map<String, Object> getServiceHealthAndStats();

    Map<String, Object> getAvailableCategories();
}
