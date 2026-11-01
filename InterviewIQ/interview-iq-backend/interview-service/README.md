# interview-service (port 8085)

AI mock interviews (Technical / HR): question generation, rubric scoring, feedback, session history.
Contract: `docs/api-contracts/interview.md`. AI member ka original service hai; integration ke liye badla gaya.
Original README: `README-ai-member.md` (uske paths/ports ab purane hain).

## Run
Pehle MySQL (aur resume-service optional). `.\mvnw.cmd spring-boot:run` (JDK 21).
DB password `src/main/resources/application.properties` mein (ya env `DB_PASSWORD`). Optional AI: `$env:GROQ_API_KEY = "gsk_..."`.

## Integration changes (original se)
- Port 8082 -> **8085** (8082 resume-service ka hai). Package `com.interviewiq.interview`.
- Paths `/api/interviews/**` -> **`/interview/**`**.
- `studentId` body/path se hata diya: user **X-User-Id** header se (gateway ke verified JWT se). Original mein koi bhi
  `/student/7` ya sessionId se doosre ka interview dekh/jawab de sakta tha. Ab session sirf owner ka (warna 404).
- Resume service se student ka latest resume apne aap (`/internal/resume/latest/{id}`), pehle client se bhejna padta tha.
  Resume nahi hai toh role-based questions (REQ-INT-15).
- Analytics ke liye `/internal/interview/{studentId}/summary` (gateway pe route nahi).
- H2 in-memory default -> **MySQL** (`interview_db`), H2 sirf tests mein. `api-key=${API_KEY}` (bina default) -> `${GROQ_API_KEY:}`.
- CORS service se hata diya. Galat JSON/enum pe 400 (pehle 500), 500 mein internal text nahi jata, khali answer reject.

## Limits
- **WebSocket** (`/ws/interview`) code maujood hai par gateway pe route nahi, aur usme studentId message se aata hai
  (unauthenticated). Isliye frontend REST use karega. WebSocket + token handshake future scope.
- Active session state memory mein (restart pe chal rahi sessions jati hain, DB se `reconnect` se wapas rehydrate hoti hain jahan tak ho sake).
- Question text token-by-token streaming REST pe nahi hai.
