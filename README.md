# Pokemon Go Code Sharing Backend

A Java Spring Boot backend service for sharing Pokemon Go friend codes and facilitating connections between players.

## 🚀 Features

- **Friend Code Management**: Submit, update, and search Pokemon Go friend codes
- **User Profiles**: Manage trainer information and preferences
- **Rate Limiting**: Prevent spam with intelligent rate limiting
- **Circuit Breaker**: Resilient API with automatic fallback mechanisms
- **Caching**: Redis-powered caching for optimal performance
- **Database Migrations**: Flyway-managed PostgreSQL schema evolution
- **API Documentation**: Comprehensive OpenAPI/Swagger documentation
- **Health Monitoring**: Spring Boot Actuator endpoints for monitoring

## 🛠 Technology Stack

- **Java 21** - Latest LTS version with modern language features
- **Spring Boot 3.5.0** - Enterprise-ready application framework
- **Spring Data JPA** - Simplified data access layer
- **PostgreSQL 15** - Robust relational database
- **Redis 7** - In-memory data structure store for caching
- **Flyway** - Database migration management
- **Spring Security** - Authentication and authorization
- **Gradle** - Build automation and dependency management
- **Docker** - Containerization for consistent deployments
- **Kubernetes** - Container orchestration and scaling

## 📁 Project Structure

```
.
├── src/
│   ├── main/
│   │   ├── java/com/devs/simplicity/poke_go_friends/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST API controllers
│   │   │   ├── domain/          # Domain entities and DTOs
│   │   │   ├── repository/      # Data access layer
│   │   │   ├── service/         # Business logic
│   │   │   └── util/            # Utility classes
│   │   └── resources/
│   │       ├── application*.properties  # Configuration files
│   │       └── db/migration/    # Database migration scripts
│   └── test/                    # Unit and integration tests
├── deployment/                  # Deployment configurations
│   ├── docker/                  # Docker and Docker Compose files
│   │   ├── Dockerfile
│   │   └── compose/
│   └── kubernetes/              # Kubernetes manifests
│       ├── base/                # Base configurations
│       └── overlays/            # Environment-specific overlays
├── docs/                        # Documentation
├── postman/                     # API testing collections
└── swagger/                     # API documentation exports
```

## 🚦 Getting Started

### Prerequisites

- **Java 21** or higher
- **Docker** and **Docker Compose**
- **PostgreSQL 15** (for local development)
- **Redis 7** (for local development)

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/MikeMikerson/pokemon_go_code_sharing_backend.git
   cd poke-go-friends
   ```

2. **Start dependencies with Docker Compose**
   ```bash
   cd deployment/docker/compose
   docker-compose up -d postgres redis
   ```

3. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

4. **Access the application**
   - API Base URL: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Health Check: http://localhost:8080/actuator/health

### Docker Development

Run the entire stack with Docker Compose:

```bash
cd deployment/docker/compose
docker-compose up -d
```

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Integration Tests
```bash
./gradlew integrationTest
```

### Test Coverage
```bash
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

## 🚀 Deployment

### Development Environment
```bash
kubectl apply -k deployment/kubernetes/overlays/development
```

### Production Environment
```bash
kubectl apply -k deployment/kubernetes/overlays/production
```

For detailed deployment instructions, see [deployment/README.md](deployment/README.md).

## 📊 CI/CD Pipeline

The project uses GitHub Actions for automated testing, security scanning, and deployment following industry best practices:

- **🧪 Continuous Integration**: Automated testing with PostgreSQL and Redis services
- **🔒 Security Scanning**: Vulnerability scanning with Trivy and dependency checks
- **🚀 Auto-Deployment**: Automatic deployment to development environment
- **🏭 Production Deployment**: Manual approval required for production releases
- **⚡ Performance Optimization**: Advanced Gradle and Docker layer caching
- **🧹 Cache Management**: Automatic cleanup of feature branch caches

**Key Features:**
- Multi-architecture Docker builds (AMD64/ARM64)
- GitHub Container Registry integration
- Kustomize-based Kubernetes deployments
- Comprehensive smoke tests and health checks
- Daily security scans and dependency updates

For detailed information, see [docs/CI_CD_PIPELINE.md](docs/CI_CD_PIPELINE.md).

## 🔧 Configuration

### Environment Profiles

- **Development** (`dev`): Local development with debug logging
- **Production** (`prod`): Optimized for production with security settings
- **Test** (`test`): Testing environment with in-memory databases

### Key Configuration Properties

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/poke_go_friends
spring.datasource.username=postgres
spring.datasource.password=${DATABASE_PASSWORD}

# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Rate Limiting
app.rate-limit.submissions-per-hour-per-ip=5
app.rate-limit.submissions-per-day-per-user=10
```

## 📡 API Documentation

### Swagger/OpenAPI
- **Interactive UI**: http://localhost:8080/swagger-ui.html
- **JSON Spec**: http://localhost:8080/api-docs
- **Postman Collection**: [postman/](postman/)

### Key Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/friend-codes` | GET | Search friend codes |
| `/api/friend-codes` | POST | Submit new friend code |
| `/api/friend-codes/{id}` | PUT | Update friend code |
| `/api/friend-codes/{id}` | DELETE | Delete friend code |
| `/api/users` | GET | Get user profiles |
| `/actuator/health` | GET | Health check |

## 🔒 Security

- **Input Validation**: Comprehensive validation on all inputs
- **Rate Limiting**: Per-IP and per-user rate limits
- **SQL Injection Protection**: Parameterized queries with JPA
- **CORS Configuration**: Controlled cross-origin requests
- **Security Headers**: Standard security headers applied
- **Authentication**: Token-based authentication (configurable)

## 📈 Monitoring & Observability

### Health Checks
- Database connectivity
- Redis connectivity
- Custom application health indicators

### Metrics
- Application metrics via Spring Boot Actuator
- Custom business metrics
- Performance monitoring ready

### Logging
- Structured logging with JSON format
- Configurable log levels per environment
- Request/response logging for debugging

## 🤝 Contributing

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Follow** the coding standards (see [.github/copilot-instructions.md](.github/copilot-instructions.md))
4. **Write tests** for your changes
5. **Commit** your changes (`git commit -m 'Add amazing feature'`)
6. **Push** to the branch (`git push origin feature/amazing-feature`)
7. **Open** a Pull Request

### Development Guidelines

- Follow TDD practices (Red-Green-Refactor)
- Write unit tests for all business logic
- Use integration tests for API endpoints
- Follow Spring Boot best practices
- Keep methods small and focused
- Use meaningful variable and method names

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

- **Mike Farelly** - [@MikeMikerson](https://github.com/MikeMikerson)

## 🐛 Issues & Support

- **Bug Reports**: [GitHub Issues](https://github.com/MikeMikerson/pokemon_go_code_sharing_backend/issues)
- **Feature Requests**: [GitHub Discussions](https://github.com/MikeMikerson/pokemon_go_code_sharing_backend/discussions)
- **Documentation**: [docs/](docs/)

## 🏗 Architecture

The application follows Domain-Driven Design principles with a layered architecture:

- **Controller Layer**: REST API endpoints and request/response handling
- **Service Layer**: Business logic and orchestration
- **Repository Layer**: Data access and persistence
- **Domain Layer**: Core business entities and value objects

### Design Patterns Used

- **Repository Pattern**: Data access abstraction
- **DTO Pattern**: Data transfer between layers
- **Circuit Breaker Pattern**: Resilience and fault tolerance
- **Factory Pattern**: Object creation
- **Strategy Pattern**: Configurable behavior

## 🔄 Database Schema

The application uses PostgreSQL with Flyway migrations for schema management:

- **friend_codes**: Core table for Pokemon Go friend codes
- **users**: User profiles and preferences
- **flyway_schema_history**: Migration tracking

See [src/main/resources/db/migration/](src/main/resources/db/migration/) for all migration scripts.

---

**Happy Coding!** 🎮✨
