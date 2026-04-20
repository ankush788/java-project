# API Gateway Service - API Reference

## Health Check Endpoint

**Endpoint**: `GET /gateway/health`

**What it does**:
- Checks if API Gateway is running
- Used for monitoring and container orchestration
- No authentication required

**Curl**:
```bash
curl -X GET http://localhost:8080/gateway/health
```

**Response**:
```json
{
  "status": "UP",
  "service": "API Gateway Service"
}
```

---

## Token Validation Endpoint

**Endpoint**: `POST /gateway/validate-token`

**What it does**:
- Validates JWT tokens without making protected requests
- Returns token details and expiration info
- Checks token signature and claims

**Curl**:
```bash
curl -X POST http://localhost:8080/gateway/validate-token \
  -H "Authorization: Bearer <jwt-token>"
```

**Response**:
```json
{
  "valid": true,
  "userId": "user123",
  "issuedAt": "2026-04-20T10:30:00Z",
  "expiresAt": "2026-04-21T10:30:00Z"
}
```

---

## Login Endpoint

**Endpoint**: `POST /auth/login`

**What it does**:
- Authenticates user with credentials
- Returns JWT token for authenticated user
- Token used for subsequent protected requests
- No JWT validation required

**Curl**:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}'
```

---

## Register Endpoint

**Endpoint**: `POST /auth/register`

**What it does**:
- Creates new user account
- No JWT validation required
- Allows registration of new users

**Curl**:
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "newuser", "email": "user@example.com", "password": "password123"}'
```

---

## User Management Routes

**Endpoint**: `GET/POST/PUT/DELETE /user/**`

**What it does**:
- Accesses user management APIs
- Requires valid JWT token
- Subject to rate limiting (100 requests per 60 seconds)
- Returns rate limit info in response headers

**Curl**:
```bash
curl -X GET http://localhost:8080/user/profile \
  -H "Authorization: Bearer <jwt-token>"
```

**Response Headers**:
```
X-RateLimit-Remaining: 95
X-RateLimit-Capacity: 100
```

---

## Bug Triage Routes

**Endpoint**: `GET/POST/PUT/DELETE /bug/**`

**What it does**:
- Accesses bug triage and management APIs
- Requires valid JWT token
- Subject to rate limiting (100 requests per 60 seconds)
- Returns rate limit info in response headers

**Curl**:
```bash
curl -X GET http://localhost:8080/bug/list \
  -H "Authorization: Bearer <jwt-token>"
```

---

## Error Responses

**401 Unauthorized** (Invalid Token):
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid JWT signature"
}
```

**429 Too Many Requests** (Rate Limit Exceeded):
```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Please try again later."
}
```

**500 Internal Server Error**:
```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

