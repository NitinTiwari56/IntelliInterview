package com.nitin.AptituteQuestionsService.service;

import com.nitin.AptituteQuestionsService.model.DifficultyLevel;
import com.nitin.AptituteQuestionsService.model.Question;
import com.nitin.AptituteQuestionsService.model.QuestionCategory;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionBankService {

    private final List<Question> masterBank = new ArrayList<>();

    @PostConstruct
    public void init() {
        populateBank();
    }

    public List<Question> getRandomQuestions(int count, QuestionCategory categoryFilter,
                                             DifficultyLevel difficultyFilter, List<String> topicFilters) {
        List<Question> pool = new ArrayList<>(masterBank);

        if (categoryFilter != null) {
            pool = pool.stream()
                    .filter(q -> q.getCategory() == categoryFilter)
                    .collect(Collectors.toList());
        }

        if (difficultyFilter != null) {
            pool = pool.stream()
                    .filter(q -> q.getDifficulty() == difficultyFilter)
                    .collect(Collectors.toList());
        }

        if (topicFilters != null && !topicFilters.isEmpty()) {
            Set<String> lowerTopics = topicFilters.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
            pool = pool.stream()
                    .filter(q -> lowerTopics.contains(q.getTopic().toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (pool.isEmpty()) {
            pool = new ArrayList<>(masterBank);
        }

        List<Question> selected;
        // Balanced distribution across categories if categoryFilter is null and count >= 4
        if (categoryFilter == null && count >= 4) {
            selected = selectBalanced(pool, count);
        } else {
            Collections.shuffle(pool);
            selected = pool.stream().limit(count).collect(Collectors.toList());
        }

        // If pool was smaller than requested count, wrap around to ensure requested count is fulfilled
        if (selected.size() < count) {
            List<Question> extended = new ArrayList<>(selected);
            while (extended.size() < count) {
                extended.add(pool.get(extended.size() % pool.size()));
            }
            selected = extended;
        }

        // Clone and randomize options for freshness
        return selected.stream().map(this::cloneAndShuffleOptions).collect(Collectors.toList());
    }

    private List<Question> selectBalanced(List<Question> pool, int totalNeeded) {
        Map<QuestionCategory, List<Question>> byCategory = pool.stream()
                .collect(Collectors.groupingBy(Question::getCategory));

        for (List<Question> list : byCategory.values()) {
            Collections.shuffle(list);
        }

        int quantCount = (int) Math.round(totalNeeded * 0.35); // e.g. 7
        int logicalCount = (int) Math.round(totalNeeded * 0.35); // e.g. 7
        int verbalCount = (int) Math.round(totalNeeded * 0.20); // e.g. 4
        int diCount = totalNeeded - (quantCount + logicalCount + verbalCount); // e.g. 2

        Map<QuestionCategory, Integer> quota = new EnumMap<>(QuestionCategory.class);
        quota.put(QuestionCategory.QUANTITATIVE, quantCount);
        quota.put(QuestionCategory.LOGICAL_REASONING, logicalCount);
        quota.put(QuestionCategory.VERBAL_ABILITY, verbalCount);
        quota.put(QuestionCategory.DATA_INTERPRETATION, diCount);

        List<Question> result = new ArrayList<>();
        List<Question> remainingPool = new ArrayList<>();

        for (Map.Entry<QuestionCategory, Integer> entry : quota.entrySet()) {
            List<Question> catList = byCategory.getOrDefault(entry.getKey(), Collections.emptyList());
            int take = Math.min(catList.size(), entry.getValue());
            result.addAll(catList.subList(0, take));
            if (catList.size() > take) {
                remainingPool.addAll(catList.subList(take, catList.size()));
            }
        }

        if (result.size() < totalNeeded && !remainingPool.isEmpty()) {
            Collections.shuffle(remainingPool);
            int needed = totalNeeded - result.size();
            result.addAll(remainingPool.subList(0, Math.min(needed, remainingPool.size())));
        }

        Collections.shuffle(result);
        return result;
    }

    private Question cloneAndShuffleOptions(Question orig) {
        Question q = new Question();
        q.setId("APT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        q.setCategory(orig.getCategory());
        q.setTopic(orig.getTopic());
        q.setDifficulty(orig.getDifficulty());
        q.setQuestion(orig.getQuestion());
        q.setExplanation(orig.getExplanation());
        q.setMarks(orig.getMarks());

        // Shuffle options while preserving correct answer
        String correctOption = orig.getCorrectAnswer();
        List<String> options = new ArrayList<>(orig.getOptions());
        Collections.shuffle(options);

        q.setOptions(options);
        int newIndex = options.indexOf(correctOption);
        if (newIndex == -1) {
            newIndex = 0;
            correctOption = options.get(0);
        }
        q.setCorrectOptionIndex(newIndex);
        q.setCorrectAnswer(correctOption);

        return q;
    }

    public int getBankSize() {
        return masterBank.size();
    }

    public Map<String, Long> getCategoryCounts() {
        return masterBank.stream()
                .collect(Collectors.groupingBy(q -> q.getCategory().name(), Collectors.counting()));
    }

    private void populateBank() {
        // ==================== QUANTITATIVE APTITUDE ====================
        masterBank.add(new Question(
                "Q1", QuestionCategory.QUANTITATIVE, "Time and Work", DifficultyLevel.EASY,
                "A can complete a piece of work in 12 days, and B can complete it in 18 days. If they work together, in how many days will they finish the work?",
                List.of("7.2 days", "8.5 days", "6.4 days", "9.0 days"),
                0, "7.2 days",
                "Work done by A in 1 day = 1/12. Work done by B in 1 day = 1/18. Together in 1 day = 1/12 + 1/18 = 5/36. Total time = 36/5 = 7.2 days.", 1
        ));

        masterBank.add(new Question(
                "Q2", QuestionCategory.QUANTITATIVE, "Profit and Loss", DifficultyLevel.MEDIUM,
                "A shopkeeper sells an article at a discount of 20% on the marked price and still makes a profit of 25%. If the cost price is $480, what is the marked price?",
                List.of("$750", "$720", "$800", "$650"),
                0, "$750",
                "Cost Price (CP) = $480. Profit = 25%, so Selling Price (SP) = 480 * 1.25 = $600. Discount = 20%, so SP = 0.80 * Marked Price (MP). MP = 600 / 0.80 = $750.", 1
        ));

        masterBank.add(new Question(
                "Q3", QuestionCategory.QUANTITATIVE, "Speed, Time and Distance", DifficultyLevel.EASY,
                "A train 180 meters long is running at a speed of 72 km/h. How much time will it take to cross an electric pole?",
                List.of("9 seconds", "8 seconds", "10 seconds", "12 seconds"),
                0, "9 seconds",
                "Speed = 72 km/h = 72 * (5/18) = 20 m/s. Time to cross pole = Distance / Speed = 180 / 20 = 9 seconds.", 1
        ));

        masterBank.add(new Question(
                "Q4", QuestionCategory.QUANTITATIVE, "Percentages", DifficultyLevel.EASY,
                "If the price of sugar increases by 25%, by what percentage must a household reduce its consumption so that the total expenditure remains the same?",
                List.of("20%", "25%", "15%", "16.67%"),
                0, "20%",
                "Reduction in consumption = (R / (100 + R)) * 100 = (25 / 125) * 100 = 20%.", 1
        ));

        masterBank.add(new Question(
                "Q5", QuestionCategory.QUANTITATIVE, "Ratio and Proportion", DifficultyLevel.MEDIUM,
                "The ratio of the ages of two persons A and B is 4:5. After 6 years, the ratio of their ages will become 5:6. What is the present age of A?",
                List.of("24 years", "20 years", "28 years", "30 years"),
                0, "24 years",
                "Let ages be 4x and 5x. After 6 years: (4x + 6) / (5x + 6) = 5/6 => 24x + 36 = 25x + 30 => x = 6. Present age of A = 4 * 6 = 24 years.", 1
        ));

        masterBank.add(new Question(
                "Q6", QuestionCategory.QUANTITATIVE, "Simple and Compound Interest", DifficultyLevel.HARD,
                "The difference between compound interest (compounded annually) and simple interest on a sum of money for 2 years at 10% per annum is $85. What is the principal sum?",
                List.of("$8,500", "$8,000", "$9,000", "$7,500"),
                0, "$8,500",
                "Difference for 2 years = P * (R/100)^2. Here, 85 = P * (10/100)^2 = P * (1/100) => P = $8,500.", 1
        ));

        masterBank.add(new Question(
                "Q7", QuestionCategory.QUANTITATIVE, "Probability", DifficultyLevel.MEDIUM,
                "Two dice are thrown simultaneously. What is the probability of getting a total score of 7?",
                List.of("1/6", "1/12", "5/36", "7/36"),
                0, "1/6",
                "Total outcomes = 6 * 6 = 36. Favorable outcomes for sum 7: (1,6), (2,5), (3,4), (4,3), (5,2), (6,1) = 6 outcomes. Probability = 6/36 = 1/6.", 1
        ));

        masterBank.add(new Question(
                "Q8", QuestionCategory.QUANTITATIVE, "Permutations and Combinations", DifficultyLevel.MEDIUM,
                "In how many different ways can the letters of the word 'LEADER' be arranged?",
                List.of("360", "720", "180", "120"),
                0, "360",
                "Total letters = 6. Letter 'E' repeats 2 times. Total permutations = 6! / 2! = 720 / 2 = 360.", 1
        ));

        masterBank.add(new Question(
                "Q9", QuestionCategory.QUANTITATIVE, "Averages", DifficultyLevel.EASY,
                "The average score of a batsman in 10 innings was 32. How many runs must he score in his 11th inning to raise his average to 36?",
                List.of("76", "68", "72", "80"),
                0, "76",
                "Total runs in 10 innings = 10 * 32 = 320. Desired total in 11 innings = 11 * 36 = 396. Runs needed = 396 - 320 = 76.", 1
        ));

        masterBank.add(new Question(
                "Q10", QuestionCategory.QUANTITATIVE, "Pipes and Cisterns", DifficultyLevel.HARD,
                "Pipe A can fill a tank in 10 hours, and Pipe B can empty it in 15 hours. If both pipes are opened together, how long will it take to fill the tank completely?",
                List.of("30 hours", "25 hours", "20 hours", "35 hours"),
                0, "30 hours",
                "Net work in 1 hour = 1/10 - 1/15 = (3 - 2)/30 = 1/30. So tank fills completely in 30 hours.", 1
        ));

        masterBank.add(new Question(
                "Q11", QuestionCategory.QUANTITATIVE, "Number System", DifficultyLevel.EASY,
                "What is the greatest number that will divide 43, 91, and 183 so as to leave the same remainder in each case?",
                List.of("4", "7", "9", "13"),
                0, "4",
                "Required number = HCF of (91 - 43), (183 - 91), (183 - 43) = HCF of 48, 92, 140 = 4.", 1
        ));

        masterBank.add(new Question(
                "Q12", QuestionCategory.QUANTITATIVE, "Speed, Time and Distance", DifficultyLevel.MEDIUM,
                "A person travels from city A to B at 60 km/h and returns at 40 km/h. What is his average speed for the whole journey?",
                List.of("48 km/h", "50 km/h", "52 km/h", "45 km/h"),
                0, "48 km/h",
                "Average speed for equal distances = (2 * S1 * S2) / (S1 + S2) = (2 * 60 * 40) / (60 + 40) = 4800 / 100 = 48 km/h.", 1
        ));

        // ==================== LOGICAL REASONING ====================
        masterBank.add(new Question(
                "Q13", QuestionCategory.LOGICAL_REASONING, "Blood Relations", DifficultyLevel.EASY,
                "Pointing to a photograph of a man, Rahul said, 'He is the son of the only son of my grandfather.' How is Rahul related to the man in the photograph?",
                List.of("Brother or Himself", "Uncle", "Father", "Cousin"),
                0, "Brother or Himself",
                "The only son of Rahul's grandfather is Rahul's father. The son of Rahul's father is either Rahul himself or his brother.", 1
        ));

        masterBank.add(new Question(
                "Q14", QuestionCategory.LOGICAL_REASONING, "Coding and Decoding", DifficultyLevel.EASY,
                "If 'ROSE' is coded as '6821' and 'CHAIR' is coded as '73456', how will 'SEARCH' be coded?",
                List.of("214673", "214567", "214736", "241673"),
                0, "214673",
                "Corresponding letters: S=2, E=1, A=4, R=6, C=7, H=3. Hence, SEARCH = 214673.", 1
        ));

        masterBank.add(new Question(
                "Q15", QuestionCategory.LOGICAL_REASONING, "Number Series", DifficultyLevel.MEDIUM,
                "Find the missing number in the sequence: 4, 9, 25, 49, 121, 169, ?",
                List.of("289", "225", "196", "361"),
                0, "289",
                "The numbers are squares of consecutive prime numbers: 2^2=4, 3^2=9, 5^2=25, 7^2=49, 11^2=121, 13^2=169. The next prime number is 17, and 17^2 = 289.", 1
        ));

        masterBank.add(new Question(
                "Q16", QuestionCategory.LOGICAL_REASONING, "Direction Sense", DifficultyLevel.EASY,
                "Karan walks 10 meters toward North, turns right and walks 15 meters, then turns right again and walks 10 meters. In which direction is he now from his starting point?",
                List.of("East", "West", "North", "South"),
                0, "East",
                "Karan moved North 10m, East 15m, then South 10m. He is directly 15 meters East of his original position.", 1
        ));

        masterBank.add(new Question(
                "Q17", QuestionCategory.LOGICAL_REASONING, "Syllogisms", DifficultyLevel.MEDIUM,
                "Statements:\n1. All cars are vehicles.\n2. All vehicles are machines.\nConclusions:\nI. All cars are machines.\nII. Some machines are cars.",
                List.of("Both I and II follow", "Only I follows", "Only II follows", "Neither follows"),
                0, "Both I and II follow",
                "Since Cars ⊆ Vehicles ⊆ Machines, all cars are machines (Conclusion I). Since some elements of Machines are Cars, Conclusion II also follows.", 1
        ));

        masterBank.add(new Question(
                "Q18", QuestionCategory.LOGICAL_REASONING, "Seating Arrangement", DifficultyLevel.HARD,
                "Five friends P, Q, R, S, and T are sitting around a circular table facing the center. P is to the immediate right of Q. R is between P and S. Who is sitting to the immediate left of S?",
                List.of("R", "T", "Q", "P"),
                0, "R",
                "Facing center clockwise: Q -> P -> R -> S -> T -> Q. R is immediately between P and S, which places R to the immediate left/adjacent side of S facing center.", 1
        ));

        masterBank.add(new Question(
                "Q19", QuestionCategory.LOGICAL_REASONING, "Statement and Assumptions", DifficultyLevel.MEDIUM,
                "Statement: 'Please read the terms and conditions carefully before signing this agreement.'\nAssumptions:\nI. People may sign without reading.\nII. Reading terms and conditions is important.",
                List.of("Both I and II are implicit", "Only I is implicit", "Only II is implicit", "Neither is implicit"),
                0, "Both I and II are implicit",
                "The advisory is given specifically because people tend to overlook agreements (I) and understanding the legal obligations is vital (II).", 1
        ));

        masterBank.add(new Question(
                "Q20", QuestionCategory.LOGICAL_REASONING, "Clock and Calendar", DifficultyLevel.HARD,
                "At what time between 3 o'clock and 4 o'clock will the hands of a clock be together (0 degrees angle)?",
                List.of("16 4/11 minutes past 3", "15 minutes past 3", "16 7/11 minutes past 3", "18 minutes past 3"),
                0, "16 4/11 minutes past 3",
                "Formula for coincidence: Minute = (Hour * 30 * 2) / 11 = (3 * 30 * 2) / 11 = 180 / 11 = 16 4/11 minutes past 3.", 1
        ));

        masterBank.add(new Question(
                "Q21", QuestionCategory.LOGICAL_REASONING, "Analogy", DifficultyLevel.EASY,
                "Book : Author :: Symphony : ?",
                List.of("Composer", "Conductor", "Musician", "Orchestra"),
                0, "Composer",
                "A book is written/created by an author; a symphony is composed/created by a composer.", 1
        ));

        masterBank.add(new Question(
                "Q22", QuestionCategory.LOGICAL_REASONING, "Letter Series", DifficultyLevel.MEDIUM,
                "Complete the series: B2D, D4F, F6H, H8J, ?",
                List.of("J10L", "I10K", "J10K", "K10M"),
                0, "J10L",
                "First letter moves +2 (B, D, F, H, J). Number increments by 2 (2, 4, 6, 8, 10). Third letter moves +2 (D, F, H, J, L). Hence J10L.", 1
        ));

        // ==================== VERBAL ABILITY ====================
        masterBank.add(new Question(
                "Q23", QuestionCategory.VERBAL_ABILITY, "Synonyms", DifficultyLevel.EASY,
                "Choose the word that is closest in meaning to 'METICULOUS':",
                List.of("Painstaking", "Careless", "Rapid", "Casual"),
                0, "Painstaking",
                "'Meticulous' means showing great attention to detail; very careful and precise. 'Painstaking' has the same meaning.", 1
        ));

        masterBank.add(new Question(
                "Q24", QuestionCategory.VERBAL_ABILITY, "Antonyms", DifficultyLevel.EASY,
                "Choose the word that is opposite in meaning to 'CANDID':",
                List.of("Deceitful", "Blunt", "Honest", "Frank"),
                0, "Deceitful",
                "'Candid' means truthful, straightforward, and sincere. Its opposite is 'deceitful' or 'guarded'.", 1
        ));

        masterBank.add(new Question(
                "Q25", QuestionCategory.VERBAL_ABILITY, "Sentence Correction", DifficultyLevel.MEDIUM,
                "Identify the incorrect part of the sentence: 'Neither of the two candidates (A) / have submitted their application (B) / before the deadline (C) / No error (D)'",
                List.of("Part (B)", "Part (A)", "Part (C)", "Part (D)"),
                0, "Part (B)",
                "'Neither' is a singular pronoun and takes a singular verb. Part (B) should be 'has submitted his/her application' instead of 'have submitted their application'.", 1
        ));

        masterBank.add(new Question(
                "Q26", QuestionCategory.VERBAL_ABILITY, "Idioms and Phrases", DifficultyLevel.EASY,
                "What is the meaning of the idiom 'To burn the midnight oil'?",
                List.of("To work late into the night", "To waste precious resources", "To create unnecessary danger", "To illuminate dark areas"),
                0, "To work late into the night",
                "'Burn the midnight oil' is an idiom meaning to study or work hard until late at night.", 1
        ));

        masterBank.add(new Question(
                "Q27", QuestionCategory.VERBAL_ABILITY, "Fill in the Blanks", DifficultyLevel.MEDIUM,
                "The CEO's vision for renewable energy was so ______ that even seasoned skeptics began to endorse the initiative.",
                List.of("compelling", "nebulous", "pedestrian", "ephemeral"),
                0, "compelling",
                "'Compelling' means evoking interest, attention, or admiration in a powerfully irresistible way.", 1
        ));

        masterBank.add(new Question(
                "Q28", QuestionCategory.VERBAL_ABILITY, "Para Jumbles", DifficultyLevel.HARD,
                "Arrange the sentences in logical order:\nP: He was a man of few words.\nQ: Yet his decisions carried immense weight across the entire organization.\nR: Leaders often speak frequently to inspire confidence.\nS: But his calm silence inspired even deeper trust.",
                List.of("R - P - Q - S", "P - Q - R - S", "S - Q - P - R", "R - S - P - Q"),
                0, "R - P - Q - S",
                "R introduces the general observation about leadership speech. P introduces the subject who speaks little. Q explains the influence despite this. S contrasts his silence with the talkative approach.", 1
        ));

        masterBank.add(new Question(
                "Q29", QuestionCategory.VERBAL_ABILITY, "Reading Comprehension", DifficultyLevel.MEDIUM,
                "Statement: 'Quantum computing does not simply execute classical operations faster; it computes in an entirely distinct state space using superposition and entanglement.' What can be inferred?",
                List.of("Quantum computers solve certain classes of problems fundamentally differently from classical computers.", "Quantum computers will immediately replace all silicon processors.", "Superposition causes classical computers to fail.", "Quantum computing only works at optical speeds."),
                0, "Quantum computers solve certain classes of problems fundamentally differently from classical computers.",
                "The statement explicitly emphasizes computing in an entirely distinct state space rather than just speeding up classical methods.", 1
        ));

        masterBank.add(new Question(
                "Q30", QuestionCategory.VERBAL_ABILITY, "Spotting Errors", DifficultyLevel.EASY,
                "Identify the error in: 'Each of the participants (A) / were given a certificate (B) / of completion (C) / No error (D)'",
                List.of("Part (B)", "Part (A)", "Part (C)", "Part (D)"),
                0, "Part (B)",
                "'Each' takes a singular verb. The correct phrasing is 'was given a certificate', not 'were given'.", 1
        ));

        // ==================== DATA INTERPRETATION ====================
        masterBank.add(new Question(
                "Q31", QuestionCategory.DATA_INTERPRETATION, "Table Analysis", DifficultyLevel.MEDIUM,
                "A company's quarterly revenue (in $ millions) is: Q1: 120, Q2: 150, Q3: 180, Q4: 210. What is the percentage growth in revenue from Q1 to Q4?",
                List.of("75%", "60%", "90%", "85%"),
                0, "75%",
                "Growth = ((210 - 120) / 120) * 100 = (90 / 120) * 100 = 75%.", 1
        ));

        masterBank.add(new Question(
                "Q32", QuestionCategory.DATA_INTERPRETATION, "Pie Charts", DifficultyLevel.EASY,
                "In an expenditure budget represented by a 360° pie chart, the sector for Employee Salaries subtends an angle of 108°. What percentage of total expenditure is spent on salaries?",
                List.of("30%", "25%", "35%", "28%"),
                0, "30%",
                "Percentage = (108 / 360) * 100 = 0.30 * 100 = 30%.", 1
        ));

        masterBank.add(new Question(
                "Q33", QuestionCategory.DATA_INTERPRETATION, "Bar Graphs", DifficultyLevel.MEDIUM,
                "Factory production of widgets over 3 years: Year 1 = 40,000 units; Year 2 = 50,000 units; Year 3 = 65,000 units. What is the average annual production?",
                List.of("51,667 units", "50,000 units", "53,333 units", "48,500 units"),
                0, "51,667 units",
                "Average = (40,000 + 50,000 + 65,000) / 3 = 155,000 / 3 = 51,666.67 ≈ 51,667 units.", 1
        ));

        masterBank.add(new Question(
                "Q34", QuestionCategory.DATA_INTERPRETATION, "Caselet Analysis", DifficultyLevel.HARD,
                "In a college with 800 students, 60% study Computer Science, 50% study Mathematics, and 30% study both. How many students study neither subject?",
                List.of("160 students", "120 students", "200 students", "140 students"),
                0, "160 students",
                "Total studying CS or Math = 60% + 50% - 30% = 80%. Percentage studying neither = 100% - 80% = 20%. Number of students = 20% of 800 = 160 students.", 1
        ));

        masterBank.add(new Question(
                "Q35", QuestionCategory.DATA_INTERPRETATION, "Line Graphs", DifficultyLevel.MEDIUM,
                "A stock's monthly closing prices were Jan: $100, Feb: $120, Mar: $90. What is the overall percentage change from Jan to Mar?",
                List.of("-10%", "-5%", "+10%", "0%"),
                0, "-10%",
                "Net change = ((90 - 100) / 100) * 100 = -10%.", 1
        ));

        masterBank.add(new Question(
                "Q36", QuestionCategory.QUANTITATIVE, "Permutations and Combinations", DifficultyLevel.HARD,
                "From a group of 7 men and 6 women, 5 persons are to be selected to form a committee so that at least 3 men are there on the committee. In how many ways can it be done?",
                List.of("756", "642", "812", "720"),
                0, "756",
                "Cases: (3 Men & 2 Women): 7C3 * 6C2 = 35 * 15 = 525. (4 Men & 1 Woman): 7C4 * 6C1 = 35 * 6 = 210. (5 Men & 0 Women): 7C5 * 6C0 = 21 * 1 = 21. Total ways = 525 + 210 + 21 = 756.", 1
        ));

        masterBank.add(new Question(
                "Q37", QuestionCategory.LOGICAL_REASONING, "Number Series", DifficultyLevel.MEDIUM,
                "Find the next number in the series: 3, 10, 29, 66, 127, ?",
                List.of("218", "216", "225", "214"),
                0, "218",
                "Pattern is n^3 + 2: 1^3+2=3, 2^3+2=10, 3^3+2=29, 4^3+2=66, 5^3+2=127. Next term is 6^3+2 = 216 + 2 = 218.", 1
        ));

        masterBank.add(new Question(
                "Q38", QuestionCategory.QUANTITATIVE, "Profit and Loss", DifficultyLevel.EASY,
                "If an article is bought for $250 and sold for $300, what is the profit percentage?",
                List.of("20%", "25%", "15%", "18%"),
                0, "20%",
                "Profit = $300 - $250 = $50. Profit % = (50 / 250) * 100 = 20%.", 1
        ));

        masterBank.add(new Question(
                "Q39", QuestionCategory.VERBAL_ABILITY, "Synonyms", DifficultyLevel.MEDIUM,
                "Choose the synonym for 'LACONIC':",
                List.of("Concise", "Verbose", "Lethargic", "Eloquent"),
                0, "Concise",
                "'Laconic' means using very few words to express what you mean; concise or brief.", 1
        ));

        masterBank.add(new Question(
                "Q40", QuestionCategory.QUANTITATIVE, "Ages", DifficultyLevel.EASY,
                "A father is twice as old as his son. Twenty years ago, the father was four times as old as his son. What is the current age of the father?",
                List.of("60 years", "50 years", "40 years", "70 years"),
                0, "60 years",
                "Let son's age be s, father's age be 2s. (2s - 20) = 4 * (s - 20) => 2s - 20 = 4s - 80 => 2s = 60 => s = 30. Father's age = 60 years.", 1
        ));
    }
}
