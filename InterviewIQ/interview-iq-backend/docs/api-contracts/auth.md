# Auth Service API (port 8081)

Base URL (abhi, direct): `http://localhost:8081`
Gateway aane ke baad: `http://localhost:8080` (paths same rahenge)

## POST /auth/register
Request
```json
{ "name": "Khushi", "email": "khushi@example.com", "password": "Strong@123" }
```
Response `201 Created`
```json
{ "id": 1, "name": "Khushi", "email": "khushi@example.com", "role": "STUDENT" }
```
Errors: `400` validation, `409` Email already exists

## POST /auth/login
Request
```json
{ "email": "khushi@example.com", "password": "Strong@123" }
```
Response `200 OK`
```json
{
  "token": "<jwt>",
  "user": { "id": 1, "name": "Khushi", "email": "khushi@example.com", "role": "STUDENT" }
}
```
Errors: `400` validation, `401` Invalid email or password

## Common error format (saari services mein same)
```json
{ "status": 409, "message": "Email already exists", "timestamp": "2026-10-01T15:30:00Z" }
```

## Protected APIs ke liye
Header: `Authorization: Bearer <token>`

## JWT contents
- `sub` = userId
- `email`, `role` (STUDENT / ADMIN)
- expiry: 24 hours

## Notes
- Password rules: 8-64 characters
- Target role registration mein nahi hai; wo Resume upload ke time per-analysis bheja jayega.
