### Read All Users
```bash
curl -X GET "http://localhost:8082/api/users?page=0&size=10"
```

### Read User by ID
```bash
curl -X GET "http://localhost:8082/api/users/1"
```

### Read User by email
```bash
curl -X GET "http://localhost:8082/api/users/email?email=user@example.com" \-H "Content-Type: application/json"
```

### Update User
```bash
curl -X PUT "http://localhost:8082/api/users/1" \
-H "Content-Type: application/json" \
  -d '{
    "email" : "ankushsingh@gmail.com"
  }'
```

### Delete User
```bash
curl -X DELETE "http://localhost:8082/api/users/1"
```