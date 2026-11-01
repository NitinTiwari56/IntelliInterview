# Interview IQ frontend (React + Vite)

## Run
1. Backend first (see backend README): `docker compose up -d`, then `mvn spring-boot:run` inside auth-service (port 8081)
2. Here:  npm install   then   npm run dev   -> http://localhost:5173
3. API address is in `.env` (VITE_API_URL). After the API Gateway is ready change it to http://localhost:8080

## Auth structure
- src/auth/api.js          token storage + api() helper (adds Bearer token to every call)
- src/auth/AuthContext.jsx useAuth() -> user, token, isAuthed, login, register, logout
- src/auth/ProtectedRoute.jsx  protects pages (optional role="ADMIN")
- src/pages/Auth.jsx       login + signup page
- src/pages/Dashboard.jsx  placeholder protected page
