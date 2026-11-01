package com.interviewiq.coding.service;

import com.interviewiq.coding.dto.CodingDtos.*;
import com.interviewiq.coding.entity.*;
import com.interviewiq.coding.exception.ApiException;
import com.interviewiq.coding.judge0.Language;
import com.interviewiq.coding.repository.ProblemRepository;
import com.interviewiq.coding.repository.SubmissionRepository;
import com.interviewiq.coding.service.ExecutionService.CaseOutcome;
import com.interviewiq.coding.service.ExecutionService.CaseSpec;
import com.interviewiq.coding.service.ExecutionService.Evaluation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CodingService {

    private final ProblemService problemService;
    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final ExecutionService executionService;
    private final long cooldownMs;

    // userId -> last run/submit time (in-memory; Judge0 public instance ki throttling se bachne ke liye)
    private final Map<Long, Long> lastRequestAt = new ConcurrentHashMap<>();

    public CodingService(ProblemService problemService,
                         ProblemRepository problemRepository,
                         SubmissionRepository submissionRepository,
                         ExecutionService executionService,
                         @Value("${coding.submit-cooldown-seconds}") int cooldownSeconds) {
        this.problemService = problemService;
        this.problemRepository = problemRepository;
        this.submissionRepository = submissionRepository;
        this.executionService = executionService;
        this.cooldownMs = cooldownSeconds * 1000L;
    }

    public List<LanguageInfo> languages() {
        return Arrays.stream(Language.values())
                .map(l -> new LanguageInfo(l.getKey(), l.getLabel(), l.getStarterCode()))
                .toList();
    }

    // =====================================================================
    // RUN: sirf sample cases (ya custom input) pe chalao, kuch save nahi hota
    // =====================================================================
    public RunResponse run(Long userId, String slug, RunRequest request) {
        guard(userId);
        Language language = Language.fromKey(request.language());
        Problem problem = problemService.getPublishedWithCases(slug);

        boolean custom = request.customInput() != null;
        List<CaseSpec> specs = new ArrayList<>();
        if (custom) {
            specs.add(new CaseSpec(request.customInput(), null, false));
        } else {
            for (TestCase tc : problem.getTestCases()) {
                if (tc.isSample()) {
                    specs.add(new CaseSpec(tc.getInput(), tc.getExpectedOutput(), true));
                }
            }
        }
        if (specs.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This problem has no sample test cases");
        }

        Evaluation ev = executionService.execute(language, request.sourceCode(), problem.getTimeLimitSeconds(),
                problem.getMemoryLimitKb(), specs);
        failIfInternal(ev);

        List<CaseResult> results = new ArrayList<>();
        for (int i = 0; i < specs.size(); i++) {
            CaseSpec spec = specs.get(i);
            CaseOutcome o = ev.outcomes().get(i);
            String status = custom && o.verdict() == Verdict.ACCEPTED ? "RAN" : caseStatus(o.verdict());
            results.add(new CaseResult(i + 1, status, spec.input(), spec.expected(),
                    o.actualOutput(), o.stderr(), o.timeMs()));
        }

        String verdict = custom && ev.verdict() == Verdict.ACCEPTED ? "COMPLETED" : ev.verdict().name();
        return new RunResponse(verdict, ev.compileOutput(), results);
    }

    private String caseStatus(Verdict v) {
        return v == Verdict.ACCEPTED ? "PASSED" : (v == Verdict.WRONG_ANSWER ? "FAILED" : v.name());
    }

    // =====================================================================
    // SUBMIT: saare test cases (hidden bhi), result save hota hai
    // =====================================================================
    public SubmitResponse submit(Long userId, String slug, SubmitRequest request) {
        guard(userId);
        Language language = Language.fromKey(request.language());
        Problem problem = problemService.getPublishedWithCases(slug);

        List<CaseSpec> specs = problem.getTestCases().stream()
                .map(tc -> new CaseSpec(tc.getInput(), tc.getExpectedOutput(), tc.isSample()))
                .toList();
        if (specs.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This problem has no test cases yet");
        }

        Evaluation ev = executionService.execute(language, request.sourceCode(), problem.getTimeLimitSeconds(),
                problem.getMemoryLimitKb(), specs);
        failIfInternal(ev);

        String message = ev.verdict() == Verdict.COMPILATION_ERROR ? ev.compileOutput() : ev.message();

        Submission s = new Submission();
        s.setUserId(userId);
        s.setProblem(problem);
        s.setLanguage(language.getKey());
        s.setSourceCode(request.sourceCode());
        s.setVerdict(ev.verdict());
        s.setPassedTests(ev.passed());
        s.setTotalTests(specs.size());
        s.setMaxTimeMs(ev.maxTimeMs());
        s.setMaxMemoryKb(ev.maxMemoryKb());
        s.setMessage(message);
        submissionRepository.save(s);

        FailedCase failedCase = null;
        boolean failedOnHidden = false;
        boolean testFailure = ev.verdict() == Verdict.WRONG_ANSWER
                || ev.verdict() == Verdict.RUNTIME_ERROR
                || ev.verdict() == Verdict.TIME_LIMIT_EXCEEDED;
        if (testFailure && ev.firstFailIndex() >= 0) {
            CaseSpec spec = specs.get(ev.firstFailIndex());
            if (spec.sample()) {
                failedCase = new FailedCase(spec.input(), spec.expected(),
                        ev.outcomes().get(ev.firstFailIndex()).actualOutput());
            } else {
                failedOnHidden = true;       // hidden test ka input/expected kabhi nahi dikhate
            }
        }

        return new SubmitResponse(s.getId(), ev.verdict(), ev.passed(), specs.size(), ev.maxTimeMs(),
                ev.maxMemoryKb(), message, failedCase, failedOnHidden);
    }

    // =====================================================================
    // history + stats
    // =====================================================================
    @Transactional(readOnly = true)
    public List<SubmissionItem> mySubmissions(Long userId) {
        return submissionRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 50)).stream()
                .map(this::toItem).toList();
    }

    @Transactional(readOnly = true)
    public List<SubmissionItem> mySubmissionsForProblem(Long userId, String slug) {
        return submissionRepository.findByUserIdAndProblemSlugOrderByCreatedAtDesc(userId, slug).stream()
                .map(this::toItem).toList();
    }

    @Transactional(readOnly = true)
    public SubmissionDetail submissionDetail(Long userId, Long id) {
        Submission s = submissionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Submission not found"));
        return new SubmissionDetail(s.getId(), s.getProblem().getSlug(), s.getProblem().getTitle(),
                s.getLanguage(), s.getVerdict(), s.getPassedTests(), s.getTotalTests(), s.getMaxTimeMs(),
                s.getMaxMemoryKb(), s.getMessage(), s.getSourceCode(), s.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public StatsResponse stats(Long userId) {
        long total = submissionRepository.countByUserId(userId);
        long accepted = submissionRepository.countByUserIdAndVerdict(userId, Verdict.ACCEPTED);

        Map<Long, Long> acceptedCounts = problemService.acceptedCounts(userId);   // attempted problems
        List<Problem> published = problemRepository.findByPublishedTrueOrderByIdAsc();

        Map<Difficulty, long[]> byDifficulty = new EnumMap<>(Difficulty.class);   // [solved, total]
        for (Difficulty d : Difficulty.values()) {
            byDifficulty.put(d, new long[2]);
        }
        Map<String, long[]> byTopic = new TreeMap<>();                             // [solved, attempted]
        long attempted = 0;
        long solved = 0;

        for (Problem p : published) {
            long[] d = byDifficulty.get(p.getDifficulty());
            d[1]++;
            Long acc = acceptedCounts.get(p.getId());
            if (acc != null) {
                attempted++;
                long[] t = byTopic.computeIfAbsent(p.getTopic(), k -> new long[2]);
                t[1]++;
                if (acc > 0) {
                    solved++;
                    d[0]++;
                    t[0]++;
                }
            }
        }

        List<DifficultyStat> diffStats = new ArrayList<>();
        byDifficulty.forEach((d, v) -> diffStats.add(new DifficultyStat(d, v[0], v[1])));
        List<TopicStat> topicStats = new ArrayList<>();
        byTopic.forEach((topic, v) -> topicStats.add(new TopicStat(topic, v[0], v[1])));

        double rate = total == 0 ? 0.0 : Math.round(accepted * 1000.0 / total) / 10.0;
        return new StatsResponse(total, accepted, rate, attempted, solved, diffStats, topicStats);
    }

    /**
     * Analytics ke liye. Koi submission nahi = 404 (analytics "no data" maanta hai).
     * averageScore: 70% (difficulty-weighted solved: EASY 1, MEDIUM 2, HARD 3 points) + 30% acceptance rate.
     */
    @Transactional(readOnly = true)
    public CodingSummary summary(Long userId) {
        long total = submissionRepository.countByUserId(userId);
        if (total == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "No coding activity yet");
        }
        long accepted = submissionRepository.countByUserIdAndVerdict(userId, Verdict.ACCEPTED);
        double accuracy = Math.round(accepted * 1000.0 / total) / 10.0;

        Map<Long, Long> acceptedCounts = problemService.acceptedCounts(userId);
        long totalPoints = 0;
        long solvedPoints = 0;
        int solved = 0;
        for (Problem p : problemRepository.findByPublishedTrueOrderByIdAsc()) {
            int points = switch (p.getDifficulty()) {
                case EASY -> 1;
                case MEDIUM -> 2;
                case HARD -> 3;
            };
            totalPoints += points;
            Long acc = acceptedCounts.get(p.getId());
            if (acc != null && acc > 0) {
                solvedPoints += points;
                solved++;
            }
        }
        double coverage = totalPoints == 0 ? 0.0 : (double) solvedPoints / totalPoints * 100.0;
        double score = Math.round((0.7 * coverage + 0.3 * accuracy) * 10.0) / 10.0;

        Map<String, Double> topicAccuracy = new TreeMap<>();
        for (Object[] row : submissionRepository.topicSummary(userId, Verdict.ACCEPTED)) {
            long t = ((Number) row[1]).longValue();
            long a = row[2] == null ? 0 : ((Number) row[2]).longValue();
            topicAccuracy.put((String) row[0], t == 0 ? 0.0 : Math.round(a * 1000.0 / t) / 10.0);
        }
        return new CodingSummary(userId, solved, (int) total, accuracy, score, topicAccuracy);
    }

    // =====================================================================
    // helpers
    // =====================================================================
    private SubmissionItem toItem(Submission s) {
        return new SubmissionItem(s.getId(), s.getProblem().getSlug(), s.getProblem().getTitle(), s.getLanguage(),
                s.getVerdict(), s.getPassedTests(), s.getTotalTests(), s.getMaxTimeMs(), s.getMaxMemoryKb(),
                s.getCreatedAt());
    }

    private void failIfInternal(Evaluation ev) {
        if (ev.verdict() == Verdict.INTERNAL_ERROR) {
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "The judge had an internal problem. Please try again. (" + ev.message() + ")");
        }
    }

    /** Ek user har cooldown seconds mein sirf ek run/submit kar sakta hai. */
    private void guard(Long userId) {
        long now = System.currentTimeMillis();
        Long last = lastRequestAt.put(userId, now);
        if (last != null && now - last < cooldownMs) {
            long wait = (cooldownMs - (now - last) + 999) / 1000;
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Please wait " + wait + " second(s) before running code again.");
        }
    }
}
