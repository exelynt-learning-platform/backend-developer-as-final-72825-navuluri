# RESTful Resource Booking System

A robust, production-grade RESTful API built with **Spring Boot 3**, **Java 17+**, **Spring Security**, **JWT Authentication**, and **Spring Data JPA**.

The system enables users to view available resources and make reservations while providing Administrators with full access to manage resources, users, and reservations.

---

## Technical Stack & Architecture

- **Framework**: Spring Boot 3.2.5 (Java 17+)
- **Security**: Spring Security 6 with JJWT (io.jsonwebtoken 0.12.5)
- **Database**: Spring Data JPA / Hibernate with support for **H2** (In-Memory), **MySQL**, and **PostgreSQL**
- **Validation**: Jakarta Bean Validation (`@Valid`, `@NotNull`, `@Min`, `@NotBlank`, etc.)
- **Documentation**: OpenAPI 3.0 / Swagger UI (`springdoc-openapi-starter-webmvc-ui`)

---

## Key Features & Business Rules

1. **JWT Authentication & RBAC**:
   - `POST /auth/login` returns a signed Bearer JWT token.
   - `POST /auth/register` permits creating new users.
   - Pre-seeded `ADMIN` and `USER` accounts provided for testing.
2. **Resource Management**:
   - `ADMIN` role: Full CRUD (Create, Read, Update, Delete) access for resources.
   - `USER` role: Read-only access to available resources.
3. **Reservation Management**:
   - `USER` role: Can create reservations and view **only their own** reservations.
   - **Security Guarantee**: User identity is extracted **strictly from the JWT token**, not from request body parameters.
   - Statuses: `PENDING`, `CONFIRMED`, `CANCELLED`.
   - Prices stored as decimal values (`BigDecimal`).
4. **Filtering, Pagination, and Sorting**:
   - Filter reservations dynamically by `status`, `minPrice`, and `maxPrice`.
   - Paginate results using `page` (0-indexed) and `size` parameters.
   - Optional sorting (e.g. `sort=createdAt,desc`, `sort=price,asc`).
   - `ADMIN` gets all matching reservations across all users; `USER` gets only their own matching reservations.
5. **Validation & Exception Handling**:
   - `@RestControllerAdvice` Global Exception Handler returning standardized JSON error payloads (`timestamp`, `status`, `error`, `message`, `path`, `details`).

---

## Seed Data & Credentials

On application startup, `DataInitializer` automatically seeds default test accounts and sample data:

| Username | Password | Role | Description |
| :--- | :--- | :--- | :--- |
| `admin` | `admin123` | `ROLE_ADMIN` | Administrator with full system privileges |
| `user` | `user123` | `ROLE_USER` | Standard User with booking privileges |

### Seed Resources:
1. **Executive Conference Room A** (Type: `Room`, Capacity: 12, Price: `$50.00`/hr)
2. **Tesla Model 3 - EV-01** (Type: `Vehicle`, Capacity: 5, Price: `$75.00`/hr)
3. **Epson 4K Pro Cinema Projector** (Type: `Equipment`, Capacity: 1, Price: `$25.00`/hr)

---

## Quickstart & Setup Instructions

### Prerequisites
- **Java 17** or higher installed (`java -version`)

### 1. Build the Application
Run the Maven wrapper to compile and build the project JAR:
```bash
mvnw.cmd clean package
```

### 2. Run with Default H2 Database (In-Memory, Zero Setup)
By default, the application runs with an **H2 In-Memory Database**:
```bash
mvnw.cmd spring-boot:run
```
- **H2 Web Console**: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:bookingdb`
  - Username: `sa`
  - Password: *(leave blank)*

---

## Database Configuration (MySQL / PostgreSQL)

You can easily switch database profiles via environment variables or command-line args:

### MySQL Profile (`mysql`)
```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
```
Environment Variables:
```env
SPRING_PROFILES_ACTIVE=mysql
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/bookingdb?useSSL=false&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_password
```

### PostgreSQL Profile (`postgres`)
```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=postgres
```
Environment Variables:
```env
SPRING_PROFILES_ACTIVE=postgres
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/bookingdb
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password
```

---

## Interactive API Documentation (Swagger / OpenAPI)

Once the application is running, open your browser to interact with the API endpoints:
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Spec**: `http://localhost:8080/v3/api-docs`

> **Note**: Click the **Authorize** button in Swagger UI and enter your JWT Bearer Token (e.g. `Bearer <your_token>`) obtained from `/auth/login`.

---

## Summary of API Endpoints

### 🔑 Authentication (`/auth`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/auth/login` | Public | Authenticate user & return JWT token |
| `POST` | `/auth/register` | Public | Register new user account |

### 🏢 Resources (`/api/resources`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/resources` | `USER`, `ADMIN` | List all resources |
| `GET` | `/api/resources/{id}` | `USER`, `ADMIN` | Get resource by ID |
| `POST` | `/api/resources` | `ADMIN` | Create new resource |
| `PUT` | `/api/resources/{id}` | `ADMIN` | Update existing resource |
| `DELETE` | `/api/resources/{id}` | `ADMIN` | Delete resource |

### 📅 Reservations (`/api/reservations`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/reservations` | `USER`, `ADMIN` | Create reservation (User ID from JWT) |
| `GET` | `/api/reservations` | `USER`, `ADMIN` | Filter, paginate & sort reservations |
| `GET` | `/api/reservations/{id}` | `USER`, `ADMIN` | Get reservation by ID |
| `PUT` | `/api/reservations/{id}` | `USER`, `ADMIN` | Update/Cancel reservation |
| `DELETE` | `/api/reservations/{id}` | `ADMIN` | Delete reservation |

#### Reservation Query Parameters (`GET /api/reservations`):
- `status`: `PENDING`, `CONFIRMED`, or `CANCELLED`
- `minPrice`: Decimal value (e.g. `50.00`)
- `maxPrice`: Decimal value (e.g. `300.00`)
- `page`: Page index (default: `0`)
- `size`: Page size (default: `10`)
- `sort`: Field and direction (e.g. `createdAt,desc` or `price,asc`)

---

## Sample Request Payloads

### 1. Login (`POST /auth/login`)
```json
{
  "username": "user",
  "password": "user123"
}
```

### 2. Create Reservation (`POST /api/reservations`)
```json
{
  "resourceId": 1,
  "startTime": "2026-09-01T09:00:00",
  "endTime": "2026-09-01T12:00:00",
  "status": "PENDING"
}
```

---

## License & Author
Created for Final Project Assignment - Backend Developer.
