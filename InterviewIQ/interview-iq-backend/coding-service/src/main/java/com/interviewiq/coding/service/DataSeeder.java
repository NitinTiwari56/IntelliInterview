package com.interviewiq.coding.service;

import com.interviewiq.coding.entity.Difficulty;
import com.interviewiq.coding.entity.Problem;
import com.interviewiq.coding.entity.TestCase;
import com.interviewiq.coding.repository.ProblemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Pehli baar (problems table khali ho tab) 7 sample problems daal deta hai,
 * taaki admin API ke bina bhi demo/testing ho sake. Band karne ke liye: coding.seed-sample-problems=false
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final ProblemRepository problemRepository;
    private final boolean enabled;

    public DataSeeder(ProblemRepository problemRepository,
                      @Value("${coding.seed-sample-problems}") boolean enabled) {
        this.problemRepository = problemRepository;
        this.enabled = enabled;
    }

    @Override
    public void run(String... args) {
        if (!enabled || problemRepository.count() > 0) {
            return;
        }

        save(problem("sum-of-two-numbers", "Sum of Two Numbers", "Math", Difficulty.EASY, """
                Do integers `a` aur `b` diye gaye hain. Unka sum print karo.

                **Input:** Ek line mein do integers `a` aur `b` (space se alag).

                **Output:** `a + b`

                **Constraints:** -10^9 <= a, b <= 10^9

                **Example**
                ```
                Input:  3 5
                Output: 8
                ```
                """,
                tc("3 5\n", "8\n", true),
                tc("-2 7\n", "5\n", true),
                tc("1000000000 1000000000\n", "2000000000\n", false),
                tc("0 0\n", "0\n", false)));

        save(problem("reverse-a-string", "Reverse a String", "Strings", Difficulty.EASY, """
                Ek string ko ulta (reverse) karke print karo.

                **Input:** Ek line mein string `s` (spaces ho sakte hain).

                **Output:** `s` ka reverse.

                **Constraints:** 1 <= length(s) <= 1000

                **Example**
                ```
                Input:  hello
                Output: olleh
                ```
                """,
                tc("hello\n", "olleh\n", true),
                tc("Interview IQ\n", "QI weivretnI\n", true),
                tc("a\n", "a\n", false),
                tc("racecar\n", "racecar\n", false)));

        save(problem("palindrome-check", "Palindrome Check", "Strings", Difficulty.EASY, """
                Check karo ki diya gaya word palindrome hai ya nahi (aage-peeche same padhta hai).

                **Input:** Ek line mein lowercase word `s`.

                **Output:** Palindrome ho toh `YES`, warna `NO`.

                **Constraints:** 1 <= length(s) <= 1000

                **Example**
                ```
                Input:  madam
                Output: YES
                ```
                """,
                tc("madam\n", "YES\n", true),
                tc("hello\n", "NO\n", true),
                tc("a\n", "YES\n", false),
                tc("abba\n", "YES\n", false),
                tc("abca\n", "NO\n", false)));

        save(problem("largest-element", "Largest Element in Array", "Arrays", Difficulty.EASY, """
                Array ka sabse bada element print karo.

                **Input:**
                - Pehli line: `n` (elements ki ginti)
                - Doosri line: `n` integers (space se alag)

                **Output:** Maximum element.

                **Constraints:** 1 <= n <= 10^5, -10^9 <= element <= 10^9

                **Example**
                ```
                Input:
                5
                3 9 2 7 1
                Output: 9
                ```
                """,
                tc("5\n3 9 2 7 1\n", "9\n", true),
                tc("4\n-3 -1 -7 -2\n", "-1\n", true),
                tc("1\n-5\n", "-5\n", false),
                tc("6\n10 10 10 10 10 10\n", "10\n", false)));

        save(problem("fibonacci-number", "Fibonacci Number", "Dynamic Programming", Difficulty.EASY, """
                Fibonacci sequence: F(0) = 0, F(1) = 1, F(n) = F(n-1) + F(n-2). `F(n)` print karo.

                **Input:** Ek integer `n`.

                **Output:** `F(n)`

                **Constraints:** 0 <= n <= 90 (answer 64-bit integer mein aata hai, Java mein `long` use karo)

                **Example**
                ```
                Input:  10
                Output: 55
                ```
                """,
                tc("10\n", "55\n", true),
                tc("1\n", "1\n", true),
                tc("0\n", "0\n", false),
                tc("50\n", "12586269025\n", false),
                tc("90\n", "2880067194370816120\n", false)));

        save(problem("two-sum-indices", "Two Sum", "Arrays", Difficulty.MEDIUM, """
                Ek array aur `target` diya hai. Aise do alag indices `i < j` dhundo jinke elements ka sum `target` ho.
                Exactly ek valid jodi guaranteed hai.

                **Input:**
                - Pehli line: `n` aur `target`
                - Doosri line: `n` integers

                **Output:** `i j` (0-based indices, space se alag)

                **Constraints:** 2 <= n <= 10^4

                **Example**
                ```
                Input:
                4 9
                2 7 11 15
                Output: 0 1
                ```
                """,
                tc("4 9\n2 7 11 15\n", "0 1\n", true),
                tc("3 6\n3 2 4\n", "1 2\n", true),
                tc("2 6\n3 3\n", "0 1\n", false),
                tc("5 0\n-3 4 3 90 1\n", "0 2\n", false)));

        save(problem("valid-parentheses", "Valid Parentheses", "Stack", Difficulty.MEDIUM, """
                Ek string me sirf `(`, `)`, `[`, `]`, `{`, `}` hain. Check karo ki brackets sahi tarah se khule aur band hue hain ya nahi.

                **Input:** Ek line mein string `s`.

                **Output:** Valid ho toh `YES`, warna `NO`.

                **Constraints:** 1 <= length(s) <= 10^4

                **Example**
                ```
                Input:  ()[]{}
                Output: YES
                ```
                """,
                tc("()[]{}\n", "YES\n", true),
                tc("(]\n", "NO\n", true),
                tc("([{}])\n", "YES\n", false),
                tc("(((\n", "NO\n", false),
                tc("{[}]\n", "NO\n", false)));

        log.info("Seeded {} sample coding problems", problemRepository.count());
    }

    private void save(Problem p) {
        problemRepository.save(p);
    }

    private Problem problem(String slug, String title, String topic, Difficulty difficulty,
                            String description, TestCase... cases) {
        Problem p = new Problem();
        p.setSlug(slug);
        p.setTitle(title);
        p.setTopic(topic);
        p.setDifficulty(difficulty);
        p.setDescription(description);
        int position = 1;
        for (TestCase tc : cases) {
            tc.setProblem(p);
            tc.setPosition(position++);
            p.getTestCases().add(tc);
        }
        return p;
    }

    private TestCase tc(String input, String expectedOutput, boolean sample) {
        TestCase t = new TestCase();
        t.setInput(input);
        t.setExpectedOutput(expectedOutput);
        t.setSample(sample);
        return t;
    }
}
