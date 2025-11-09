# Config Server Integration Test Script
# This script tests the Spring Cloud Config Server integration

Write-Host "🔍 Testing Config Server Integration..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Function to check if a service is running
function Test-Service {
    param (
        [string]$ServiceName,
        [int]$Port
    )

    $url = "http://localhost:$Port/actuator/health"
    Write-Host -NoNewline "Checking $ServiceName on port $Port... "

    try {
        $response = Invoke-WebRequest -Uri $url -TimeoutSec 5 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ RUNNING" -ForegroundColor Green
            return $true
        }
    }
    catch {
        Write-Host "❌ NOT RUNNING" -ForegroundColor Red
        return $false
    }
}

# Function to test Config Server endpoint
function Test-ConfigServer {
    param (
        [string]$ServiceName,
        [string]$Profile = "default"
    )

    Write-Host -NoNewline "Testing Config Server for $ServiceName ($Profile)... "

    try {
        $url = "http://localhost:8888/$ServiceName/$Profile"
        $response = Invoke-WebRequest -Uri $url -TimeoutSec 5 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ CONFIG AVAILABLE" -ForegroundColor Green
            return $true
        }
    }
    catch {
        Write-Host "❌ CONFIG NOT AVAILABLE" -ForegroundColor Red
        return $false
    }
}

Write-Host ""
Write-Host "1. Checking Infrastructure Services:" -ForegroundColor Yellow
Write-Host "------------------------------------" -ForegroundColor Yellow

# Check Config Server
Test-Service -ServiceName "Config Server" -Port 8888

# Check Eureka Server
Test-Service -ServiceName "Eureka Server" -Port 8761

Write-Host ""
Write-Host "2. Testing Config Server Endpoints:" -ForegroundColor Yellow
Write-Host "-----------------------------------" -ForegroundColor Yellow

# Test Config Server endpoints for all services
Test-ConfigServer -ServiceName "user-service" -Profile "dev"
Test-ConfigServer -ServiceName "api-gateway" -Profile "dev"
Test-ConfigServer -ServiceName "product-service" -Profile "dev"
Test-ConfigServer -ServiceName "cart-service" -Profile "dev"
Test-ConfigServer -ServiceName "order-service" -Profile "dev"
Test-ConfigServer -ServiceName "payment-service" -Profile "dev"
Test-ConfigServer -ServiceName "eureka-server" -Profile "dev"

Write-Host ""
Write-Host "3. Manual Testing Instructions:" -ForegroundColor Yellow
Write-Host "------------------------------" -ForegroundColor Yellow
Write-Host "To fully test the integration:" -ForegroundColor White
Write-Host "1. Start Config Server: .\mvnw.cmd spring-boot:run -pl config-server" -ForegroundColor White
Write-Host "2. Start Eureka Server: .\mvnw.cmd spring-boot:run -pl eureka-server" -ForegroundColor White
Write-Host "3. Start any service: .\mvnw.cmd spring-boot:run -pl user-service -Dspring.profiles.active=dev" -ForegroundColor White
Write-Host "4. Check service logs for 'Located property source' messages" -ForegroundColor White
Write-Host "5. Verify service health: http://localhost:{port}/actuator/health" -ForegroundColor White
Write-Host ""
Write-Host "Expected log messages:" -ForegroundColor White
Write-Host "- 'Located property source: Config Server:http://localhost:8888/'" -ForegroundColor White
Write-Host "- 'Fetching config from server at: http://localhost:8888'" -ForegroundColor White
Write-Host ""
Write-Host "🎉 Config Server integration setup complete!" -ForegroundColor Green
Write-Host "Repository: https://github.com/PunitShirsal101/Config-server.git" -ForegroundColor Green