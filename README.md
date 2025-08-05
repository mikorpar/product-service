# Product web service - REST API

A simple product web service built with Java 21 and Spring Boot 3.

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

# 5. Generate private key and self-signed certificate for JWT signature generation and validation
## Note - on deployment environments (dev, staging, prod) always use CA signed certificates and secure keystore password
openssl req -x509 -newkey rsa:2048 -keyout src/main/resources/key.pem -out src/main/resources/cert.pem -days 365 -nodes -subj "/CN=ProductService" \
&& openssl pkcs12 -export -inkey src/main/resources/key.pem -in src/main/resources/cert.pem -out src/main/resources/keystore.p12 -name jtw_sign_key -passout pass:keystore-pwd \
&& rm src/main/resources/cert.pem src/main/resources/key.pem
```

## How to run

### With default profile
```bash
# Run Spring service with default profile (local)
## Should be used for local development
./mvnw spring-boot:run
```

### With non-default profile
```bash
# start PostgreSQL DB container
docker compose up

# 1. Set environment variables
## Example
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/product_db
export SPRING_DATASOURCE_USERNAME=user 
export SPRING_DATASOURCE_PASSWORD=pwd
export JWT_KEYSTORE_PWD=keystore-pwd
export OAUTH_CLIENT_ID=test-client
### "test-secret" BCrypt hash
export OAUTH_CLIENT_SECRET=$2a$10$kBTwnnW/YUg5ikdsQwUO7e9dAX0LO.pGzGmUW31cmoleYUjblwcdK
# 2. Run the application with the desired Spring profile_name
./mvnw spring-boot:run -Dspring-boot.run.profiles=<profile_name>
```

**Supported Spring profiles**

| Profile                | Standalone                                                | Embedded authorization server  | Description                                                                                          | DB migration execution  |
|------------------------|-----------------------------------------------------------|--------------------------------|------------------------------------------------------------------------------------------------------|-------------------------|
| `local` (default)      | YES                                                       | Enabled                        | Local environment                                                                                    | Automatic on startup    |
| `dev`                  | YES                                                       | Enabled                        | Develop environment                                                                                  | Manual with Maven       | 
| `staging`              | YES                                                       | Enabled                        | Staging environment                                                                                  | Manual with Maven       |
| `prod`                 | YES                                                       | Enabled                        | Staging environment                                                                                  | Manual with Maven       |
| `test`                 | YES                                                       | Enabled                        | Used for test execution                                                                              | Not used                | 
| `noauth`               | NO (*to be used in combination with standalone profiles*) | Disabled                       | Disables auth mechanisms (spring security, resource and authorization server)                        | N/A                     |
| `external-auth-server` | NO (*to be used in combination with standalone profiles*) | Disabled                       | Disables embedded authorization server and it's mandatory to configure external one (e.g. Keyclock)  | N/A                     |  


**Note**  
When `dev`, `staging` or `prod` profile is used, DB migrations are not executed on startup.  
To create DB table(s) and keep DB schema up-to-date, the following command needs to be executed.

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
The API is documented using OpenAPI 3.0 specification. Documentation is accessible locally at
<a href="http://localhost:8080/swagger-ui/index.html" target="_blank">http://localhost:8080/swagger-ui/index.html</a>.

| Endpoint                  | Method | Description                          | Authentication                                                                                                | Request fields                                |
|---------------------------|--------|--------------------------------------|---------------------------------------------------------------------------------------------------------------|-----------------------------------------------|
| `/api/v1/products`        | POST   | Creates new product                  | Bearer token - by default, when `noauth` profile is inactive <br> None - when `noauth` profile is active      | Body: `code`,`name`,`price_eur`,`available`   |
| `/api/v1/products/{code}` | GET    | Gets product by product `code`       | None                                                                                                          | Path parameteres: `code`                      |
| `/api/v1/products`        | GET    | Returns a paginated list of products | None                                                                                                          | Query parameters: `page`,`size`,`sort`        |
| `/oauth2/token`           | POST   | Returns access token                 | None                                                                                                          | Body: `grant_type`  Headers: `Authorization`  |

### Authentication

By default, OAuth client with `client_credentials` grant type is created and should be used for local development.  
Default client can be modified and new ones can be created by modifying `application-{profile}.properties` file(s).

| client_id   | client_secret | grant_type         |
|-------------|---------------|--------------------|
| test-client | test-secret   | client_credentials |

**How to get the access token**

To get access token, POST `/oauth2/token` request needs to be sent.  
**Basic authentication** is used as authentication method.  
**client_id** and **client_secret** need to be provided in the `Authorization` header in **base64** encoded format.
```bash
# Example with default OAuth test-client
curl --location 'http://localhost:8080/oauth2/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--header 'Authorization: Basic dGVzdC1jbGllbnQ6dGVzdC1zZWNyZXQ=' \
--data-urlencode 'grant_type=client_credentials'
```