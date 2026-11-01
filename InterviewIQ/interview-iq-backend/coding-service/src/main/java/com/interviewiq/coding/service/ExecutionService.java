package com.interviewiq.coding.service;

import com.interviewiq.coding.config.Judge0Properties;
import com.interviewiq.coding.entity.Verdict;
import com.interviewiq.coding.judge0.Judge0Client;
import com.interviewiq.coding.judge0.Judge0Client.Outcome;
import com.interviewiq.coding.judge0.Language;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Code ko Judge0 pe chalata hai aur har test case ka verdict nikalta hai. */
@Service
public class ExecutionService {

    private static final int MAX_MESSAGE_CHARS = 2000;
    private static final double MAX_CPU_SECONDS = 15.0;      // Judge0 ka default max
    private static final int MAX_MEMORY_KB = 512000;         // Judge0 ka default max

    private final Judge0Client client;
    private final Judge0Properties props;

    public ExecutionService(Judge0Client client, Judge0Properties props) {
        this.client = client;
        this.props = props;
    }

    /** expected null = sirf chalao (custom input), compare mat karo. */
    public record CaseSpec(String input, String expected, boolean sample) {}

    public record CaseOutcome(Verdict verdict, String actualOutput, String stderr, Integer timeMs, Integer memoryKb) {}

    public record Evaluation(Verdict verdict, int passed, Integer maxTimeMs, Integer maxMemoryKb,
                             int firstFailIndex, String compileOutput, String message,
                             List<CaseOutcome> outcomes) {}

    public Evaluation execute(Language language, String sourceCode, double timeLimitSeconds,
                              int memoryLimitKb, List<CaseSpec> cases) {

        double cpuLimit = Math.min(MAX_CPU_SECONDS, timeLimitSeconds * language.getTimeMultiplier());
        int memLimit = (int) Math.min(MAX_MEMORY_KB, memoryLimitKb * language.getMemoryMultiplier());

        List<Outcome> raw = client.runBatch(props.idFor(language), sourceCode, cpuLimit, memLimit,
                cases.stream().map(CaseSpec::input).toList());

        List<CaseOutcome> outcomes = new ArrayList<>();
        int passed = 0;
        int firstFail = -1;
        Verdict overall = Verdict.ACCEPTED;
        Integer maxTime = null;
        Integer maxMem = null;

        for (int i = 0; i < raw.size(); i++) {
            Outcome o = raw.get(i);
            Verdict v = classify(o, cases.get(i).expected());
            outcomes.add(new CaseOutcome(v, o.stdout(), o.stderr(), o.timeMs(), o.memoryKb()));

            if (v == Verdict.ACCEPTED) {
                passed++;
            } else if (firstFail == -1) {
                firstFail = i;
                overall = v;
            }
            if (o.timeMs() != null && (maxTime == null || o.timeMs() > maxTime)) {
                maxTime = o.timeMs();
            }
            if (o.memoryKb() != null && (maxMem == null || o.memoryKb() > maxMem)) {
                maxMem = o.memoryKb();
            }
        }

        String compileOutput = null;
        String message = null;
        if (firstFail >= 0) {
            Outcome failed = raw.get(firstFail);
            switch (overall) {
                case COMPILATION_ERROR -> compileOutput = trunc(failed.compileOutput());
                case RUNTIME_ERROR -> message = trunc(firstNonBlank(failed.stderr(), failed.message(),
                        failed.statusDescription()));
                case TIME_LIMIT_EXCEEDED -> message = "Time limit exceeded on test " + (firstFail + 1);
                case INTERNAL_ERROR -> message = trunc(firstNonBlank(failed.message(), failed.statusDescription()));
                default -> { }
            }
        }

        return new Evaluation(overall, passed, maxTime, maxMem, firstFail, compileOutput, message, outcomes);
    }

    /** Judge0 status id: 3 ok, 5 TLE, 6 compile error, 7-12 runtime errors, 13 internal, 14 exec format. */
    private Verdict classify(Outcome o, String expected) {
        int id = o.statusId();
        if (id == 3) {
            if (expected == null) {
                return Verdict.ACCEPTED;
            }
            return OutputComparator.matches(expected, o.stdout()) ? Verdict.ACCEPTED : Verdict.WRONG_ANSWER;
        }
        if (id == 5) {
            return Verdict.TIME_LIMIT_EXCEEDED;
        }
        if (id == 6) {
            return Verdict.COMPILATION_ERROR;
        }
        if ((id >= 7 && id <= 12) || id == 14) {
            return Verdict.RUNTIME_ERROR;
        }
        return Verdict.INTERNAL_ERROR;
    }

    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return "";
    }

    private String trunc(String s) {
        if (s == null) {
            return null;
        }
        return s.length() <= MAX_MESSAGE_CHARS ? s : s.substring(0, MAX_MESSAGE_CHARS) + "\n...(truncated)";
    }
}
