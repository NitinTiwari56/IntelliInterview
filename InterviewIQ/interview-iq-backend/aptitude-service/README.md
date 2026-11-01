# aptitude-service (port 8084)

Aptitude test: ek-ek question, har question ka 2 min timer, server-side scoring, chhota result DB mein.
Questions `aptitude-question-service` (8091) se aate hain. API: `docs/api-contracts/aptitude.md`.

## Design
- Test ke dauran questions + answers **memory** mein (ActiveTest), DB mein nahi. DB mein sirf final result (~200 bytes + 4 category rows).
- Frontend ko ek time pe ek question (correct answer ke bina). Answers server pe rehte hain.
- Har question: 120 sec + 10 sec grace. Late answer = skip. Timer 0 pe frontend `selectedIndex: null` bhejta hai.
- Student chhod ke chala jaye: idle 5 min baad server test band karta hai (answers diye the toh result save, nahi toh kuch save nahi).
- Limit: service restart pe chal rahe tests chale jate hain (results safe). Ek hi instance pe chalta hai.

## Run
Pehle MySQL + aptitude-question-service (8091). Phir `.\mvnw.cmd spring-boot:run`.

## Config (application.yml, `aptitude:`)
`question-count` 20, `seconds-per-question` 120, `grace-seconds`, `min-valid-questions`.
Optional: `idle-timeout-seconds` (default 300), `review-ttl-minutes` (default 30).

## Purani tables (agar pehle wala version chalaya tha)
Ye ab use nahi hoti, drop kar sakte ho:
```sql
DROP TABLE IF EXISTS aptitude_test_questions;
DROP TABLE IF EXISTS aptitude_tests;
```
