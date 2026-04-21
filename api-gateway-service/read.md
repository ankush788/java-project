# API Gateway Curl Reference

eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlckVtYWlsIjoidXNlckBleGFtcGxlLmNvbSIsImlhdCI6MTc3NjgwMzcxOCwiZXhwIjoxNzc2ODA0NjE4fQ.8Va7r802U33rrMXrm8Bfg2eym9GwMNRi58HobW2njDg
## Base URL

```bash
BASE_URL=http://localhost:8080
```

## Public Gateway Endpoints

### Health Check
```bash
curl -X GET "http://localhost:8080/gateway/health"
```

### Validate JWT Token
```bash
curl -X POST "http://localhost:8080/gateway/validate-token" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

## Auth Service Endpoints (No JWT Required)

### Register
```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password@123"
  }'
```

### Login
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password@123"
  }'
```

## Protected Endpoints (JWT Required)

```bash
TOKEN="<JWT_TOKEN>"
```

## User Management Service (via `/user/**` route)

### Get All Users
```bash
curl -X GET "http://localhost:8080/api/users?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

### Get User By ID
```bash
curl -X GET "http://localhost:8080/api/users/1" \
  -H "Authorization: Bearer $TOKEN"
```

### Get User By Email
```bash
curl -X GET "http://localhost:8080/api/users/email?email=user@example.com" \
  -H "Authorization: Bearer $TOKEN"
```

### Update User
```bash
curl -X PUT "http://localhost:8080/api/users/1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newemail@example.com"
  }'
```

### Delete User
```bash
curl -X DELETE "http://localhost:8080/user/api/users/1" \
  -H "Authorization: Bearer $TOKEN"
```

## Bug Triage Service (via `/bug/**` route)

### Create Bug
```bash
curl -X POST "http://localhost:8080/api/bugs" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Login fails on Safari",
    "description": "Users cannot log in on Safari 17 when MFA is enabled.",
    "severity": "HIGH"
  }'
```

### Get All Bugs
```bash
curl -X GET "http://localhost:8080/api/bugs?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

### Get Bug By ID
```bash
curl -X GET "http://localhost:8080/api/bugs/1" \
  -H "Authorization: Bearer $TOKEN"
```

### Update Bug
```bash
curl -X PUT "http://localhost:8080/api/bugs/1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Login fails on Safari 17",
    "description": "Issue reproduced on macOS. Working on fix.",
    "status": "IN_PROGRESS",
    "severity": "CRITICAL"
  }'
```

### Delete Bug
```bash
curl -X DELETE "http://localhost:8080/api/bugs/1" \
  -H "Authorization: Bearer $TOKEN"
```

## Notes

- Allowed `severity` values: `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`
- Allowed `status` values: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`
- User and bug routes are rate limited at the gateway.
