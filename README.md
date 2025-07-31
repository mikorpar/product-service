# Product microservice - REST API

A simple product microservice built with Java 21 and Spring Boot 3.

## Requirements

- Java 21 or higher
- Docker

## Setup

```bash
# 1. Clone the repository
git clone https://github.com/mikorpar/product-service.git

# 2. Navigate to the project directory
cd product-service

# 3. Create a .env file based on the example
cp .env.example .env

# 4. Create a flyway.conf file based on the example - used for manual DB migrations
cp flyway.conf.example flyway.conf
```

## How to run

```bash
# Optional: only if dev, staging or prod profile is used
docker compose up

# Run with default profile (local) - should be used for local development
./mvnw spring-boot:run

# Run with specific profile
## 1. Set environment variables - e.g.
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/product_db
export SPRING_DATASOURCE_USERNAME=user 
export SPRING_DATASOURCE_PASSWORD=pwd
## 2. Run the application with the desired profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=<profile_name>
```

**Supported Spring profiles**

| Profile   | Description             | DB migration execution |
|-----------|-------------------------|------------------------|
| `local`   | Local environment       | Automatic on startup   |
| `dev`     | Develop environment     | Manual with Maven      |
| `staging` | Staging environment     | Manual with Maven      |              
| `prod`    | Staging environment     | Manual with Maven      |  
| `test`    | Used for test execution | Not used               |

**Note**  
When `dev`, `staging` or `prod` profile is used, DB migrations are not executed on startup.  
To create initial DB table(s) and keep DB schema up-to-date, the following command needs to be executed.

```bash
# Run DB migrations
./mvnw flyway:migrate 
```

## Test execution
```bash
# Run tests
./mvnw test 
```

## API
The API is documented using OpenAPI 3.0 specification. Locally, you can access the documentation at
http://localhost:8080/swagger-ui/index.html.

| Endpoint                  | Method | Description                          | Request fields                                |
|---------------------------|--------|--------------------------------------|-----------------------------------------------|
| `/api/v1/products`        | POST   | Creates new product                  | Body:`code`,`name`,`price_eur`,`available` |
| `/api/v1/products/{code}` | GET    | Gets product by product `code`       | Path parameteres:`code`                       |
| `/api/v1/products`        | GET    | Returns a paginated list of products | Query parameters:`page`,`size`,`sort`         |
