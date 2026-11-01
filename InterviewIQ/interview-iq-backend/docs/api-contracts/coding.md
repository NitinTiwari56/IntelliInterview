# Coding Service API (frontend gateway ke through: http://localhost:8080)

Sab endpoints mein header: `Authorization: Bearer <token>`

Rules:
- Problems **stdin/stdout** style ki hain: student code input stdin se padhta hai, answer print karta hai.
- Languages: `java`, `python`, `cpp`, `c`, `javascript`. (Java mein class ka naam `Main` hona chahiye.)
- **Run** = sirf sample test cases (ya custom input), kuch save nahi hota.
- **Submit** = saare test cases (hidden bhi), result save hota hai.
- Run/Submit ke beech **5 second ka cooldown** (`429` aata hai). UI mein button 5 sec disable karo.
- Execution 2-10 sec le sakti hai, loading state dikhao.

## GET /coding/languages
```json
[ { "key": "java", "label": "Java", "starterCode": "import java.util.*; ..." } ]
```
Editor mein language badalne pe `starterCode` daalo.

## GET /coding/problems?difficulty=EASY&topic=Arrays   (dono filter optional)
```json
[ { "id": 1, "slug": "sum-of-two-numbers", "title": "Sum of Two Numbers",
    "difficulty": "EASY", "topic": "Math", "status": "TODO" } ]
```
`status`: `TODO` | `ATTEMPTED` | `SOLVED` (is user ke liye)

## GET /coding/problems/{slug}
```json
{ "id": 1, "slug": "...", "title": "...", "description": "<markdown>", "difficulty": "EASY", "topic": "Math",
  "timeLimitSeconds": 2.0, "memoryLimitKb": 256000,
  "samples": [ { "input": "3 5\n", "expectedOutput": "8\n" } ] }
```
Errors: `404` problem nahi mili

## POST /coding/problems/{slug}/run
```json
{ "language": "python", "sourceCode": "a, b = map(int, input().split())\nprint(a + b)" }
```
Custom input ke saath (sirf us input pe chalega): `"customInput": "10 20\n"` bhi bhejo.
```json
{ "verdict": "ACCEPTED", "compileOutput": null,
  "results": [ { "caseNumber": 1, "status": "PASSED", "input": "3 5\n", "expectedOutput": "8\n",
                 "actualOutput": "8\n", "stderr": "", "timeMs": 12 } ] }
```
- `verdict`: ACCEPTED | WRONG_ANSWER | COMPILATION_ERROR | RUNTIME_ERROR | TIME_LIMIT_EXCEEDED, custom input pe `COMPLETED`
- `results[].status`: PASSED | FAILED | RUNTIME_ERROR | TIME_LIMIT_EXCEEDED | COMPILATION_ERROR, custom input pe `RAN`
- `compileOutput`: compile error ka text (UI mein dikhao)

## POST /coding/problems/{slug}/submit
Body run jaisa (customInput nahi).
```json
{ "submissionId": 5, "verdict": "WRONG_ANSWER", "passedTests": 2, "totalTests": 4,
  "maxTimeMs": 15, "maxMemoryKb": 9000, "message": null,
  "failedCase": { "input": "3 5\n", "expectedOutput": "8\n", "actualOutput": "0\n" },
  "failedOnHiddenTest": false }
```
- `failedCase` sirf tab aata hai jab **sample** test fail ho. Hidden test fail ho toh `failedOnHiddenTest: true` (input/expected kabhi nahi dikhte).
- `message`: compile error ka text ya runtime error (stderr) ya "Time limit exceeded on test N".

## Submissions
- `GET /coding/submissions` : mere latest 50 submissions
- `GET /coding/problems/{slug}/submissions` : is problem pe mere saare submissions
- `GET /coding/submissions/{id}` : detail, `sourceCode` ke saath
```json
{ "id": 5, "problemSlug": "...", "problemTitle": "...", "language": "python", "verdict": "ACCEPTED",
  "passedTests": 4, "totalTests": 4, "maxTimeMs": 15, "maxMemoryKb": 9000, "createdAt": "..." }
```

## GET /coding/stats   (dashboard ke liye)
```json
{ "totalSubmissions": 12, "acceptedSubmissions": 5, "acceptanceRate": 41.7,
  "problemsAttempted": 4, "problemsSolved": 3,
  "byDifficulty": [ { "difficulty": "EASY", "solved": 3, "total": 5 }, { "difficulty": "MEDIUM", "solved": 0, "total": 2 }, { "difficulty": "HARD", "solved": 0, "total": 0 } ],
  "byTopic": [ { "topic": "Arrays", "solved": 1, "attempted": 2 } ] }
```

## Errors (format: status, message, timestamp)
`400` galat input/language, `404` not found, `429` bahut jaldi dobara run, `502/503/504` code execution service ki dikkat (dobara try karo)

## Admin (sirf role ADMIN; frontend mein admin page banao toh)
- `GET /coding/admin/problems`, `GET /coding/admin/problems/{id}`
- `POST /coding/admin/problems`, `PUT /coding/admin/problems/{id}`
- `POST /coding/admin/problems/import` (bulk). Details: `docs/problems/README.md`
