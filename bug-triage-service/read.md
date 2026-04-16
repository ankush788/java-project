# Bug Triage System

## 1) CRUD operations with `curl`

Base URL: `http://localhost:8080/api/v1/incidents`

### Create incident
```bash
curl -X POST http://localhost:8080/api/v1/incidents \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Example bug report",
    "description": "The application crashes when submitting the form.",
    "severity": "HIGH"
  }'
```

### Read all incidents (paged)
```bash
curl "http://localhost:8080/api/v1/incidents?page=0&size=10"
```

### Read incident by id
```bash
curl http://localhost:8080/api/v1/incidents/1
```

### Update incident
```bash
curl -X PUT http://localhost:8080/api/v1/incidents/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated bug title",
    "description": "The updated description with more details.",
    "status": "IN_PROGRESS",
    "severity": "CRITICAL"
  }'
```

### Delete incident
```bash
curl -X DELETE http://localhost:8080/api/v1/incidents/1
```

### Valid enum values
- `IncidentSeverity`: `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`
- `IncidentStatus`: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`

---

## 2) Project structure

```
Bug-triage system/
├── docker-compose.yml
├── pom.xml
├── read.md
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── bugtriage
│   │   │           ├── BugTriageApplication.java
│   │   │           ├── controller
│   │   │           │   └── IncidentController.java
│   │   │           ├── dto
│   │   │           │   ├── CreateIncidentRequest.java
│   │   │           │   ├── ErrorResponse.java
│   │   │           │   ├── IncidentResponse.java
│   │   │           │   ├── PageResponse.java
│   │   │           │   └── UpdateIncidentRequest.java
│   │   │           ├── entity
│   │   │           │   ├── Incident.java
│   │   │           │   ├── IncidentSeverity.java
│   │   │           │   ├── IncidentStatus.java
│   │   │           │   └── UpdateIncidentRequest.java
│   │   │           ├── exception
│   │   │           │   ├── GlobalExceptionHandler.java
│   │   │           │   └── ResourceNotFoundException.java
│   │   │           ├── repository
│   │   │           │   └── IncidentRepository.java
│   │   │           └── service
│   │   │               └── IncidentService.java
│   │   └── resources
│   │       └── application.properties
│   └── test
│       └── java
│           └── com
│               └── bugtriage
├── target
│   ├── classes
│   ├── generated-sources
│   └── test-classes
```
