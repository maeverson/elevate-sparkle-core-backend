# Sparkle Core Backend

Enterprise-grade backend system built with **Hexagonal Architecture** (Ports & Adapters), designed for scalability, resilience, and observability.

## 🏗️ Architecture

This project follows **Hexagonal Architecture** principles with strict separation of concerns:

```
┌─────────────────────────────────────────────────────┐
│                   Bootstrap Layer                    │
│            (Spring Boot Configuration)               │
└─────────────────────────────────────────────────────┘
                          ▲
                          │
        ┌─────────────────┴─────────────────┐
        │                                   │
┌───────▼────────┐              ┌──────────▼─────────┐
│  Adapter-In    │              │   Adapter-Out      │
│  (REST API)    │              │  (Persistence +    │
│                │              │   Messaging)       │
└───────┬────────┘              └──────────┬─────────┘
        │                                   │
        └─────────────────┬─────────────────┘
                          ▼
             ┌────────────────────────┐
             │  Core Application      │
             │  (Use Cases + Ports)   │
             └────────────┬───────────┘
                          ▼
                ┌─────────────────┐
                │  Core Domain    │
                │ (Pure Business  │
                │     Logic)      │
                └─────────────────┘
```

### Modules

1. **core-domain**: Pure domain logic (no framework dependencies)
2. **core-application**: Use cases and port interfaces
3. **adapter-in-web**: REST API controllers
4. **adapter-out-persistence**: PostgreSQL persistence (JPA)
5. **adapter-out-messaging**: AWS SQS event publishing
6. **infrastructure**: Security, observability, cross-cutting concerns
7. **bootstrap**: Spring Boot application entry point

## 🚀 Technology Stack

- **Java 17** + **Spring Boot 3.2.5**
- **Maven** (multi-module project)
- **PostgreSQL 15** (database)
- **AWS SQS** (asynchronous messaging)
- **Spring Security** + **JWT** (authentication)
- **OpenTelemetry** + **Prometheus** + **Grafana** (observability)
- **Resilience4j** (circuit breaker, retry)
- **Flyway** (database migrations)
- **Testcontainers** (integration testing)

## 📋 Prerequisites

- JDK 17+
- Maven 3.8+
- Docker & Docker Compose
- AWS CLI (for production deployment)

## 🛠️ Getting Started

### 1. Clone the repository
```bash
git clone <repository-url>
cd elevate-sparkle-core-backend
```

### 2. Build the project
```bash
mvn clean install
```

### 3. Run with Docker Compose
```bash
docker-compose up -d
```

This will start:
- PostgreSQL database (port 5432)
- LocalStack (SQS emulator, port 4566)
- OpenTelemetry Collector (port 4317)
- Prometheus (port 9090)
- Grafana (port 3000)
- Application (port 8080)

### 4. Access the application

- **API**: http://localhost:8080
- **API Docs**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/prometheus
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090

## 🧪 Testing

### Run all tests
```bash
mvn verify
```

### Run only unit tests
```bash
mvn test
```

### Run only integration tests
```bash
mvn verify -P integration-tests
```

## 📊 Observability

### Metrics
- Exposed via `/actuator/prometheus`
- Collected by Prometheus
- Visualized in Grafana

### Tracing
- OpenTelemetry instrumentation
- OTLP export to collector
- Correlation ID propagation via MDC

### Logging
- Structured JSON logging (Logstash format)
- Includes: correlationId, traceId, spanId
- Centralized via stdout (for container orchestration)

## 🔐 Security

### Authentication
- JWT-based stateless authentication
- POST `/api/v1/auth/register` - Register new user
- POST `/api/v1/auth/login` - Get JWT token

### Authorization
- Role-based access control (USER, ADMIN)
- Spring Security configuration
- Password encryption with BCrypt

### Example Login Request
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user@example.com",
    "password": "password123"
  }'
```

## 📦 API Endpoints

### Orders
- `POST /api/v1/orders` - Create new order
- `GET /api/v1/orders/{id}` - Get order by ID
- `GET /api/v1/orders` - List all orders
- `PATCH /api/v1/orders/{id}/status` - Update order status

### Users
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Authenticate user

All endpoints (except auth) require `Authorization: Bearer <JWT>` header.

## 🗄️ Database Migrations

### Flyway Migrations

O projeto usa **Flyway** para versionamento do banco de dados. As migrations estão localizadas em:
```
adapter-out-persistence/src/main/resources/db/migration/
```

### Migrations Disponíveis

| Versão | Arquivo | Descrição |
|--------|---------|-----------|
| V1 | `V1__create_users_table.sql` | Tabelas de usuários e roles |
| V2 | `V2__create_orders_table.sql` | Tabelas de pedidos e itens |
| V3 | `V3__create_event_store.sql` | Event Store para Event Sourcing |
| V4 | `V4__create_journey_tables.sql` | Jornadas e versionamento |
| V5 | `V5__create_execution_projections.sql` | Projeções CQRS para dashboard |
| V6 | `V6__seed_system_user.sql` | Usuário sistema inicial |
| V7 | `V7__seed_test_data.sql` | Dados de teste (dev only) |

### Aplicar Migrations

```bash
# Via Maven
mvn flyway:migrate

# Via Spring Boot (automático no startup)
# Configurado em application.yml:
#   spring.flyway.enabled: true
```

### Comandos Úteis

```bash
# Verificar status das migrations
mvn flyway:info

# Validar migrations
mvn flyway:validate

# Limpar banco (⚠️ CUIDADO - apenas dev!)
mvn flyway:clean

# Validar scripts SQL
./scripts/validate-migrations.sh
```

### Usuário Padrão (Development)

**Username:** `system`  
**Password:** `admin123`  
**Email:** `system@sparkle.local`  
**Roles:** ADMIN, USER

**⚠️ IMPORTANTE:** Alterar senha em produção!

### Schema do Banco de Dados

```sql
-- Autenticação
users, user_roles

-- Domínio de Negócio
orders, order_items

-- Journey Orchestration
journey_definitions, journey_versions, execution_summary

-- Event Sourcing
event_store, event_snapshot
```

Para mais detalhes, consulte: `adapter-out-persistence/src/main/resources/db/migration/README.md`

## 🌍 Environment Configuration

### Development (application-dev.yml)
- Uses LocalStack for SQS
- H2 console enabled (if needed)
- Verbose logging

### Production (application-prod.yml)
- Real AWS SQS
- Production database
- WARN-level logging
- Enhanced security

### Environment Variables
```bash
SPRING_PROFILES_ACTIVE=dev                # or prod
SPRING_DATASOURCE_URL=jdbc:postgresql://...
SPRING_DATASOURCE_USERNAME=sparkle
SPRING_DATASOURCE_PASSWORD=secret
CLOUD_AWS_SQS_ENDPOINT=http://localhost:4566
JWT_SECRET=your-256-bit-secret-key
```

## 🐳 Docker

### Build image
```bash
docker build -t sparkle-backend:latest .
```

### Run standalone
```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e JWT_SECRET=my-secret-key \
  sparkle-backend:latest
```

## 🚢 CI/CD

GitHub Actions workflow (`.github/workflows/ci-cd.yml`) includes:

1. **Build & Test** - Maven build + unit/integration tests
2. **Code Quality** - Checkstyle, SpotBugs
3. **Security Scan** - Dependency vulnerability check
4. **Package** - JAR artifact creation
5. **Docker Build** - Multi-stage Docker image
6. **Deploy Staging** - Automatic deployment to staging
7. **Deploy Production** - Manual approval required

### Required Secrets
- `DOCKER_USERNAME`
- `DOCKER_PASSWORD`
- `SLACK_WEBHOOK` (optional)

## 📁 Project Structure

```
elevate-sparkle-core-backend/
├── core-domain/                    # Pure domain logic
│   └── src/main/java/.../domain/
│       ├── model/                  # Entities (Order, User)
│       ├── valueobject/           # Value objects (Money, Email, etc.)
│       └── exception/             # Domain exceptions
│
├── core-application/               # Use cases
│   └── src/main/java/.../application/
│       ├── port/in/               # Input ports (use case interfaces)
│       ├── port/out/              # Output ports (repository interfaces)
│       └── usecase/               # Use case implementations
│
├── adapter-in-web/                # REST controllers
│   └── src/main/java/.../adapter/web/
│       ├── controller/            # REST endpoints
│       ├── dto/                   # Request/Response DTOs
│       └── mapper/                # DTO↔Domain mappers
│
├── adapter-out-persistence/       # Database adapter
│   └── src/main/java/.../adapter/persistence/
│       ├── entity/                # JPA entities
│       ├── repository/            # Spring Data repositories
│       ├── mapper/                # JPA↔Domain mappers
│       └── adapter/               # Port implementations
│
├── adapter-out-messaging/         # Messaging adapter
│   └── src/main/java/.../adapter/messaging/
│       ├── event/                 # Event DTOs
│       ├── mapper/                # Event mappers
│       └── adapter/               # SQS publisher
│
├── infrastructure/                # Cross-cutting concerns
│   └── src/main/java/.../infrastructure/
│       ├── security/              # JWT, security config
│       ├── observability/         # OpenTelemetry, MDC
│       └── config/                # OpenAPI, beans
│
├── bootstrap/                     # Application entry point
│   └── src/main/java/.../bootstrap/
│       ├── SparkleBackendApplication.java
│       └── config/                # Bean wiring
│
├── docker-compose.yml
├── Dockerfile
└── .github/workflows/ci-cd.yml
```

## 🔧 Configuration Files

- **pom.xml** - Maven parent POM with dependency management
- **application.yml** - Main configuration (database, SQS, JWT, OTEL)
- **application-dev.yml** - Development profile
- **application-prod.yml** - Production profile
- **logback-spring.xml** - Structured logging configuration

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License.

## 📞 Contact

For questions or support, contact the development team.

---

**Built with ❤️ using Hexagonal Architecture**
