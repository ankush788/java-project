# API Curl Commands

## Running the Service with Docker
 Build the JAR files for the services:
   ```
   cd auth-service
   mvn clean package -DskipTests
   cd ../user-management-service
   mvn clean package -DskipTests
   cd ..

   ```
 Build the Docker images:
   ```
   docker compose build --no-cache
   docker compose up -d
   ```


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