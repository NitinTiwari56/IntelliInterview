package com.nitin.AptituteQuestionsService.service;

import com.nitin.AptituteQuestionsService.dto.QuestionBatchResponse;
import com.nitin.AptituteQuestionsService.dto.QuestionRequest;
import java.util.Map;

public interface AptitudeQuestionService {

    QuestionBatchResponse generateQuestions(QuestionRequest request);

    Map<String, Object> getServiceHealthAndStats();

    Map<String, Object> getAvailableCategories();
}
