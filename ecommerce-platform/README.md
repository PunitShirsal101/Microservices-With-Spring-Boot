# Enterprise E-Commerce Microservices Platform

# In-Progress

A comprehensive e-commerce platform built with Spring Boot microservices, featuring service discovery and event-driven communication.

## Architecture Overview

```
┌─────────────┐    ┌─────────────┐
│   FRONTEND  │    │  API GATEWAY│
└─────────────┘    └─────────────┘
         │               │
         ▼               ▼
┌─────────────┐    ┌─────────────┐
│ USER SERVICE│    │PRODUCT SVC  │
└─────────────┘    └─────────────┘
         │               │
         ▼               ▼
┌─────────────┐    ┌─────────────┐
│ ORDER SVC   │    │ PAYMENT SVC │
└─────────────┘    └─────────────┘
         │               │
         ▼               ▼
┌─────────────┐    ┌─────────────┐
│   DATABASE  │    │    CACHE    │
│ (PostgreSQL)│    │   (Redis)   │
└─────────────┘    └─────────────┘
```

## Features

- **User Management**: Registration, authentication, profiles
- **Product Catalog**: Browse, search, inventory
- **Shopping Cart**: Add/remove items, persistence
- **Order Management**: Creation, tracking, history
- **Payment Processing**: Multiple methods, transactions
- **Microservices**: Independent, scalable services
- **Service Discovery**: Eureka server
- **API Gateway**: Routing, load balancing
- **Event-Driven**: Kafka messaging
- **Monitoring**: Health checks, metrics

## Technology Stack

| Component         | Technology            |
| ----------------- | --------------------- |
| Backend           | Spring Boot 3.3.5     |
| Microservices     | Spring Cloud 2023.0.3 |
| Database          | PostgreSQL 16         |
| Cache             | Redis 7               |
| Message Broker    | Apache Kafka 3.8.1    |
| Service Discovery | Netflix Eureka        |
| API Gateway       | Spring Cloud Gateway  |
| Security          | Spring Security 6.3.4 |
| Documentation     | SpringDoc OpenAPI 3   |
| Build Tool        | Maven 3.9+            |
| Java              | OpenJDK 17            |

## Services Overview

| Service         | Port | Description       |
| --------------- | ---- | ----------------- |
| Eureka Server   | 8761 | Service discovery |
| API Gateway     | 8080 | Main entry point  |
| User Service    | 8081 | User management   |
| Product Service | 8082 | Product catalog   |
| Cart Service    | 8083 | Shopping cart     |
| Order Service   | 8084 | Order processing  |
| Payment Service | 8085 | Payment handling  |

### Infrastructure

| Service    | Port | Description        |
| ---------- | ---- | ------------------ |
| PostgreSQL | 5432 | Primary database   |
| Redis      | 6379 | Caching            |
| Kafka      | 9092 | Messaging          |
| Zookeeper  | 2181 | Kafka coordination |

## Development Setup

### Prerequisites

- Java 17 (OpenJDK)
- Maven 3.9+
- PostgreSQL 16
- Redis 7
- Apache Kafka 3.8.1
- ZooKeeper

### Local Development

1. **Start Infrastructure Services**

   Start PostgreSQL, Redis, Kafka, and ZooKeeper locally or using your preferred method.

2. **Run Services Locally**

   Each service can be run independently:

   ```bash
   # Terminal 1: Eureka Server
   cd eureka-server && mvn spring-boot:run

   # Terminal 2: API Gateway
   cd api-gateway && mvn spring-boot:run

   # Terminal 3: User Service
   cd user-service && mvn spring-boot:run

   # Terminal 4: Product Service
   cd product-service && mvn spring-boot:run

   # Terminal 5: Cart Service
   cd cart-service && mvn spring-boot:run

   # Terminal 6: Order Service
   cd order-service && mvn spring-boot:run

   # Terminal 7: Payment Service
   cd payment-service && mvn spring-boot:run
   ```

### Development URLs

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080

## Monitoring and Management

### Health Endpoints

All services expose Spring Boot Actuator endpoints:

- `/actuator/health` - Health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

### Database Management

- **pgAdmin**: http://localhost:5050
  - Email: admin@ecommerce.com
  - Password: admin
  - PostgreSQL Host: localhost
  - Port: 5432
  - Username: ecommerce_user
  - Password: ecommerce_password

### Cache Management

- **Redis Commander**: http://localhost:8081

## Security Configuration

### Database Security

- Separate database schemas per service
- Dedicated database user with limited privileges
- Password-protected access

### Service Security

- JWT-based authentication ready
- CORS configuration for web clients
- Secure inter-service communication

## API Documentation

Each service provides OpenAPI 3 documentation:

- **User Service**: http://localhost:8081/swagger-ui.html
- **Product Service**: http://localhost:8082/swagger-ui.html
- **Cart Service**: http://localhost:8083/swagger-ui.html
- **Order Service**: http://localhost:8084/swagger-ui.html
- **Payment Service**: http://localhost:8085/swagger-ui.html

## Production Deployment

### Environment Variables

Key environment variables for production:

```env
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/ecommerce
SPRING_DATASOURCE_USERNAME=your-db-user
SPRING_DATASOURCE_PASSWORD=your-db-password

# Redis
SPRING_REDIS_HOST=your-redis-host
SPRING_REDIS_PORT=6379

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=your-kafka-host:9092

# Eureka
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://your-eureka-host:8761/eureka/

# Security
JWT_SECRET=your-jwt-secret-key
```

## Testing

### Run Tests

```bash
# Run all tests
./mvnw test

# Run tests for specific service
./mvnw test -pl user-service
```

### Integration Testing

```bash
# Run integration tests (ensure infrastructure is running)
./mvnw verify
```

## Performance Tuning

### JVM Optimization

Services are configured with optimized JVM settings for production use.

### Database Optimization

- Connection pooling configured
- Database indexes on frequently queried fields
- Separate schemas for isolation

## Troubleshooting

### Common Issues

#### 1. Services Not Starting

```bash
# Check service logs
# Run the service and check console output

# Check service health
curl http://localhost:[port]/actuator/health
```

#### 2. Database Connection Issues

```bash
# Check PostgreSQL status (if using local PostgreSQL)
pg_isready -h localhost -p 5432

# Check database connectivity
psql -h localhost -p 5432 -U ecommerce_user -d ecommerce -c "\l"
```

#### 3. Memory Issues

Monitor JVM memory usage through actuator endpoints or application logs.

### Service Dependencies

Services should be started in the following order:

1. Infrastructure (PostgreSQL, Redis, Kafka)
2. Eureka Server
3. API Gateway
4. Microservices

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the Apache License.

## Support

For support and questions:

- Create an issue in the repository
- Check the troubleshooting section
- Review service logs using `./deploy.sh logs [service-name]`



---
