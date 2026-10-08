package com.InterviewPrep.InterviewPrep.service;

import com.InterviewPrep.InterviewPrep.dto.AnswerEvaluationDto;
import com.InterviewPrep.InterviewPrep.model.InterviewType;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RuleBasedInterviewEngineService {

    public static class QuestionTemplate {
        private final String question;
        private final List<String> expectedKeywords;
        private final String idealOutline;

        public QuestionTemplate(String question, List<String> expectedKeywords, String idealOutline) {
            this.question = question;
            this.expectedKeywords = expectedKeywords;
            this.idealOutline = idealOutline;
        }

        public String getQuestion() { return question; }
        public List<String> getExpectedKeywords() { return expectedKeywords; }
        public String getIdealOutline() { return idealOutline; }
    }

    private final Map<String, List<QuestionTemplate>> roleTechnicalQuestions = new HashMap<>();
    private final List<QuestionTemplate> hrQuestions = new ArrayList<>();

    public RuleBasedInterviewEngineService() {
        initQuestionBanks();
    }

    private void initQuestionBanks() {
        // --- Java Backend Developer ---
        List<QuestionTemplate> javaQuestions = new ArrayList<>();
        javaQuestions.add(new QuestionTemplate(
                "Can you explain the internal working of HashMap in Java, and how hash collisions are handled in Java 8 and later?",
                List.of("bucket", "hashcode", "equals", "linkedlist", "red-black tree", "treeify", "threshold", "o(1)", "o(log n)"),
                "Mention hashing mechanism, bucket index calculation (hash & n-1), linked list chaining for collisions, and treeification to Red-Black tree when bucket count exceeds 8."
        ));
        javaQuestions.add(new QuestionTemplate(
                "What is the difference between @Transactional in Spring Boot and manual transaction management? How does transaction propagation work?",
                List.of("proxy", "aop", "rollback", "propagation", "required", "requires_new", "isolation", "acid", "runtimeexception"),
                "Explain AOP proxy wrapping, default rollback on unchecked exceptions, and propagation behaviors like REQUIRED vs REQUIRES_NEW."
        ));
        javaQuestions.add(new QuestionTemplate(
                "How do you ensure thread-safety in a multithreaded Java application? Compare synchronized, volatile, and AtomicInteger.",
                List.of("thread-safety", "synchronized", "volatile", "atomic", "cas", "compare and swap", "mutex", "visibility", "memory barrier"),
                "Discuss visibility vs atomicity, volatile guaranteeing visibility without mutual exclusion, synchronized providing locks, and atomic classes using hardware-level CAS."
        ));
        javaQuestions.add(new QuestionTemplate(
                "In a microservices architecture, how do you handle distributed data consistency and inter-service communication failures?",
                List.of("saga", "circuit breaker", "resilience4j", "kafka", "eventual consistency", "idempotency", "outbox pattern", "retry"),
                "Discuss saga pattern (orchestration vs choreography), 2-phase commit drawbacks, circuit breakers for fault isolation, and message brokers with idempotency."
        ));
        javaQuestions.add(new QuestionTemplate(
                "How would you optimize a slow database query in a Spring Data JPA and MySQL environment?",
                List.of("index", "explain", "n+1", "join fetch", "entitygraph", "caching", "redis", "connection pool", "batching"),
                "Mention analyzing EXPLAIN plans, adding composite indexes, solving the N+1 problem with JOIN FETCH or @EntityGraph, and leveraging second-level or Redis caching."
        ));
        roleTechnicalQuestions.put("java", javaQuestions);

        // --- Frontend / React Developer ---
        List<QuestionTemplate> reactQuestions = new ArrayList<>();
        reactQuestions.add(new QuestionTemplate(
                "Explain how React's Virtual DOM works and what reconciliation algorithm React uses during state updates.",
                List.of("virtual dom", "reconciliation", "diffing", "fiber", "render", "keys", "batching", "re-render"),
                "Describe tree diffing algorithm, key attribute significance for list identification, and React Fiber's incremental rendering architecture."
        ));
        reactQuestions.add(new QuestionTemplate(
                "What is the difference between useEffect, useLayoutEffect, and useMemo? When would you use useMemo over useCallback?",
                List.of("useeffect", "usememo", "usecallback", "paint", "dom", "dependency array", "expensive calculation", "function reference"),
                "Explain asynchronous execution of useEffect after paint vs synchronous useLayoutEffect, and caching values (useMemo) vs memoizing function references (useCallback)."
        ));
        reactQuestions.add(new QuestionTemplate(
                "How do you manage complex application state in React? Compare Redux/Zustand with React Context API.",
                List.of("context", "redux", "zustand", "prop drilling", "re-render", "store", "reducer", "performance", "action"),
                "Context is ideal for low-frequency updates (theme/auth) due to full subtree re-renders; external state managers like Zustand/Redux provide selector-based granular subscriptions."
        ));
        reactQuestions.add(new QuestionTemplate(
                "What strategies do you employ to improve the Core Web Vitals and loading performance of a web application?",
                List.of("code splitting", "lazy loading", "lcp", "cls", "fid", "inp", "tree shaking", "cdn", "memo", "image optimization"),
                "Discuss dynamic imports with React.lazy, SSR/SSG, responsive image sizing, font optimization, and reducing JavaScript bundle size."
        ));
        reactQuestions.add(new QuestionTemplate(
                "How do you handle client-side form validation, error handling, and secure authentication tokens in single-page applications?",
                List.of("httponly cookie", "jwt", "xss", "csrf", "schema validation", "zod", "interceptor", "refresh token"),
                "Explain storing tokens in secure HttpOnly cookies or memory to mitigate XSS, using Axios/Fetch interceptors for automatic refresh, and schema-based form validation."
        ));
        roleTechnicalQuestions.put("frontend", reactQuestions);
        roleTechnicalQuestions.put("react", reactQuestions);

        // --- Python / Data / Full Stack ---
        List<QuestionTemplate> generalQuestions = new ArrayList<>();
        generalQuestions.add(new QuestionTemplate(
                "Can you walk me through your understanding of RESTful API design principles and HTTP status codes (200, 201, 400, 401, 403, 500)?",
                List.of("stateless", "resources", "http methods", "idempotent", "status codes", "get", "post", "put", "delete", "payload"),
                "Mention statelessness, proper nouns for URIs, correct HTTP verb usage, and distinguishing 401 Unauthorized from 403 Forbidden."
        ));
        generalQuestions.add(new QuestionTemplate(
                "How do you design a database schema for an application with high read traffic? Discuss normalization versus denormalization.",
                List.of("normalization", "denormalization", "index", "redundancy", "joins", "read replica", "caching", "query speed"),
                "Explain 3NF for minimizing update anomalies, strategic denormalization to eliminate heavy JOINs for read-heavy workloads, and read-replica scaling."
        ));
        generalQuestions.add(new QuestionTemplate(
                "What is your approach to writing unit and integration tests? How do you mock external dependencies?",
                List.of("unit test", "integration test", "mock", "mockito", "junit", "test pyramid", "coverage", "tdd"),
                "Discuss the test pyramid, testing business logic in isolation with mocks, and verifying end-to-end repository queries with integration tests."
        ));
        generalQuestions.add(new QuestionTemplate(
                "Describe a system architecture for handling asynchronous background tasks and high-throughput events.",
                List.of("message broker", "kafka", "rabbitmq", "queue", "worker", "asynchronous", "event-driven", "dead letter queue"),
                "Discuss decoupling producer and consumer with queues/topics, consumer group scalability, acknowledgment strategies, and dead letter queues for errors."
        ));
        generalQuestions.add(new QuestionTemplate(
                "How do you detect and fix memory leaks or CPU bottlenecks in a production service?",
                List.of("profiler", "heap dump", "metrics", "gc", "garbage collection", "flame graph", "monitoring", "prometheus", "logs"),
                "Discuss CPU/memory profiling tools, taking and analyzing heap dumps to find memory retention paths, and monitoring GC pause metrics."
        ));
        roleTechnicalQuestions.put("general", generalQuestions);

        // --- HR / Behavioural Questions (STAR Method) ---
        hrQuestions.add(new QuestionTemplate(
                "Tell me about yourself, your educational background, and what motivated you to pursue a career in software development.",
                List.of("background", "projects", "passion", "engineering", "learning", "growth", "interest", "technology", "skills"),
                "Clear narrative: educational highlights, interest in technology, key projects built, and current career trajectory."
        ));
        hrQuestions.add(new QuestionTemplate(
                "Can you describe a challenging project or technical problem you worked on? What specific obstacles did you encounter, and how did you resolve them?",
                List.of("situation", "task", "action", "result", "challenge", "problem", "solution", "learned", "debugging"),
                "STAR format (Situation, Task, Action, Result). Highlight specific technical choices, debugging strategies, and measurable outcomes."
        ));
        hrQuestions.add(new QuestionTemplate(
                "Tell me about a time you experienced a conflict or disagreement with a team member or project partner. How did you handle the situation?",
                List.of("communication", "perspective", "listening", "collaboration", "resolution", "professionalism", "compromise", "outcome"),
                "Demonstrate emotional intelligence: active listening, focusing on project goals rather than personal ego, finding a consensus or data-backed compromise."
        ));
        hrQuestions.add(new QuestionTemplate(
                "How do you handle strict deadlines when you have multiple competing priorities or unexpected scope changes?",
                List.of("prioritize", "deadline", "time management", "trade-off", "communication", "stakeholders", "deliverables", "agile"),
                "Show prioritization methods (MoSCoW/Eisenhower matrix), transparent communication with mentors/stakeholders early, and pragmatic scope management."
        ));
        hrQuestions.add(new QuestionTemplate(
                "Where do you see yourself in the next 3 to 5 years, and how does this role align with your personal and professional career goals?",
                List.of("growth", "leadership", "technical depth", "impact", "mentorship", "learning", "domain expertise", "contributing"),
                "Clear balance of deepening technical mastery, taking ownership of larger systems, mentoring newcomers, and contributing value to the organization."
        ));
    }

    public String getNextQuestion(InterviewSessionState sessionState) {
        int nextQNumber = sessionState.getCurrentQuestionNumber() + 1;
        InterviewType type = sessionState.getInterviewType();

        if (type == InterviewType.HR) {
            int index = (nextQNumber - 1) % hrQuestions.size();
            return hrQuestions.get(index).getQuestion();
        }

        // Technical Questions: match role keywords or resume skills
        String role = sessionState.getTargetRole().toLowerCase();
        List<String> resumeSkills = sessionState.getResumeSkills();

        List<QuestionTemplate> candidates = new ArrayList<>();
        if (role.contains("java") || role.contains("backend") || containsSkill(resumeSkills, "java", "spring")) {
            candidates.addAll(roleTechnicalQuestions.get("java"));
        }
        if (role.contains("frontend") || role.contains("react") || role.contains("web") || containsSkill(resumeSkills, "react", "javascript")) {
            candidates.addAll(roleTechnicalQuestions.get("frontend"));
        }
        if (candidates.isEmpty()) {
            candidates.addAll(roleTechnicalQuestions.get("general"));
        }

        int index = (nextQNumber - 1) % candidates.size();
        return candidates.get(index).getQuestion();
    }

    private boolean containsSkill(List<String> skills, String... targets) {
        if (skills == null || skills.isEmpty()) return false;
        for (String skill : skills) {
            String lower = skill.toLowerCase();
            for (String t : targets) {
                if (lower.contains(t.toLowerCase())) return true;
            }
        }
        return false;
    }

    public AnswerEvaluationDto evaluateAnswer(InterviewSessionState sessionState, String question, String answer) {
        int qNum = sessionState.getCurrentQuestionNumber();
        InterviewType type = sessionState.getInterviewType();

        AnswerEvaluationDto dto = new AnswerEvaluationDto();
        dto.setSessionId(sessionState.getSessionId());
        dto.setQuestionNumber(qNum);
        dto.setQuestion(question);
        dto.setAnswer(answer);

        if (answer == null || answer.trim().length() < 10) {
            dto.setScore(30);
            dto.setFeedback("The response was extremely brief or lacked substantive detail. In an interview, provide concrete explanations, examples, and technical context.");
            dto.setStrengths(List.of("Attempted to address the question prompt."));
            dto.setImprovements(List.of("Elaborate on core mechanisms", "Provide practical examples from your projects", "Use structured explanation format (e.g., definition -> mechanism -> trade-offs)"));
            return dto;
        }

        String lowerAns = answer.toLowerCase();
        int wordCount = answer.trim().split("\\s+").length;

        int score = 65; // baseline
        List<String> strengths = new ArrayList<>();
        List<String> improvements = new ArrayList<>();

        if (type == InterviewType.HR) {
            // Check STAR indicators
            boolean hasSituation = lowerAns.contains("situation") || lowerAns.contains("project") || lowerAns.contains("when i") || lowerAns.contains("at my");
            boolean hasAction = lowerAns.contains("i decided") || lowerAns.contains("i implemented") || lowerAns.contains("i resolved") || lowerAns.contains("i worked") || lowerAns.contains("i learned");
            boolean hasResult = lowerAns.contains("result") || lowerAns.contains("outcome") || lowerAns.contains("finally") || lowerAns.contains("successful") || lowerAns.contains("impact");

            if (hasSituation) { score += 8; strengths.add("Clearly framed the situational background and project context."); }
            if (hasAction) { score += 10; strengths.add("Articulated specific proactive actions and problem-solving steps taken."); }
            if (hasResult) { score += 8; strengths.add("Demonstrated measurable results and key lessons learned."); }
            else { improvements.add("Include quantifiable results or explicit positive impact of your actions."); }

            if (wordCount < 40) {
                score -= 10;
                improvements.add("Expand your response using the STAR methodology (Situation, Task, Action, Result).");
            } else if (wordCount >= 70) {
                score += 5;
                strengths.add("Thorough, well-developed response with good storytelling depth.");
            }
        } else {
            // Technical Evaluation: keyword density and structural depth
            int matchedKeywords = 0;
            String[] techTerms = {"architecture", "algorithm", "performance", "complexity", "concurrency", "thread",
                    "database", "index", "cache", "async", "latency", "scale", "security", "component", "state", "test",
                    "transaction", "rollback", "reconciliation", "memory", "exception", "api", "design", "o(1)", "o(n)"};

            for (String term : techTerms) {
                if (lowerAns.contains(term)) {
                    matchedKeywords++;
                }
            }

            score += Math.min(25, matchedKeywords * 4);

            if (matchedKeywords >= 3) {
                strengths.add("Accurate use of core engineering terminology and principles.");
            }
            if (lowerAns.contains("example") || lowerAns.contains("project") || lowerAns.contains("production") || lowerAns.contains("in my code")) {
                score += 5;
                strengths.add("Grounded theoretical explanation with real-world implementation context.");
            } else {
                improvements.add("Illustrate your answer with a concrete code snippet or project example.");
            }

            if (wordCount < 40) {
                score -= 10;
                improvements.add("Provide a deeper technical breakdown of internal mechanics and trade-offs.");
            } else if (wordCount >= 80) {
                score += 5;
                strengths.add("Comprehensive explanation covering both fundamentals and architectural trade-offs.");
            }
        }

        // Clamp score between 40 and 96
        score = Math.max(40, Math.min(96, score));
        dto.setScore(score);

        if (strengths.isEmpty()) {
            strengths.add("Clear communication style and good flow of thoughts.");
        }
        if (improvements.isEmpty()) {
            improvements.add("Consider mentioning edge cases and system performance trade-offs.");
        }

        dto.setStrengths(strengths);
        dto.setImprovements(improvements);

        if (score >= 85) {
            dto.setFeedback("Excellent answer! You demonstrated solid command of the concepts, clear articulation, and good practical understanding.");
        } else if (score >= 70) {
            dto.setFeedback("Good response. You captured the main ideas well. Adding deeper architectural trade-offs or quantifiable project metrics will make your answer stand out.");
        } else {
            dto.setFeedback("Fair attempt. Your explanation covered basic aspects, but interviewers look for specific technical depth, exact mechanisms, and structured presentation.");
        }

        return dto;
    }

    public String generateConsolidatedFeedback(InterviewSessionState sessionState, double avgScore, int totalQuestions) {
        StringBuilder sb = new StringBuilder();
        sb.append("Performance Summary for ").append(sessionState.getInterviewType())
          .append(" Interview (Role: ").append(sessionState.getTargetRole()).append("):\n");

        if (avgScore >= 85.0) {
            sb.append("Strong Hire candidate! Outstanding technical clarity, structured communication, and deep conceptual knowledge displayed across all ")
              .append(totalQuestions).append(" questions.\n\n");
            sb.append("Key Strengths: Clear problem-solving approach, accurate terminology, and confident delivery.\n");
            sb.append("Recommendation: Continue refining large-scale system design and deep concurrency optimizations to target top tier positions.");
        } else if (avgScore >= 70.0) {
            sb.append("Solid performance! You demonstrated good fundamentals and readiness for entry to mid-level engineering assessments across ")
              .append(totalQuestions).append(" questions.\n\n");
            sb.append("Key Strengths: Good baseline knowledge and coherent explanations.\n");
            sb.append("Areas to Improve: Deepen your understanding of internal implementation details, edge cases, and ensure you cite specific metrics in project experiences.");
        } else {
            sb.append("Developing candidate. You completed ")
              .append(totalQuestions).append(" questions with commendable effort, but further preparation is recommended prior to company placement rounds.\n\n");
            sb.append("Focus Areas: Revise core fundamentals for ").append(sessionState.getTargetRole())
              .append(", practice formulating answers with the STAR method for behavioral queries, and articulate the 'why' behind architectural choices.");
        }

        return sb.toString();
    }
}
