@echo off
REM Script to start all microservices using Command Prompt

setlocal enabledelayedexpansion

set "rootDir=%~dp0"
echo Starting all microservices from: %rootDir%

set services=eureka-server api-gateway-service auth-service user-management-service bug-triage-service

for %%S in (%services%) do (
    set "servicePath=%rootDir%%%S"
    
    if exist "!servicePath!" (
        echo Starting %%S...
        start cmd /k "cd /d "!servicePath!" && echo Starting %%S... && mvn spring-boot:run"
        timeout /t 2 /nobreak
    ) else (
        echo Warning: %%S directory not found at !servicePath!
    )
)

echo.
echo All services started! Check individual Command Prompt windows for logs.
