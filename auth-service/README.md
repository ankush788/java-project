# API Curl Commands

## Register User (Create)
```bash
curl -X POST http://localhost:8081/auth/register \
-H "Content-Type: application/json" \
-d '{"email":"user@example.com","password":"password123"}'
```

## Login User (Authenticate)
```bash
curl -X POST http://localhost:8081/auth/login \
-H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

Note: This auth-service currently only supports user registration and login. Full CRUD operations for users are not implemented in the API.