package com.interviewiq.coding.judge0;

import com.interviewiq.coding.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Supported languages. Problems stdin/stdout style ke hain (input padho, answer print karo).
 * timeMultiplier: Java/JS ko startup time ke liye thoda extra time milta hai.
 */
public enum Language {

    JAVA("java", "Java", 62, 2.0, 2.0, """
            import java.util.*;

            public class Main {
                public static void main(String[] args) {
                    Scanner sc = new Scanner(System.in);
                    // input padho, solution likho, answer System.out.println se print karo
                }
            }
            """),

    PYTHON("python", "Python 3", 71, 2.0, 1.0, """
            # input() se padho, print() se answer do
            """),

    CPP("cpp", "C++", 54, 1.0, 1.0, """
            #include <bits/stdc++.h>
            using namespace std;

            int main() {
                ios::sync_with_stdio(false);
                cin.tie(nullptr);
                // input cin se padho, answer cout se print karo
                return 0;
            }
            """),

    C("c", "C", 50, 1.0, 1.0, """
            #include <stdio.h>

            int main() {
                // input scanf se padho, answer printf se print karo
                return 0;
            }
            """),

    JAVASCRIPT("javascript", "JavaScript (Node.js)", 63, 2.0, 2.0, """
            const lines = require('fs').readFileSync(0, 'utf8').split('\\n');
            // lines[0], lines[1] ... se input lo, console.log se answer print karo
            """);

    private final String key;
    private final String label;
    private final int defaultJudge0Id;
    private final double timeMultiplier;
    private final double memoryMultiplier;
    private final String starterCode;

    Language(String key, String label, int defaultJudge0Id, double timeMultiplier,
             double memoryMultiplier, String starterCode) {
        this.key = key;
        this.label = label;
        this.defaultJudge0Id = defaultJudge0Id;
        this.timeMultiplier = timeMultiplier;
        this.memoryMultiplier = memoryMultiplier;
        this.starterCode = starterCode;
    }

    public String getKey() { return key; }
    public String getLabel() { return label; }
    public int getDefaultJudge0Id() { return defaultJudge0Id; }
    public double getTimeMultiplier() { return timeMultiplier; }
    public double getMemoryMultiplier() { return memoryMultiplier; }
    public String getStarterCode() { return starterCode; }

    public static Language fromKey(String key) {
        if (key != null) {
            for (Language l : values()) {
                if (l.key.equalsIgnoreCase(key.trim())) {
                    return l;
                }
            }
        }
        String supported = Arrays.stream(values()).map(Language::getKey).collect(Collectors.joining(", "));
        throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported language. Supported: " + supported);
    }
}
