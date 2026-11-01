package com.interviewiq.aptitude.service;

import com.interviewiq.aptitude.client.QuestionServiceClient;
import com.interviewiq.aptitude.client.QuestionServiceClient.GeneratedQuestion;
import com.interviewiq.aptitude.client.QuestionServiceClient.GeneratedQuestionsResponse;
import com.interviewiq.aptitude.dto.AptitudeDtos.*;
import com.interviewiq.aptitude.entity.AptitudeResult;
import com.interviewiq.aptitude.entity.AptitudeResultCategory;
import com.interviewiq.aptitude.exception.ApiException;
import com.interviewiq.aptitude.repository.AptitudeResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Design:
 *  - Question service ka JSON memory mein (ActiveTest) rehta hai, DB mein NAHI.
 *  - Frontend ko ek time pe ek hi question jata hai (correct answer ke bina).
 *  - Har question ka apna 2 min ka timer, server pe enforce hota hai.
 *  - Sahi answer pe score (marks counter) badhta hai, memory mein.
 *  - Test khatam hone pe sirf chhota final result DB mein save hota hai.
 * Limit: sessions memory mein hain, toh service restart pe chal rahe tests chale jate hain (results nahi).
 *        Aur ye ek hi instance pe chalta hai. College project ke liye theek.
 */
@Service
public class AptitudeService {

    private static final Logger log = LoggerFactory.getLogger(AptitudeService.class);

    private final AptitudeResultRepository resultRepository;
    private final QuestionServiceClient questionClient;

    private final int questionCount;
    private final int secondsPerQuestion;
    private final int graceSeconds;
    private final int minValidQuestions;
    private final int idleTimeoutSeconds;
    private final int reviewTtlMinutes;

    /** userId -> uska chal raha (ya abhi abhi khatam hua) test. Ek user ka ek hi active test. */
    private final Map<Long, ActiveTest> sessions = new ConcurrentHashMap<>();

    public AptitudeService(AptitudeResultRepository resultRepository,
                           QuestionServiceClient questionClient,
                           @Value("${aptitude.question-count}") int questionCount,
                           @Value("${aptitude.seconds-per-question}") int secondsPerQuestion,
                           @Value("${aptitude.grace-seconds}") int graceSeconds,
                           @Value("${aptitude.min-valid-questions}") int minValidQuestions,
                           @Value("${aptitude.idle-timeout-seconds:300}") int idleTimeoutSeconds,
                           @Value("${aptitude.review-ttl-minutes:30}") int reviewTtlMinutes) {
        this.resultRepository = resultRepository;
        this.questionClient = questionClient;
        this.questionCount = questionCount;
        this.secondsPerQuestion = secondsPerQuestion;
        this.graceSeconds = graceSeconds;
        this.minValidQuestions = minValidQuestions;
        this.idleTimeoutSeconds = idleTimeoutSeconds;
        this.reviewTtlMinutes = reviewTtlMinutes;
    }

    // =====================================================================
    // START / CURRENT
    // =====================================================================

    /** Test shuru karo. Chal raha test hai toh wahi wapas (resume), naye questions nahi bante. */
    public TestStateResponse start(Long userId) {
        ActiveTest existing = sessions.get(userId);
        if (existing != null) {
            synchronized (existing) {
                if (!existing.finished) {
                    Instant now = Instant.now();
                    boolean timedOut = applyTimeout(existing, now);
                    if (!existing.finished) {
                        return state(existing, now, timedOut);
                    }
                }
            }
        }

        // naya test: question service ka call lock ke bahar (slow ho sakta hai)
        List<SessionQuestion> questions = fetchValidQuestions();
        ActiveTest created = new ActiveTest(userId, questions, Instant.now());

        ActiveTest winner = sessions.compute(userId, (k, current) ->
                (current != null && !current.finished) ? current : created);   // double-click pe do test nahi bante

        synchronized (winner) {
            return state(winner, Instant.now(), false);
        }
    }

    /** Page refresh pe: abhi ka question + bacha hua time. */
    public TestStateResponse current(Long userId) {
        ActiveTest t = sessions.get(userId);
        if (t == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "No active test");
        }
        synchronized (t) {
            if (t.finished) {
                throw new ApiException(HttpStatus.NOT_FOUND, "No active test");
            }
            Instant now = Instant.now();
            boolean timedOut = applyTimeout(t, now);
            return state(t, now, timedOut);
        }
    }

    // =====================================================================
    // ANSWER (ek-ek question ka jawab, loop yahin chalta hai)
    // =====================================================================

    /**
     * Frontend har question ke jawab pe (ya timer 0 hone pe selectedIndex=null ke saath) ye call karta hai.
     * Response mein agla question aata hai, ya aakhri question ke baad result.
     */
    public TestStateResponse answer(Long userId, String testId, AnswerRequest request) {
        ActiveTest t = sessions.get(userId);
        if (t == null || !t.id.equals(testId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Test not found");
        }
        synchronized (t) {
            if (t.finished) {
                throw new ApiException(HttpStatus.CONFLICT, "This test is already finished");
            }
            Instant now = Instant.now();

            // Is question ka time (grace ke saath) nikal chuka hai: late answer maanya nahi, skip hua
            if (applyTimeout(t, now)) {
                return state(t, now, true);
            }

            if (request.questionIndex() != t.current + 1) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "This is not the current question. Fetch the current state again.");
            }

            SessionQuestion q = t.questions.get(t.current);
            Integer selected = request.selectedIndex();
            if (selected != null && (selected < 0 || selected >= q.options().size())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid option selected");
            }

            t.selected[t.current] = selected;
            if (selected != null && selected == q.correctIndex()) {
                t.correct[t.current] = true;
                t.score += q.marks();                       // marks counter badha
            }

            advance(t, now);
            return state(t, now, false);
        }
    }

    // =====================================================================
    // RESULTS, HISTORY, STATS
    // =====================================================================

    /** Test ke turant baad review (correct answers + explanation). Kuch der baad memory se hat jata hai. */
    public ResultResponse reviewResult(Long userId, String testId) {
        ActiveTest t = sessions.get(userId);
        if (t == null || !t.id.equals(testId) || !t.finished || t.finalResult == null) {
            throw new ApiException(HttpStatus.NOT_FOUND,
                    "Detailed review is no longer available. See your results history.");
        }
        return t.finalResult;
    }

    @Transactional(readOnly = true)
    public List<HistoryItem> history(Long userId) {
        return resultRepository.findByUserIdOrderBySubmittedAtDesc(userId).stream()
                .map(r -> new HistoryItem(r.getId(), r.getScore(), r.getTotalMarks(),
                        percentage(r.getScore(), r.getTotalMarks()), r.getSubmittedAt()))
                .toList();
    }

    /** DB se summary (review ke bina). */
    @Transactional(readOnly = true)
    public ResultResponse savedResult(Long userId, Long resultId) {
        AptitudeResult r = resultRepository.findByIdAndUserId(resultId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Result not found"));
        List<CategoryScore> breakdown = r.getCategories().stream()
                .map(c -> new CategoryScore(c.getCategory(), c.getTotalQuestions(), c.getCorrectCount(),
                        percentage(c.getCorrectCount(), c.getTotalQuestions())))
                .toList();
        int incorrect = r.getTotalQuestions() - r.getCorrectCount() - r.getSkippedCount();
        return new ResultResponse(r.getId(), r.getScore(), r.getTotalMarks(),
                percentage(r.getScore(), r.getTotalMarks()), r.getTotalQuestions(), r.getCorrectCount(),
                incorrect, r.getSkippedCount(), r.isAutoEnded(), r.getSubmittedAt(), breakdown, null);
    }

    /** Saare attempts ka category-wise accuracy (dashboard / weak areas). */
    @Transactional(readOnly = true)
    public List<CategoryScore> stats(Long userId) {
        List<CategoryScore> stats = new ArrayList<>();
        for (Object[] row : resultRepository.categoryStats(userId)) {
            long total = ((Number) row[1]).longValue();
            long correct = row[2] == null ? 0 : ((Number) row[2]).longValue();
            stats.add(new CategoryScore((String) row[0], total, correct, percentage(correct, total)));
        }
        return stats;
    }

    /** Analytics ke liye. Koi result nahi = 404 (analytics "no data" maanta hai). */
    @Transactional(readOnly = true)
    public AptitudeSummary summary(Long userId) {
        Object[] t = resultRepository.totals(userId).get(0);
        long attempts = ((Number) t[2]).longValue();
        if (attempts == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "No aptitude attempts yet");
        }
        long score = ((Number) t[0]).longValue();
        long marks = ((Number) t[1]).longValue();

        long[] quant = new long[2];      // [total, correct]
        long[] logical = new long[2];
        long[] verbal = new long[2];
        for (Object[] row : resultRepository.categoryStats(userId)) {
            String category = (String) row[0];
            long total = ((Number) row[1]).longValue();
            long correct = row[2] == null ? 0 : ((Number) row[2]).longValue();
            long[] bucket = switch (category) {
                case "QUANTITATIVE", "DATA_INTERPRETATION" -> quant;
                case "LOGICAL_REASONING" -> logical;
                case "VERBAL_ABILITY" -> verbal;
                default -> null;
            };
            if (bucket != null) {
                bucket[0] += total;
                bucket[1] += correct;
            }
        }
        return new AptitudeSummary(userId, (int) attempts, percentage(score, marks),
                bucketScore(quant), bucketScore(logical), bucketScore(verbal));
    }

    private Double bucketScore(long[] bucket) {
        return bucket[0] == 0 ? null : percentage(bucket[1], bucket[0]);
    }

    // =====================================================================
    // Scheduler ke liye: chhode hue tests ko band karo, purane review hatao
    // =====================================================================

    public void cleanupSessions() {
        Instant now = Instant.now();
        for (Map.Entry<Long, ActiveTest> entry : sessions.entrySet()) {
            ActiveTest t = entry.getValue();
            synchronized (t) {
                if (t.finished) {
                    if (t.finishedAt != null
                            && now.isAfter(t.finishedAt.plus(Duration.ofMinutes(reviewTtlMinutes)))) {
                        sessions.remove(entry.getKey(), t);
                    }
                } else {
                    Instant idleDeadline = t.currentStartedAt.plusSeconds((long) secondsPerQuestion + idleTimeoutSeconds);
                    if (now.isAfter(idleDeadline)) {
                        log.info("Closing idle aptitude test of user {}", t.userId);
                        finish(t, true);      // baaki questions skipped maane jayenge
                    }
                }
            }
        }
    }

    // =====================================================================
    // internals (jab bhi call ho, `synchronized (t)` ke andar)
    // =====================================================================

    /** Current question ka time (grace ke saath) nikal gaya ho toh use skip karke aage badho. */
    private boolean applyTimeout(ActiveTest t, Instant now) {
        if (t.finished) {
            return false;
        }
        Instant hardDeadline = t.currentStartedAt.plusSeconds((long) secondsPerQuestion + graceSeconds);
        if (now.isAfter(hardDeadline)) {
            advance(t, now);              // selected null reh gaya = skipped
            return true;
        }
        return false;
    }

    private void advance(ActiveTest t, Instant now) {
        t.current++;
        if (t.current >= t.questions.size()) {
            finish(t, false);
        } else {
            t.currentStartedAt = now;     // agle question ka 2 min ka timer ab shuru
        }
    }

    private TestStateResponse state(ActiveTest t, Instant now, boolean previousTimedOut) {
        if (t.finished) {
            return new TestStateResponse(t.id, "COMPLETED", null, previousTimedOut, t.finalResult);
        }
        SessionQuestion q = t.questions.get(t.current);
        Instant timerEnd = t.currentStartedAt.plusSeconds(secondsPerQuestion);
        int remaining = (int) Math.max(0, Duration.between(now, timerEnd).getSeconds());
        QuestionView view = new QuestionView(t.current + 1, t.questions.size(), q.category(), q.topic(),
                q.question(), q.options(), q.marks(), remaining, secondsPerQuestion);
        return new TestStateResponse(t.id, "IN_PROGRESS", view, previousTimedOut, null);
    }

    /**
     * Test band karo, result banao, aur (agar kuch attempt hua ho) DB mein chhota record save karo.
     * autoEnded + ek bhi answer nahi = student ne test kabhi attempt hi nahi kiya: save nahi karte.
     */
    private void finish(ActiveTest t, boolean autoEnded) {
        Instant now = Instant.now();
        int n = t.questions.size();
        int correctCount = 0;
        int skipped = 0;
        int totalMarks = 0;
        Map<String, long[]> byCategory = new LinkedHashMap<>();     // [total, correct]
        List<ReviewItem> review = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            SessionQuestion q = t.questions.get(i);
            totalMarks += q.marks();
            if (t.selected[i] == null) {
                skipped++;
            }
            if (t.correct[i]) {
                correctCount++;
            }
            long[] counts = byCategory.computeIfAbsent(q.category(), k -> new long[2]);
            counts[0]++;
            if (t.correct[i]) {
                counts[1]++;
            }
            review.add(new ReviewItem(i + 1, q.category(), q.topic(), q.question(), q.options(),
                    t.selected[i], q.correctIndex(), t.correct[i], q.explanation()));
        }

        boolean answeredAny = skipped < n;
        Long resultId = null;
        if (!autoEnded || answeredAny) {
            try {
                AptitudeResult r = new AptitudeResult();
                r.setUserId(t.userId);
                r.setScore(t.score);
                r.setTotalMarks(totalMarks);
                r.setTotalQuestions(n);
                r.setCorrectCount(correctCount);
                r.setSkippedCount(skipped);
                r.setAutoEnded(autoEnded);
                r.setStartedAt(t.startedAt);
                r.setSubmittedAt(now);
                for (Map.Entry<String, long[]> e : byCategory.entrySet()) {
                    AptitudeResultCategory c = new AptitudeResultCategory();
                    c.setResult(r);
                    c.setCategory(e.getKey());
                    c.setTotalQuestions((int) e.getValue()[0]);
                    c.setCorrectCount((int) e.getValue()[1]);
                    r.getCategories().add(c);
                }
                resultId = resultRepository.save(r).getId();
            } catch (Exception e) {
                log.error("Could not save aptitude result for user {}", t.userId, e);   // student ko result phir bhi dikhega
            }
        }

        List<CategoryScore> breakdown = byCategory.entrySet().stream()
                .map(e -> new CategoryScore(e.getKey(), e.getValue()[0], e.getValue()[1],
                        percentage(e.getValue()[1], e.getValue()[0])))
                .toList();

        t.finished = true;
        t.finishedAt = now;
        t.finalResult = new ResultResponse(resultId, t.score, totalMarks, percentage(t.score, totalMarks), n,
                correctCount, n - correctCount - skipped, skipped, autoEnded, now, breakdown, review);
    }

    // ---- question service ke questions ko VALIDATE karna (galat answer key = galat marks) ----

    private List<SessionQuestion> fetchValidQuestions() {
        GeneratedQuestionsResponse response = questionClient.generate(questionCount);

        Set<String> seenTexts = new HashSet<>();
        List<SessionQuestion> valid = new ArrayList<>();
        int rejected = 0;
        for (GeneratedQuestion q : response.questions()) {
            if (!isValid(q) || !seenTexts.add(q.question().trim().toLowerCase())) {
                rejected++;
                continue;
            }
            int marks = (q.marks() == null || q.marks() <= 0) ? 1 : q.marks();
            valid.add(new SessionQuestion(
                    q.category() == null ? "UNKNOWN" : q.category(),
                    q.topic(),
                    q.question().trim(),
                    q.options().stream().map(String::trim).toList(),
                    q.correctOptionIndex(),
                    q.explanation(),
                    marks));
            if (valid.size() == questionCount) {
                break;
            }
        }

        log.info("Question service returned {} questions: {} valid, {} rejected (source={})",
                response.questions().size(), valid.size(), rejected, response.source());

        if (valid.size() < minValidQuestions) {
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "Could not prepare a valid test right now. Please try again.");
        }
        return valid;
    }

    private boolean isValid(GeneratedQuestion q) {
        if (q == null || isBlank(q.question()) || q.options() == null || q.options().size() != 4) {
            return false;
        }
        if (q.options().stream().anyMatch(this::isBlank)) {
            return false;
        }
        Set<String> distinct = q.options().stream()
                .map(o -> o.trim().toLowerCase())
                .collect(Collectors.toSet());
        if (distinct.size() != 4) {
            return false;
        }
        Integer idx = q.correctOptionIndex();
        if (idx == null || idx < 0 || idx >= 4 || isBlank(q.correctAnswer())) {
            return false;
        }
        return q.options().get(idx).trim().equalsIgnoreCase(q.correctAnswer().trim());
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private double percentage(long part, long whole) {
        if (whole == 0) {
            return 0.0;
        }
        return Math.round(part * 1000.0 / whole) / 10.0;
    }
}
