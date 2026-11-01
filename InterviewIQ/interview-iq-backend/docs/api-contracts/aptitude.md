# 3. Aptitude APIs (gateway: http://localhost:8080)

Sab endpoints mein header: `Authorization: Bearer <token>`

STATUS: backend testing chal rahi hai, field names final hone se pehle thode badal sakte hain.

## Kaise kaam karta hai
- Test = 20 questions, **ek time pe ek question** aata hai (correct answer ke bina).
- **Har question ka apna 2 min (120 sec) ka timer**, server pe enforce hota hai.
- Student ne option chuna toh jawab jata hai. Timer 0 hone tak kuch nahi chuna toh frontend `selectedIndex: null` bhejta hai (skip, auto-submit).
- Sahi jawab pe marks badhte hain (backend mein counter). **Test ke dauran sahi/galat nahi dikhana**, result sirf end mein.
- Koi category/difficulty selection nahi. Negative marking nahi.
- Page refresh pe `GET /aptitude/tests/current` se wahi question aur bacha hua time wapas milta hai.

## Frontend ka loop
1. `POST /aptitude/tests` -> pehla question
2. `question.remainingSeconds` se countdown chalao
3. Option click / Next / timer 0 -> `POST /aptitude/tests/{testId}/answer` -> agla question (naya countdown)
4. `status: "COMPLETED"` aaye toh `result` dikhao
5. `previousTimedOut: true` ho toh chhota message dikhao ("Time khatam, pichla question skip hua")

## POST /aptitude/tests   (body nahi)
Test shuru karo. Chal raha test hai toh wahi wapas milta hai. Questions AI se aate hain, 2-10 sec lag sakte hain, loading dikhao.
```json
{
  "testId": "b1c7e0f2-....",
  "status": "IN_PROGRESS",
  "previousTimedOut": false,
  "result": null,
  "question": {
    "index": 1, "total": 20,
    "category": "QUANTITATIVE", "topic": "Percentages",
    "question": "...", "options": ["A text", "B text", "C text", "D text"],
    "marks": 1, "remainingSeconds": 120, "secondsPerQuestion": 120
  }
}
```
Errors: `503` question service down, `502` valid test nahi ban paya (dobara try karo)

## GET /aptitude/tests/current
Same shape. Test nahi chal raha toh `404 No active test`. Time nikal chuka ho toh us question ko skip karke agla deta hai (`previousTimedOut: true`).

## POST /aptitude/tests/{testId}/answer
```json
{ "questionIndex": 1, "selectedIndex": 2 }
```
- `questionIndex`: abhi dikh rahe question ka `index`
- `selectedIndex`: 0-3 (option ka index), ya `null` = kuch nahi chuna / skip

Response: `TestStateResponse` (upar wali shape). `IN_PROGRESS` + agla `question`, ya `COMPLETED` + `result`.
Late answer (2 min + 10 sec grace ke baad) maanya nahi: skip hota hai, `previousTimedOut: true` aata hai.
Errors: `404` test nahi mila, `409` test khatam ho chuka ya ye current question nahi hai (`GET /tests/current` se sync karo), `400` galat option

## Result (COMPLETED hone pe `result` mein)
```json
{
  "resultId": 12, "score": 14, "totalMarks": 20, "percentage": 70.0,
  "totalQuestions": 20, "correctCount": 14, "incorrectCount": 4, "skippedCount": 2,
  "autoEnded": false, "submittedAt": "2026-10-04T10:40:00Z",
  "categoryBreakdown": [ { "category": "QUANTITATIVE", "total": 7, "correct": 5, "accuracy": 71.4 } ],
  "review": [
    { "position": 1, "category": "QUANTITATIVE", "topic": "Percentages", "question": "...",
      "options": ["..","..","..",".."], "selectedIndex": 2, "correctIndex": 1, "correct": false,
      "explanation": "..." }
  ]
}
```
`autoEnded: true` = student beech mein chhod gaya, server ne band kiya.

## GET /aptitude/tests/{testId}/result
Test ke baad **~30 min tak** wahi result (review ke saath). Uske baad `404`, tab history se summary dekho.

## GET /aptitude/results  (history)
```json
[ { "resultId": 12, "score": 14, "totalMarks": 20, "percentage": 70.0, "submittedAt": "..." } ]
```

## GET /aptitude/results/{resultId}
Wahi result, par `review: null` (review DB mein save nahi hota).

## GET /aptitude/stats  (category-wise, saare attempts)
```json
[ { "category": "QUANTITATIVE", "total": 21, "correct": 15, "accuracy": 71.4 } ]
```

## GET /aptitude/health
`{ "status": "UP", "service": "aptitude-service" }`

## Error format
`{ "status": 400, "message": "...", "timestamp": "..." }`
