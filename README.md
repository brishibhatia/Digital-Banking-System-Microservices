# Digital Banking System Microservices

A Java and Spring Boot banking project organized into independent Maven services.

## Project structure

- `account-service`: account management, balance lookup, account blocking, and balance deduction APIs.
- `api-gateway`: API gateway service scaffold.
- `transaction-service`: transaction service scaffold.
- `payment-service`: payment service scaffold.
- `fraud-detection-service`: fraud detection service scaffold.
- `notification-service`: notification service scaffold.
- `docker-compose.yml`: Redis, Kafka, and ZooKeeper for local development.

Each service includes its own `pom.xml`, Maven wrapper, application configuration, and tests.

## Local development

Install Java 17 and Docker with Docker Compose. Start the supporting infrastructure from the project root:

```sh
docker compose up -d
```

Build or run a service from its directory using the Maven wrapper:

```powershell
cd account-service
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

On macOS or Linux, use `./mvnw` instead of `.\mvnw.cmd`.

The services are under development. Configure the database connection, service ports, and any required integrations before running them together; the current application YAML files only declare service names.
