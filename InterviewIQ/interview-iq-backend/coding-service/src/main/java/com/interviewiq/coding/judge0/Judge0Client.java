package com.interviewiq.coding.judge0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.interviewiq.coding.config.Judge0Properties;
import com.interviewiq.coding.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Judge0 se baat karta hai: saare test cases ek batch mein bhejta hai, phir result aane tak poll karta hai.
 * Text fields base64 mein jaate/aate hain (special characters safe rehte hain).
 */
@Component
public class Judge0Client {

    private static final Logger log = LoggerFactory.getLogger(Judge0Client.class);
    private static final int MAX_BATCH = 20;       // Judge0 ka default batch limit

    private final RestClient rest;
    private final Judge0Properties props;

    public Judge0Client(Judge0Properties props) {
        this.props = props;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);

        String baseUrl = props.getUrl().endsWith("/")
                ? props.getUrl().substring(0, props.getUrl().length() - 1)
                : props.getUrl();
        RestClient.Builder builder = RestClient.builder().baseUrl(baseUrl).requestFactory(factory);
        if (props.getApiKey() != null && !props.getApiKey().isBlank()) {
            builder.defaultHeader(props.getApiKeyHeader(), props.getApiKey());
        }
        this.rest = builder.build();
    }

    /** Result of one run (decoded). time/memory null ho sakte hain (compile error mein). */
    public record Outcome(int statusId, String statusDescription, String stdout, String stderr,
                          String compileOutput, String message, Integer timeMs, Integer memoryKb) {}

    public List<Outcome> runBatch(int languageId, String sourceCode, double cpuTimeLimitSeconds,
                                  int memoryLimitKb, List<String> stdins) {
        if (stdins.isEmpty() || stdins.size() > MAX_BATCH) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid number of test cases");
        }
        try {
            List<Map<String, Object>> submissions = new ArrayList<>();
            for (String stdin : stdins) {
                Map<String, Object> s = new LinkedHashMap<>();
                s.put("language_id", languageId);
                s.put("source_code", b64(sourceCode));
                s.put("stdin", b64(stdin));
                s.put("cpu_time_limit", cpuTimeLimitSeconds);
                s.put("memory_limit", memoryLimitKb);
                submissions.add(s);
            }

            TokenItem[] created = rest.post()
                    .uri("/submissions/batch?base64_encoded=true")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("submissions", submissions))
                    .retrieve()
                    .body(TokenItem[].class);

            if (created == null || created.length != stdins.size()) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "Code execution service returned an invalid response.");
            }
            List<String> tokens = new ArrayList<>();
            for (TokenItem item : created) {
                if (item == null || item.token() == null) {
                    throw new ApiException(HttpStatus.BAD_GATEWAY, "Code execution service rejected the submission.");
                }
                tokens.add(item.token());
            }
            return poll(tokens);

        } catch (RestClientException e) {
            log.error("Judge0 call failed: {}", e.getMessage());
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Code execution service is unavailable. Please try again in a moment.");
        }
    }

    private List<Outcome> poll(List<String> tokens) {
        String tokenParam = String.join(",", tokens);
        long deadline = System.currentTimeMillis() + props.getMaxWaitSeconds() * 1000L;

        while (true) {
            BatchResponse response = rest.get()
                    .uri(uri -> uri.path("/submissions/batch")
                            .queryParam("tokens", tokenParam)
                            .queryParam("base64_encoded", "true")
                            .queryParam("fields", "token,stdout,stderr,compile_output,message,status,time,memory")
                            .build())
                    .retrieve()
                    .body(BatchResponse.class);

            if (response != null && response.submissions() != null && response.submissions().size() == tokens.size()) {
                Map<String, JudgeSubmission> byToken = new HashMap<>();
                for (JudgeSubmission s : response.submissions()) {
                    byToken.put(s.token(), s);
                }
                boolean allDone = tokens.stream().allMatch(t -> {
                    JudgeSubmission s = byToken.get(t);
                    return s != null && s.status() != null && s.status().id() != null && s.status().id() > 2;
                });
                if (allDone) {
                    List<Outcome> outcomes = new ArrayList<>();
                    for (String t : tokens) {
                        outcomes.add(toOutcome(byToken.get(t)));
                    }
                    return outcomes;
                }
            }

            if (System.currentTimeMillis() > deadline) {
                throw new ApiException(HttpStatus.GATEWAY_TIMEOUT,
                        "Code execution took too long to respond. Please try again.");
            }
            try {
                Thread.sleep(props.getPollIntervalMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Interrupted while waiting for result");
            }
        }
    }

    private Outcome toOutcome(JudgeSubmission s) {
        return new Outcome(
                s.status().id(),
                s.status().description(),
                decode(s.stdout()),
                decode(s.stderr()),
                decode(s.compileOutput()),
                decode(s.message()),
                toMs(s.time()),
                s.memory());
    }

    private Integer toMs(String seconds) {
        if (seconds == null || seconds.isBlank()) {
            return null;
        }
        try {
            return (int) Math.round(Double.parseDouble(seconds) * 1000);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String b64(String text) {
        return Base64.getEncoder().encodeToString((text == null ? "" : text).getBytes(StandardCharsets.UTF_8));
    }

    /** Judge0 base64 mein line breaks daalta hai, isliye MIME decoder. */
    private String decode(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return "";
        }
        return new String(Base64.getMimeDecoder().decode(encoded), StandardCharsets.UTF_8);
    }

    // ---- Judge0 JSON shapes ----
    @JsonIgnoreProperties(ignoreUnknown = true)
    record TokenItem(String token) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record BatchResponse(List<JudgeSubmission> submissions) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record JudgeSubmission(String token, String stdout, String stderr,
                                   @JsonProperty("compile_output") String compileOutput,
                                   String message, Status status, String time, Integer memory) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Status(Integer id, String description) {}
}
