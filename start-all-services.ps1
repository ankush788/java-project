# Script to start all microservices in VS Code terminal using background jobs

$services = @(
    "eureka-server",
    "api-gateway-service",
    "auth-service",
    "user-management-service",
    "bug-triage-service"
)

$rootDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Write-Host "Starting all microservices from: $rootDir" -ForegroundColor Green
Write-Host ""

$jobs = @()

foreach ($service in $services) {
    $servicePath = Join-Path $rootDir $service
    
    if (Test-Path $servicePath) {
        Write-Host "Starting $service..." -ForegroundColor Yellow
        
        # Start each service as a background job
        $job = Start-Job -ScriptBlock {
            param($path, $name)
            Set-Location $path
            Write-Host "[$name] Service starting..." -ForegroundColor Cyan
            mvn spring-boot:run
        } -ArgumentList $servicePath, $service -Name $service
        
        $jobs += $job
        Start-Sleep -Seconds 1
    }
    else {
        Write-Host "Warning: $service directory not found at $servicePath" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "All services started as background jobs!" -ForegroundColor Green
Write-Host ""
Write-Host "Job Information:" -ForegroundColor Cyan
Get-Job | Select-Object Name, State, HasMoreData
Write-Host ""
Write-Host "Commands to manage jobs:" -ForegroundColor Yellow
Write-Host "  Get-Job              - Show all running jobs"
Write-Host "  Receive-Job -Name <service-name> -Keep  - View job output"
Write-Host "  Stop-Job -Name <service-name>          - Stop a specific service"
Write-Host "  Stop-Job -State Running                 - Stop all services"
Write-Host "  Remove-Job -State Completed             - Clean up completed jobs"
Write-Host ""
