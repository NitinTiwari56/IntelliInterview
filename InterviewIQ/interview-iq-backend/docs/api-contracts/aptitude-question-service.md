# Aptitude Question Service (AI member ka service) - port 8091

**INTERNAL service.** Frontend isko direct call NAHI karega. Sirf backend `aptitude-service` (port 8084) call karega.
Wajah: response mein `correctAnswer`, `correctOptionIndex`, `explanation` hote hain. Agar ye browser tak gaye toh student DevTools mein answers dekh sakta hai.
`aptitude-service` ye fields strip karke frontend ko bhejegi aur answers server pe rakh kar score karegi.

## POST /api/v1/aptitude/generate
Request (sab optional)
```json
{ "count": 20, "category": "ALL", "difficulty": "MIXED", "source": "AUTO", "topics": ["Percentages"] }
```
- category: ALL | QUANTITATIVE | LOGICAL_REASONING | VERBAL_ABILITY | DATA_INTERPRETATION
- difficulty: MIXED | EASY | MEDIUM | HARD
- source: AUTO (Groq, fail hua toh bank) | AI | BANK

Response `200`
```json
{
  "success": true,
  "message": "...",
  "count": 20,
  "source": "AI_GENERATED | QUESTION_BANK",
  "timestamp": "2026-10-01T15:30:00Z",
  "categoryDistribution": { "QUANTITATIVE": 7, "LOGICAL_REASONING": 7, "VERBAL_ABILITY": 4, "DATA_INTERPRETATION": 2 },
  "questions": [
    {
      "id": "APT-1A2B3C4D",
      "category": "QUANTITATIVE",
      "topic": "Time and Work",
      "difficulty": "EASY",
      "question": "...",
      "options": ["...", "...", "...", "..."],
      "correctOptionIndex": 0,
      "correctAnswer": "...",
      "explanation": "...",
      "marks": 1
    }
  ]
}
```
Note: field ka naam **`count`** hai (service ke README mein galti se `totalQuestions` likha tha, wo fix kar diya).

## GET /api/v1/aptitude/generate?count=20&category=ALL&difficulty=MIXED
Quick test ke liye.

## GET /api/v1/aptitude/categories  |  GET /api/v1/aptitude/health
Categories/topics ki list, aur health (groqConfigured, bank size).

## Run
- JDK **21** chahiye (pom mein `java.version` 21).
- Groq key env variable se: `GROQ_API_KEY` (git mein kabhi mat daalo). Key nahi hai toh bhi chalega, bank se questions dega.
- `./mvnw spring-boot:run`  (Windows: `.\mvnw.cmd spring-boot:run`)
