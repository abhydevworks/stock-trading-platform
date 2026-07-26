# Stock Trading Platform

A comprehensive microservices-based stock trading platform built with Spring Boot, designed for high-performance trading operations, real-time market data, and robust order execution.

## Features

- 🔐 **User Authentication & Management** - Secure user registration, login, and KYC verification
- 📈 **Real-time Trading** - Market orders, limit orders, stop-loss orders with sub-100ms execution
- 💼 **Portfolio Management** - Track holdings, performance metrics, and trade history
- 📊 **Market Data** - Real-time stock quotes, historical data, technical indicators
- 🔔 **Notifications** - Price alerts, order confirmations, trade notifications
- 💳 **Payment Processing** - Deposits, withdrawals, and settlements
- 📉 **Analytics** - Portfolio performance, tax reporting, investment insights
- 🚀 **Scalable Architecture** - Microservices with Spring Cloud, Docker support

## Architecture

```
stock-trading-platform/
├── common/                    # Shared utilities, DTOs, constants
├── api-gateway/               # API Gateway - request routing, auth, rate limiting
├── user-service/              # User management, authentication, KYC
├── trading-service/           # Order placement, execution, settlements
├── portfolio-service/         # Holdings, performance tracking
├── market-data-service/       # Stock prices, indices, real-time feeds
├── notification-service/      # Alerts, emails, SMS
├── payment-service/           # Deposits, withdrawals, settlements
├── analytics-service/         # Reports, performance metrics
└── docker-compose.yml         # Local development environment
```

## Tech Stack

- **Backend**: Spring Boot 3.2.4, Spring Cloud
- **Database**: PostgreSQL (relational), Redis (caching), TimescaleDB (time-series)
- **Message Queue**: Apache Kafka (event streaming)
- **Security**: Spring Security, JWT
- **API Documentation**: Swagger/OpenAPI
- **Containerization**: Docker, Docker Compose
- **Monitoring**: Spring Actuator, Micrometer

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Git

### Setup Local Environment

```bash
# Clone repository
git clone https://github.com/abhydevworks/stock-trading-platform.git
cd stock-trading-platform

# Start infrastructure (PostgreSQL, Redis, Kafka)
docker-compose up -d

# Build all services
mvn clean install

# Run all services
mvn spring-boot:run -pl api-gateway
# In separate terminals, run other services as needed
```

### API Gateway
Access at: `http://localhost:8080`

### Services
- **API Gateway**: 8080
- **User Service**: 8081
- **Trading Service**: 8082
- **Portfolio Service**: 8083
- **Market Data Service**: 8084
- **Notification Service**: 8085
- **Payment Service**: 8086
- **Analytics Service**: 8087

## Database Setup

```sql
-- See database/schema.sql for complete schema
-- Migrations handled by Flyway
```

## API Documentation

Swagger UI available at: `http://localhost:8080/swagger-ui.html`

## Project Structure

Each microservice follows this structure:

```
service-name/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/stocktrading/
│   │   │       ├── controller/      # REST controllers
│   │   │       ├── service/         # Business logic
│   │   │       ├── repository/      # Data access
│   │   │       ├── model/           # Entities
│   │   │       ├── dto/             # Data transfer objects
│   │   │       ├── exception/       # Custom exceptions
│   │   │       └── config/          # Configuration classes
│   │   └── resources/
│   │       ├── application.yml      # Service configuration
│   │       └── db/migration/        # Database migrations
│   └── test/
├── Dockerfile
└── pom.xml
```

## Development Workflow

1. Create feature branch from `develop`: `git checkout -b feature/your-feature develop`
2. Make changes and commit: `git commit -am "feat: description"`
3. Push to branch: `git push origin feature/your-feature`
4. Create Pull Request to `develop`
5. After review and merge to `develop`, PR to `main` for release

## Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn clean test jacoco:report
```

## Deployment

### Docker Build

```bash
mvn clean package -DskipTests
docker-compose -f docker-compose.prod.yml up -d
```

## Configuration

Each service can be configured via `application.yml` or environment variables.

### Common Configuration

```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/stock_trading
    username: admin
    password: password
  jpa:
    hibernate:
      ddl-auto: validate
  redis:
    host: redis
    port: 6379
  kafka:
    bootstrap-servers: kafka:9092
```

## Security

- JWT-based authentication
- Role-based access control (RBAC)
- API rate limiting
- SQL injection prevention via parameterized queries
- Encryption of sensitive data
- Audit logging for all transactions

## Performance

- Sub-100ms order execution latency
- Caching via Redis for frequently accessed data
- Connection pooling for database efficiency
- Asynchronous processing via Kafka
- Load balancing via API Gateway

## Monitoring & Logging

- Spring Actuator metrics: `/actuator/metrics`
- ELK stack integration ready
- Structured logging for all services
- Distributed tracing support

## Contributing

1. Follow the development workflow above
2. Write tests for new features
3. Ensure code passes linting and tests
4. Follow Spring Boot best practices
5. Update documentation as needed

## License

MIT License - see LICENSE file for details

## Support

For issues and questions, please create an issue on GitHub.
