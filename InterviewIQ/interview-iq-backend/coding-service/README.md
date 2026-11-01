# coding-service (port 8083)

Problems, Run/Submit (Judge0), submissions, stats. API: `docs/api-contracts/coding.md`. Problems kaise add karein: `docs/problems/README.md`.

## Run
Pehle MySQL chalu. Phir:
```
.\mvnw.cmd spring-boot:run
```
Pehli baar 7 sample problems apne aap seed ho jati hain (`coding.seed-sample-problems`).

## Judge0
Default: public instance `https://ce.judge0.com` (free, bina key, per-IP throttling; dev/demo ke liye).
Self-hosted ya key chahiye toh env variables: `JUDGE0_URL`, `JUDGE0_API_KEY_HEADER`, `JUDGE0_API_KEY`.
Language IDs verify karo: browser mein `https://ce.judge0.com/languages` kholo, `application.yml` ke `judge0.language-ids` se match karo.
