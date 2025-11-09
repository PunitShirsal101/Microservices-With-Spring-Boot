#!/bin/bash

# Config Server Integration Test Script
# This script tests the Spring Cloud Config Server integration

echo "🔍 Testing Config Server Integration..."
echo "========================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check if a service is running
check_service() {
    local service_name=$1
    local port=$2
    local url="http://localhost:$port/actuator/health"

    echo -n "Checking $service_name on port $port... "

    if curl -s -f "$url" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ RUNNING${NC}"
        return 0
    else
        echo -e "${RED}❌ NOT RUNNING${NC}"
        return 1
    fi
}

# Function to test Config Server endpoint
test_config_server() {
    local service_name=$1
    local profile=${2:-default}

    echo -n "Testing Config Server for $service_name ($profile)... "

    # Test if Config Server returns configuration
    if curl -s -f "http://localhost:8888/$service_name/$profile" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ CONFIG AVAILABLE${NC}"
        return 0
    else
        echo -e "${RED}❌ CONFIG NOT AVAILABLE${NC}"
        return 1
    fi
}

# Function to test Eureka registration
test_eureka_registration() {
    local service_name=$1

    echo -n "Testing Eureka registration for $service_name... "

    # Check if service is registered in Eureka
    if curl -s "http://localhost:8761/eureka/apps/$service_name" | grep -q "instance"; then
        echo -e "${GREEN}✅ REGISTERED${NC}"
        return 0
    else
        echo -e "${RED}❌ NOT REGISTERED${NC}"
        return 1
    fi
}

echo ""
echo "1. Checking Infrastructure Services:"
echo "------------------------------------"

# Check Config Server
check_service "Config Server" 8888

# Check Eureka Server
check_service "Eureka Server" 8761

echo ""
echo "2. Testing Config Server Endpoints:"
echo "-----------------------------------"

# Test Config Server endpoints for all services
test_config_server "user-service" "dev"
test_config_server "api-gateway" "dev"
test_config_server "product-service" "dev"
test_config_server "cart-service" "dev"
test_config_server "order-service" "dev"
test_config_server "payment-service" "dev"
test_config_server "eureka-server" "dev"

echo ""
echo "3. Testing Eureka Service Discovery:"
echo "------------------------------------"

# Test Eureka registration (this would require services to be running)
echo -e "${YELLOW}⚠️  Note: Eureka registration tests require services to be started${NC}"

echo ""
echo "4. Manual Testing Instructions:"
echo "------------------------------"
echo "To fully test the integration:"
echo "1. Start Config Server: ./mvnw spring-boot:run -pl config-server"
echo "2. Start Eureka Server: ./mvnw spring-boot:run -pl eureka-server"
echo "3. Start any service: ./mvnw spring-boot:run -pl user-service -Dspring.profiles.active=dev"
echo "4. Check service logs for 'Located property source' messages"
echo "5. Verify service health: http://localhost:{port}/actuator/health"
echo ""
echo "Expected log messages:"
echo "- 'Located property source: Config Server:http://localhost:8888/'"
echo "- 'Fetching config from server at: http://localhost:8888'"
echo ""
echo "🎉 Config Server integration setup complete!"
echo "Repository: https://github.com/PunitShirsal101/Config-server.git"