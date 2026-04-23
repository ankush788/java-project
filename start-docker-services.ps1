# Script to clean up Docker resources and start services

$rootDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Write-Host "Docker Cleanup and Startup Script" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Green
Write-Host ""

# Step 1: Stop and remove existing containers
Write-Host "Step 1: Stopping and removing existing containers..." -ForegroundColor Yellow
docker compose down --remove-orphans
Start-Sleep -Seconds 2

# Step 2: Remove existing volumes
Write-Host ""
Write-Host "Step 2: Removing existing volumes..." -ForegroundColor Yellow
docker volume prune -f
Start-Sleep -Seconds 2

# Step 3: Remove existing images
Write-Host ""
Write-Host "Step 3: Removing existing images..." -ForegroundColor Yellow
$images = docker images --format "{{.Repository}}:{{.Tag}}" | Where-Object { $_ -match "java-project|auth-service|user-management|bug-triage|api-gateway|eureka" }
if ($images) {
    foreach ($image in $images) {
        Write-Host "  Removing image: $image" -ForegroundColor Cyan
        docker rmi -f $image
    }
}
Start-Sleep -Seconds 2

# Step 4: Build JAR files
Write-Host ""
Write-Host "Step 4: Building JAR files..." -ForegroundColor Yellow
Write-Host ""

$services = @("auth-service", "user-management-service", "bug-triage-service", "api-gateway-service", "eureka-server")

foreach ($service in $services) {
    $servicePath = Join-Path $rootDir $service
    if (Test-Path $servicePath) {
        Write-Host "  Building $service..." -ForegroundColor Cyan
        Push-Location $servicePath
        mvn clean package -DskipTests
        Pop-Location
    }
}

Start-Sleep -Seconds 2

# Step 5: Build Docker images
Write-Host ""
Write-Host "Step 5: Building Docker images with docker compose..." -ForegroundColor Yellow
Push-Location $rootDir
docker compose build --no-cache
Pop-Location

Start-Sleep -Seconds 2

# Step 6: Start services
Write-Host ""
Write-Host "Step 6: Starting services with docker compose..." -ForegroundColor Yellow
Push-Location $rootDir
docker compose up -d
Pop-Location

Write-Host ""
Write-Host "=================================" -ForegroundColor Green
Write-Host "Docker services are now running!" -ForegroundColor Green
Write-Host ""
