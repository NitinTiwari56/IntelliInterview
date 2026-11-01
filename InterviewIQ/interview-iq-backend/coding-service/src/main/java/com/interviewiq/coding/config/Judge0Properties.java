package com.interviewiq.coding.config;

import com.interviewiq.coding.judge0.Language;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/** application.yml ke "judge0:" section se bharta hai. */
@Component
@ConfigurationProperties(prefix = "judge0")
public class Judge0Properties {

    private String url = "https://ce.judge0.com";
    private String apiKeyHeader = "X-Auth-Token";
    private String apiKey = "";
    private long pollIntervalMs = 700;
    private int maxWaitSeconds = 30;
    private Map<String, Integer> languageIds = new HashMap<>();

    public int idFor(Language language) {
        return languageIds.getOrDefault(language.getKey(), language.getDefaultJudge0Id());
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getApiKeyHeader() { return apiKeyHeader; }
    public void setApiKeyHeader(String apiKeyHeader) { this.apiKeyHeader = apiKeyHeader; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public long getPollIntervalMs() { return pollIntervalMs; }
    public void setPollIntervalMs(long pollIntervalMs) { this.pollIntervalMs = pollIntervalMs; }
    public int getMaxWaitSeconds() { return maxWaitSeconds; }
    public void setMaxWaitSeconds(int maxWaitSeconds) { this.maxWaitSeconds = maxWaitSeconds; }
    public Map<String, Integer> getLanguageIds() { return languageIds; }
    public void setLanguageIds(Map<String, Integer> languageIds) { this.languageIds = languageIds; }
}
