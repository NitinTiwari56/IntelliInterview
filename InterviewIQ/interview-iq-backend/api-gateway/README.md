# api-gateway (port 8080)

Single entry point. Routing + JWT verification + CORS. Routes aur rules: `docs/api-contracts/gateway.md`.

Run: `.\mvnw.cmd spring-boot:run` (Windows) ya `./mvnw spring-boot:run`
Pehle auth-service (8081) aur MySQL chalu hona chahiye.
