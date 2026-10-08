package com.InterviewPrep.InterviewPrep.service;

import com.InterviewPrep.InterviewPrep.dto.*;
import com.InterviewPrep.InterviewPrep.exception.InvalidInterviewSessionException;
import com.InterviewPrep.InterviewPrep.exception.ResourceNotFoundException;
import com.InterviewPrep.InterviewPrep.model.*;
import com.InterviewPrep.InterviewPrep.repository.InterviewRepository;
import com.InterviewPrep.InterviewPrep.repository.InterviewSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InterviewServiceImpl implements InterviewService {

    private static final Logger log = LoggerFactory.getLogger(InterviewServiceImpl.class);

    private final InterviewRepository interviewRepository;
    private final InterviewSessionRepository sessionRepository;
    private final AiInterviewEngineService aiEngine;
    private final InterviewEventPublisher eventPublisher;

    private final Map<String, InterviewSessionState> activeSessions = new ConcurrentHashMap<>();

    @Value("${interview.session.timeout-minutes:15}")
    private int sessionTimeoutMinutes;

    @Value("${interview.default.max-questions:5}")
    private int defaultMaxQuestions;

    public InterviewServiceImpl(InterviewRepository interviewRepository,
                                InterviewSessionRepository sessionRepository,
                                AiInterviewEngineService aiEngine,
                                InterviewEventPublisher eventPublisher) {
        this.interviewRepository = interviewRepository;
        this.sessionRepository = sessionRepository;
        this.aiEngine = aiEngine;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public InterviewSessionResponse startSession(InterviewStartRequest request) {
        String sessionId = UUID.randomUUID().toString();
        int maxQ = request.getMaxQuestions() != null && request.getMaxQuestions() > 0 ?
                request.getMaxQuestions() : defaultMaxQuestions;

        // 1. Persist initial Session entity
        InterviewSessionEntity sessionEntity = new InterviewSessionEntity();
        sessionEntity.setSessionId(sessionId);
        sessionEntity.setStudentId(request.getStudentId());
        sessionEntity.setInterviewType(request.getInterviewType() != null ? request.getInterviewType() : InterviewType.TECHNICAL);
        sessionEntity.setInputMode(request.getInputMode() != null ? request.getInputMode() : InputMode.TEXT);
        sessionEntity.setTargetRole(request.getTargetRole() != null ? request.getTargetRole() : "Software Development Engineer");
        sessionEntity.setResumeSummary(request.getResumeText());
        sessionEntity.setStatus(SessionStatus.IN_PROGRESS);
        sessionEntity.setMaxQuestions(maxQ);
        sessionEntity.setQuestionCount(0);
        sessionEntity.setStartedAt(Instant.now());
        sessionEntity.setLastActiveAt(Instant.now());
        sessionRepository.save(sessionEntity);

        // 2. Initialize in-memory state
        InterviewSessionState state = new InterviewSessionState(
                sessionId,
                request.getStudentId(),
                sessionEntity.getInterviewType(),
                sessionEntity.getInputMode(),
                sessionEntity.getTargetRole(),
                request.getResumeText(),
                request.getResumeSkills(),
                maxQ
        );

        // 3. Generate Question 1
        String firstQuestion = aiEngine.generateQuestion(state);
        state.setCurrentQuestionNumber(1);
        state.setCurrentQuestion(firstQuestion);

        activeSessions.put(sessionId, state);
        log.info("Started new interview session {} for student {}. Role: '{}', Type: {}",
                sessionId, request.getStudentId(), state.getTargetRole(), state.getInterviewType());

        return mapToSessionResponse(sessionEntity, state);
    }

    @Override
    public Flux<String> streamQuestion(String sessionId, int questionNumber) {
        InterviewSessionState state = getActiveSessionState(sessionId);
        if (state == null) {
            return Flux.error(new ResourceNotFoundException("Active interview session not found: " + sessionId));
        }
        state.touch();
        return aiEngine.generateQuestionStream(state);
    }

    @Override
    @Transactional
    public AnswerEvaluationDto submitAnswer(InterviewAnswerRequest request) {
        String sessionId = request.getSessionId();
        InterviewSessionState state = getOrRehydrateSessionState(sessionId);

        if (state.isCompleted()) {
            throw new InvalidInterviewSessionException("Interview session " + sessionId + " is already completed.");
        }

        int currentQNum = state.getCurrentQuestionNumber();
        String currentQ = state.getCurrentQuestion();
        String candidateAnswer = request.getAnswer();

        log.info("Evaluating answer for session {} | Question {}: '{}'", sessionId, currentQNum, currentQ);

        // 1. Evaluate with AI / Rule-based engine
        AnswerEvaluationDto evaluation = aiEngine.evaluateAnswer(state, currentQ, candidateAnswer);
        evaluation.setSessionId(sessionId);
        evaluation.setQuestionNumber(currentQNum);
        evaluation.setQuestion(currentQ);
        evaluation.setAnswer(candidateAnswer);

        // 2. Persist to interviews table (SRS Table 6-1 / REQ-INT-12)
        InterviewEntity interviewRecord = new InterviewEntity();
        interviewRecord.setSessionId(sessionId);
        interviewRecord.setStudentId(state.getStudentId());
        interviewRecord.setType(state.getInterviewType().name());
        interviewRecord.setQuestionNumber(currentQNum);
        interviewRecord.setQuestion(currentQ);
        interviewRecord.setAnswer(candidateAnswer);
        interviewRecord.setScore(evaluation.getScore());
        interviewRecord.setFeedback(evaluation.getFeedback());
        interviewRecord.setStrengths(evaluation.getStrengths());
        interviewRecord.setImprovements(evaluation.getImprovements());
        interviewRepository.save(interviewRecord);

        // 3. Update in-memory state
        state.recordAnswer(currentQNum, candidateAnswer, evaluation.getScore(), evaluation.getFeedback(),
                evaluation.getStrengths(), evaluation.getImprovements());

        // 4. Check if session has reached max questions
        boolean isFinished = (currentQNum >= state.getMaxQuestions());

        if (isFinished) {
            evaluation.setCompleted(true);
            evaluation.setNextQuestionNumber(null);
            evaluation.setNextQuestion(null);

            // Finalize session
            InterviewSessionSummaryDto summary = finalizeSession(sessionId, state, SessionStatus.COMPLETED);
            evaluation.setSessionSummary(summary);
        } else {
            evaluation.setCompleted(false);
            int nextQNum = currentQNum + 1;
            state.setCurrentQuestionNumber(nextQNum);

            // Generate next question
            String nextQuestion = aiEngine.generateQuestion(state);
            state.setCurrentQuestion(nextQuestion);

            evaluation.setNextQuestionNumber(nextQNum);
            evaluation.setNextQuestion(nextQuestion);
        }

        // Update session last active time
        sessionRepository.findById(sessionId).ifPresent(s -> {
            s.setLastActiveAt(Instant.now());
            s.setQuestionCount(currentQNum);
            sessionRepository.save(s);
        });

        return evaluation;
    }

    private InterviewSessionSummaryDto finalizeSession(String sessionId, InterviewSessionState state, SessionStatus status) {
        state.setCompleted(true);

        List<InterviewEntity> allQuestions = interviewRepository.findBySessionIdOrderByQuestionNumberAsc(sessionId);
        int totalScore = 0;
        int count = allQuestions.size();
        List<String> combinedStrengths = new ArrayList<>();
        List<String> combinedWeaknesses = new ArrayList<>();

        for (InterviewEntity q : allQuestions) {
            if (q.getScore() != null) {
                totalScore += q.getScore();
            }
            if (q.getStrengths() != null) {
                combinedStrengths.addAll(q.getStrengths());
            }
            if (q.getImprovements() != null) {
                combinedWeaknesses.addAll(q.getImprovements());
            }
        }

        double avgScore = count > 0 ? (double) totalScore / count : 0.0;
        String consolidatedFeedback = aiEngine.generateConsolidatedFeedback(state, avgScore, count);

        // Update database session entity
        InterviewSessionEntity entity = sessionRepository.findById(sessionId)
                .orElse(new InterviewSessionEntity());
        entity.setStatus(status);
        entity.setQuestionCount(count);
        entity.setTotalScore(totalScore);
        entity.setAverageScore(Math.round(avgScore * 10.0) / 10.0);
        entity.setConsolidatedFeedback(consolidatedFeedback);
        entity.setCompletedAt(Instant.now());
        entity.setLastActiveAt(Instant.now());
        sessionRepository.save(entity);

        InterviewSessionSummaryDto summaryDto = new InterviewSessionSummaryDto();
        summaryDto.setSessionId(sessionId);
        summaryDto.setStudentId(state.getStudentId());
        summaryDto.setInterviewType(state.getInterviewType().name());
        summaryDto.setTargetRole(state.getTargetRole());
        summaryDto.setStatus(status);
        summaryDto.setTotalScore(totalScore);
        summaryDto.setAverageScore(entity.getAverageScore());
        summaryDto.setQuestionCount(count);
        summaryDto.setConsolidatedFeedback(consolidatedFeedback);
        summaryDto.setStrengthsSummary(combinedStrengths.stream().distinct().limit(4).toList());
        summaryDto.setAreasForImprovement(combinedWeaknesses.stream().distinct().limit(4).toList());
        summaryDto.setCompletedAt(entity.getCompletedAt());

        // Publish InterviewCompleted event (SRS REQ-INT-13 & Table 3-4)
        if (status == SessionStatus.COMPLETED) {
            InterviewCompletedEvent event = new InterviewCompletedEvent(
                    sessionId,
                    state.getStudentId(),
                    state.getInterviewType().name(),
                    state.getTargetRole(),
                    totalScore,
                    entity.getAverageScore(),
                    count
            );
            event.setIdentifiedStrengths(summaryDto.getStrengthsSummary());
            event.setIdentifiedWeakAreas(summaryDto.getAreasForImprovement());
            eventPublisher.publishInterviewCompletedEvent(event);
        }

        return summaryDto;
    }

    @Override
    @Transactional
    public InterviewSessionResponse endSession(String sessionId, boolean markAsIncomplete) {
        InterviewSessionEntity sessionEntity = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview session not found: " + sessionId));

        InterviewSessionState state = activeSessions.get(sessionId);
        SessionStatus finalStatus = markAsIncomplete ? SessionStatus.INCOMPLETE : SessionStatus.COMPLETED;

        if (state != null) {
            finalizeSession(sessionId, state, finalStatus);
            activeSessions.remove(sessionId);
        } else {
            sessionEntity.setStatus(finalStatus);
            sessionEntity.setCompletedAt(Instant.now());
            sessionRepository.save(sessionEntity);
        }

        InterviewSessionEntity updated = sessionRepository.findById(sessionId).orElse(sessionEntity);
        return mapToSessionResponse(updated, state);
    }

    @Override
    public InterviewSessionResponse getSession(String sessionId) {
        InterviewSessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview session not found: " + sessionId));

        InterviewSessionState state = activeSessions.get(sessionId);
        return mapToSessionResponse(session, state);
    }

    @Override
    public InterviewSessionResponse reconnectSession(String sessionId) {
        InterviewSessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview session not found: " + sessionId));

        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new InvalidInterviewSessionException("Cannot reconnect. Session " + sessionId + " is already " + session.getStatus());
        }

        InterviewSessionState state = getOrRehydrateSessionState(sessionId);
        state.touch();
        log.info("Reconnected session {} for student {}. Current question: {}", sessionId, state.getStudentId(), state.getCurrentQuestionNumber());

        return mapToSessionResponse(session, state);
    }

    @Override
    public List<InterviewSessionResponse> getStudentSessions(Long studentId) {
        List<InterviewSessionEntity> sessions = sessionRepository.findByStudentIdOrderByStartedAtDesc(studentId);
        List<InterviewSessionResponse> result = new ArrayList<>();
        for (InterviewSessionEntity s : sessions) {
            InterviewSessionState activeState = activeSessions.get(s.getSessionId());
            result.add(mapToSessionResponse(s, activeState));
        }
        return result;
    }

    @Override
    public InterviewSummaryResponse getStudentInterviewSummary(Long studentId) {
        List<InterviewSessionEntity> sessions = sessionRepository.findByStudentIdOrderByStartedAtDesc(studentId);

        InterviewSummaryResponse summary = new InterviewSummaryResponse();
        summary.setStudentId(studentId);
        summary.setTotalSessions(sessions.size());

        long completedCount = 0;
        long totalQuestions = 0;
        double techTotalAvg = 0;
        int techCount = 0;
        double hrTotalAvg = 0;
        int hrCount = 0;

        List<InterviewSessionSummaryDto> recentList = new ArrayList<>();

        for (InterviewSessionEntity s : sessions) {
            if (s.getStatus() == SessionStatus.COMPLETED) {
                completedCount++;
                if (s.getAverageScore() != null) {
                    if (s.getInterviewType() == InterviewType.TECHNICAL) {
                        techTotalAvg += s.getAverageScore();
                        techCount++;
                    } else if (s.getInterviewType() == InterviewType.HR) {
                        hrTotalAvg += s.getAverageScore();
                        hrCount++;
                    }
                }
            }
            if (s.getQuestionCount() != null) {
                totalQuestions += s.getQuestionCount();
            }

            if (recentList.size() < 5) {
                InterviewSessionSummaryDto item = new InterviewSessionSummaryDto();
                item.setSessionId(s.getSessionId());
                item.setStudentId(s.getStudentId());
                item.setInterviewType(s.getInterviewType() != null ? s.getInterviewType().name() : null);
                item.setTargetRole(s.getTargetRole());
                item.setStatus(s.getStatus());
                item.setTotalScore(s.getTotalScore());
                item.setAverageScore(s.getAverageScore());
                item.setQuestionCount(s.getQuestionCount());
                item.setConsolidatedFeedback(s.getConsolidatedFeedback());
                item.setCompletedAt(s.getCompletedAt());
                recentList.add(item);
            }
        }

        summary.setCompletedSessions(completedCount);
        summary.setTotalQuestionsAnswered(totalQuestions);

        if (techCount > 0) {
            summary.setAverageTechnicalScore(Math.round((techTotalAvg / techCount) * 10.0) / 10.0);
        }
        if (hrCount > 0) {
            summary.setAverageHrScore(Math.round((hrTotalAvg / hrCount) * 10.0) / 10.0);
        }

        int totalCount = techCount + hrCount;
        if (totalCount > 0) {
            summary.setOverallAverageScore(Math.round(((techTotalAvg + hrTotalAvg) / totalCount) * 10.0) / 10.0);
        }

        summary.setRecentSessions(recentList);
        return summary;
    }

    @Override
    public InterviewSessionState getActiveSessionState(String sessionId) {
        return activeSessions.get(sessionId);
    }

    @Override
    public void registerWebSocketSession(String sessionId, WebSocketSession wsSession) {
        InterviewSessionState state = activeSessions.get(sessionId);
        if (state != null) {
            state.setWebSocketSession(wsSession);
            state.touch();
        }
    }

    @Override
    public void unregisterWebSocketSession(String sessionId) {
        InterviewSessionState state = activeSessions.get(sessionId);
        if (state != null) {
            state.setWebSocketSession(null);
            state.touch();
        }
    }

    private InterviewSessionState getOrRehydrateSessionState(String sessionId) {
        InterviewSessionState state = activeSessions.get(sessionId);
        if (state != null) {
            return state;
        }

        // Rehydrate from DB if within timeout
        InterviewSessionEntity sessionEntity = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview session not found: " + sessionId));

        Instant lastActive = sessionEntity.getLastActiveAt() != null ? sessionEntity.getLastActiveAt() : sessionEntity.getStartedAt();
        if (Duration.between(lastActive, Instant.now()).toMinutes() > sessionTimeoutMinutes) {
            throw new InvalidInterviewSessionException("Session " + sessionId + " expired due to inactivity.");
        }

        List<InterviewEntity> history = interviewRepository.findBySessionIdOrderByQuestionNumberAsc(sessionId);
        InterviewSessionState rehydrated = new InterviewSessionState(
                sessionId,
                sessionEntity.getStudentId(),
                sessionEntity.getInterviewType(),
                sessionEntity.getInputMode(),
                sessionEntity.getTargetRole(),
                sessionEntity.getResumeSummary(),
                new ArrayList<>(),
                sessionEntity.getMaxQuestions() != null ? sessionEntity.getMaxQuestions() : defaultMaxQuestions
        );

        for (InterviewEntity q : history) {
            rehydrated.recordAnswer(q.getQuestionNumber(), q.getAnswer(), q.getScore(), q.getFeedback(),
                    q.getStrengths(), q.getImprovements());
        }

        int nextQ = history.size() + 1;
        rehydrated.setCurrentQuestionNumber(nextQ);
        if (nextQ <= rehydrated.getMaxQuestions()) {
            rehydrated.setCurrentQuestion(aiEngine.generateQuestion(rehydrated));
        }

        activeSessions.put(sessionId, rehydrated);
        return rehydrated;
    }

    private InterviewSessionResponse mapToSessionResponse(InterviewSessionEntity entity, InterviewSessionState state) {
        InterviewSessionResponse response = new InterviewSessionResponse();
        response.setSessionId(entity.getSessionId());
        response.setStudentId(entity.getStudentId());
        response.setInterviewType(entity.getInterviewType());
        response.setInputMode(entity.getInputMode());
        response.setTargetRole(entity.getTargetRole());
        response.setStatus(entity.getStatus());
        response.setMaxQuestions(entity.getMaxQuestions());
        response.setQuestionCount(entity.getQuestionCount());
        response.setTotalScore(entity.getTotalScore());
        response.setAverageScore(entity.getAverageScore());
        response.setConsolidatedFeedback(entity.getConsolidatedFeedback());
        response.setStartedAt(entity.getStartedAt());
        response.setCompletedAt(entity.getCompletedAt());

        if (state != null) {
            response.setCurrentQuestionNumber(state.getCurrentQuestionNumber());
            response.setCurrentQuestion(state.getCurrentQuestion());
        }

        List<InterviewEntity> questions = interviewRepository.findBySessionIdOrderByQuestionNumberAsc(entity.getSessionId());
        List<InterviewQuestionItemDto> qDtos = new ArrayList<>();
        for (InterviewEntity q : questions) {
            InterviewQuestionItemDto item = new InterviewQuestionItemDto();
            item.setInterviewId(q.getInterviewId());
            item.setQuestionNumber(q.getQuestionNumber());
            item.setQuestion(q.getQuestion());
            item.setAnswer(q.getAnswer());
            item.setScore(q.getScore());
            item.setFeedback(q.getFeedback());
            item.setStrengths(q.getStrengths());
            item.setImprovements(q.getImprovements());
            item.setCreatedAt(q.getCreatedAt());
            qDtos.add(item);
        }
        response.setQuestions(qDtos);

        return response;
    }
}
