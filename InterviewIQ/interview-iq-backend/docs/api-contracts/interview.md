# 5. Interview APIs (gateway: http://localhost:8080)

Sab endpoints mein header: `Authorization: Bearer <token>`
STATUS: backend testing chal rahi hai, field names thode badal sakte hain.

## Kaise kaam karta hai
- AI mock interview: **TECHNICAL** ya **HR**. Default 5 questions (`maxQuestions` se badlo).
- Ek-ek question: jawab do -> us jawab ka **score (0-100) + feedback + strengths + improvements** + agla question.
- Student ka latest **resume apne aap** use hota hai (skills, projects). Resume nahi hai toh sirf role ke basis pe questions.
- Aakhri jawab ke baad `completed: true` aur `sessionSummary` (final score + consolidated feedback).
- Abhi sirf **REST** hai (text answers). Speech ke liye browser ki Web Speech API se bol ke text banao aur wahi text bhejo.
  Live streaming (WebSocket) abhi gateway pe nahi hai.
- AI na chale toh rule-based engine se bhi interview chalta hai, student ko error nahi dikhta.

## POST /interview/sessions/start
Body (sab optional):
```json
{ "interviewType": "TECHNICAL", "inputMode": "TEXT", "targetRole": "Backend Engineer", "maxQuestions": 5 }
```
- `interviewType`: `TECHNICAL` | `HR`
- `inputMode`: `TEXT` | `SPEECH` | `VIDEO` | `SPEECH_TO_SPEECH` (backend mein sirf label hai, input hamesha text)
- `targetRole` na do toh resume wala role (nahi hai toh SDE)

Response `201`:
```json
{ "sessionId": "9c49ae9f-...", "studentId": 7, "interviewType": "TECHNICAL", "inputMode": "TEXT",
  "targetRole": "Backend Engineer", "status": "IN_PROGRESS",
  "currentQuestionNumber": 1, "currentQuestion": "Explain how a HashMap works...",
  "maxQuestions": 5, "questionCount": 0, "totalScore": null, "averageScore": null,
  "consolidatedFeedback": null, "questions": [], "startedAt": "...", "completedAt": null }
```
Question generation 2-8 sec le sakti hai (AI), loading dikhao.

## POST /interview/sessions/{sessionId}/answers
```json
{ "answer": "HashMap uses an array of buckets ..." }
```
Response:
```json
{ "sessionId": "...", "questionNumber": 1, "question": "...", "answer": "...",
  "score": 72, "feedback": "...", "strengths": ["..."], "improvements": ["..."],
  "completed": false, "nextQuestionNumber": 2, "nextQuestion": "...", "sessionSummary": null }
```
Aakhri question pe: `"completed": true`, `nextQuestion: null`, aur `sessionSummary` bhara hua (final score, feedback).
Errors: `400` khali/bahut lamba (5000 chars se zyada) jawab ya session already complete, `404` session nahi mila (ya kisi aur ka hai)

## GET /interview/sessions/{sessionId}
Poori session + `questions[]` (har question ka answer, score, feedback). Result page ke liye.

## POST /interview/sessions/{sessionId}/reconnect
Page refresh ke baad: wahi session, `currentQuestion` ke saath.

## POST /interview/sessions/{sessionId}/end
Beech mein band karo. Status `INCOMPLETE`. (`?markAsIncomplete=false` bhejoge toh complete maana jayega.)

## GET /interview/sessions
Meri saari sessions (naya pehle). History page ke liye.

## GET /interview/summary
Mera aggregated summary: `totalSessions`, `completedSessions`, `averageTechnicalScore`, `averageHrScore`, `overallAverageScore`, `recentSessions[]`.

## Errors
`{ "timestamp": "...", "status": 400, "error": "Bad Request", "message": "..." }` : `message` dikhao.
`401` token nahi, `404` nahi mila, `502/503/504` backend ki dikkat (dobara try karo).
