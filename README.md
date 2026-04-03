# Finance Dashboard Backend

A secure, role-based REST API for managing financial records and dashboard analytics — built with **Java 21**, **Spring Boot 4.0**, **PostgreSQL**, and **Redis**.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Features](#features)
- [Role & Access Control](#role--access-control)
- [API Endpoints](#api-endpoints)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Technical Decisions & Trade-offs](#technical-decisions--trade-offs)
- [Known Limitations](#known-limitations)
- [Testing](#testing)

---

## Overview

This is the backend for a **Finance Dashboard System** where users interact with financial records based on their assigned role. The system supports full CRUD on financial entries, role-based access control, aggregated dashboard analytics with Redis caching, and JWT-based stateless authentication.

---

## Tech Stack

| Layer         | Technology                          |
|---------------|-------------------------------------|
| Language      | Java 21                             |
| Framework     | Spring Boot 4.0                     |
| Security      | Spring Security + JWT (jjwt 0.12.6) |
| Database      | PostgreSQL                          |
| Caching       | Redis                               |
| ORM           | Spring Data JPA / Hibernate         |
| Mapping       | MapStruct                           |
| Validation    | Jakarta Validation                  |
| Build Tool    | Maven                               |
| Boilerplate   | Lombok                              |

---

## Architecture

The project follows a clean layered architecture with strict separation of concerns:

```
Request
  └── Controller          (HTTP layer — routing, request/response)
        └── Service       (Business logic — validation, rules, auth checks)
              └── Repository  (Data access — JPA queries)
                    └── Database (PostgreSQL)
```

Each layer communicates only with the layer directly below it. DTOs are used at every boundary — entities never leave the service layer.

```
src/main/java/Finance/Finanace/
├── Configuration/        Spring Security + Redis config
├── Controller/           REST controllers (Auth, User, Record, Dashboard)
├── Data/                 DataInitializer — seeds default admin on startup
├── DTO/
│   ├── Request/          Input DTOs with validation annotations
│   └── Response/         Output DTOs (ApiResponse wrapper, domain responses)
├── Exceptions/           Custom exceptions + GlobalExceptionHandler
├── Mapper/               MapStruct mappers (Entity ↔ DTO)
├── Models/
│   ├── Enums/            Role, TransactionType, UserStatus
│   ├── FinancialRecord
│   └── User
├── Repository/           JPA repositories with custom JPQL queries
├── Security/             JwtUtil, JwtFilter, UserDetailsService
└── Service/              Business logic layer
```

---

## Features

### User & Role Management
- Admin can create, view, update, and deactivate users
- Three roles: `ADMIN`, `ANALYST`, `VIEWER`
- User status: `ACTIVE` / `INACTIVE` — inactive users cannot log in

### Financial Records
- Full CRUD on financial entries
- Each record has: `amount`, `type` (INCOME/EXPENSE), `category`, `date`, `description`
- Pagination on all record listings
- Filter by `category`, `type`, `startDate`, `endDate`

### Dashboard Analytics
- Total income, total expenses, net balance
- Category-wise spending totals
- Recent 10 transactions
- Month-by-month income/expense/net breakdown
- **Redis-cached** — computed once, invalidated automatically on any record change

### Security
- JWT authentication via `Authorization: Bearer <token>` header or `jwt` cookie
- Stateless sessions (no server-side session state)
- BCrypt password hashing (strength 10)
- Access control enforced at the **service layer** using `@PreAuthorize`

### Validation & Error Handling
- All inputs validated with Jakarta Validation annotations
- Centralized `GlobalExceptionHandler` maps every exception to the correct HTTP status
- Consistent `ApiResponse<T>` wrapper on all responses

---

## Role & Access Control

| Action                        | VIEWER | ANALYST | ADMIN |
|-------------------------------|--------|---------|-------|
| View dashboard summary        | ✅      | ✅       | ✅     |
| View financial records        | ❌      | ✅       | ✅     |
| Create financial record       | ❌      | ❌       | ✅     |
| Update financial record       | ❌      | ❌       | ✅     |
| Delete financial record       | ❌      | ❌       | ✅     |
| Create / manage users         | ❌      | ❌       | ✅     |
| View all users                | ❌      | ❌       | ✅     |
| Deactivate user               | ❌      | ❌       | ✅     |

Access control is applied via `@PreAuthorize` on every service method — not just at the controller level — so it is enforced regardless of how the service is invoked.

---

## API Endpoints

All endpoints except `/api/auth/**` require a valid JWT token in the `Authorization` header:
```
Authorization: Bearer <your_token>
```

### Auth
| Method | Endpoint          | Access | Description              |
|--------|-------------------|--------|--------------------------|
| POST   | /api/auth/login   | Public | Login and receive JWT    |

**Login request body:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Login response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "admin",
    "role": "ADMIN",
    "expiresIn": 86400
  }
}
```

---

### Users
| Method | Endpoint                  | Role  | Description         |
|--------|---------------------------|-------|---------------------|
| POST   | /api/users/newUser        | ADMIN | Create a new user   |
| GET    | /api/users/allUsers       | ADMIN | Get all users       |
| GET    | /api/users/{id}           | ADMIN | Get user by ID      |
| PATCH  | /api/users/{id}           | ADMIN | Update user         |
| PATCH  | /api/users/{id}/deactivate| ADMIN | Deactivate user     |

**Create user request:**
```json
{
  "username": "alice",
  "email": "alice@example.com",
  "password": "securePass123",
  "role": "ANALYST"
}
```

---

### Financial Records
| Method | Endpoint                       | Role           | Description                    |
|--------|--------------------------------|----------------|--------------------------------|
| GET    | /api/records                   | ANALYST, ADMIN | Get all records (with filters) |
| GET    | /api/records/{id}              | ANALYST, ADMIN | Get record by ID               |
| POST   | /api/records/createRecord      | ADMIN          | Create a record                |
| PATCH  | /api/records/{id}              | ADMIN          | Update a record                |
| DELETE | /api/records/{id}              | ADMIN          | Delete a record                |

**Filter parameters (query string):**
```
GET /api/records?category=Salary&type=INCOME&from=2024-01-01&to=2024-12-31&page=0&size=20
```

**Create record request:**
```json
{
  "amount": 5000.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2024-04-01",
  "description": "April salary"
}
```

---

### Dashboard
| Method | Endpoint               | Role                    | Description              |
|--------|------------------------|-------------------------|--------------------------|
| GET    | /api/dashboard/summary | VIEWER, ANALYST, ADMIN  | Full dashboard summary   |

**Response:**
```json
{
  "success": true,
  "data": {
    "totalIncome": 50000.00,
    "totalExpenses": 22000.00,
    "netBalance": 28000.00,
    "categoryTotals": {
      "Salary": 50000.00,
      "Rent": 12000.00,
      "Food": 10000.00
    },
    "recentTransactions": [...],
    "monthlySummary": {
      "Jan": { "income": 10000.00, "expenses": 4000.00, "net": 6000.00 },
      "Feb": { "income": 10000.00, "expenses": 3500.00, "net": 6500.00 }
    }
  }
}
```

---

## Getting Started

### Prerequisites

- Java 21
- Maven
- PostgreSQL (running on port 5432)
- Redis (running on port 6379)

### 1. Clone the repository

```bash
git clone <your-github-url>
cd Finanace/Finanace
```

### 2. Set up PostgreSQL

```sql
CREATE DATABASE Finance;
```

### 3. Configure environment

Open `src/main/resources/application.properties` and update:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/Finance
spring.datasource.username=your_postgres_username
spring.datasource.password=your_postgres_password

spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=your_redis_password
```

> **Note:** For production, these should be injected via environment variables, not hardcoded.

### 4. Run the application

```bash
mvn spring-boot:run
```

The server starts on **http://localhost:9090**

### 5. Default admin credentials

On first startup, a default admin user is seeded automatically:

```
Username : admin
Password : admin123
```

Use these to log in at `POST /api/auth/login` and get your JWT token.

### 6. Run tests

```bash
mvn test
```

---

## Technical Decisions & Trade-offs

**PostgreSQL over in-memory/SQLite**
PostgreSQL was chosen for its ACID compliance and accurate `BigDecimal` handling — critical for monetary values. The trade-off is a required running DB instance during local setup.

**JWT over sessions**
Stateless JWT authentication keeps the backend horizontally scalable. The trade-off is that token revocation requires a blocklist (not implemented in this scope).

**Service-level `@PreAuthorize` (not just controller-level)**
Access control is enforced inside service methods, not just at route level. This means a role check cannot be bypassed regardless of how the method is called — from a controller, scheduler, or any other entry point.

**Redis caching on dashboard summary**
The dashboard aggregates multiple queries. Redis caching (`@Cacheable`) prevents repeated heavy DB computation. Cache is evicted automatically via `@CacheEvict` on any create, update, or delete operation — ensuring data consistency without manual invalidation.

**DTO pattern throughout**
Request and Response DTOs are used at every boundary. Entities never leak out of the service layer. This prevents over-posting, keeps the API contract independent of the schema, and avoids exposing sensitive fields like hashed passwords.

**Database indexes on `date`, `category`, `type`**
These are the exact columns used in the record filter query. Indexes were added proactively to ensure acceptable query performance as data grows.

---

## Known Limitations

- **Credentials in `application.properties`** — DB password, JWT secret, and Redis password are present in the config file for local evaluation. In a real deployment, these would be injected via environment variables or a secrets manager.
- **`spring.security.debug=true`** — enabled for development visibility. Must be turned off in production.
- **No token blacklisting** — once a JWT is issued it is valid until expiry. A logout endpoint with Redis-based blocklist would be the next step.
- **No soft delete** — record deletion is permanent. An `isDeleted` flag with filtered queries would be the production approach.
- **Project name typo** — the artifact and package are named `Finanace` (double 'a') — carried from initial setup and retained to avoid breaking import paths.

---

## Testing

40 unit tests covering all core business logic — no database or network required (pure Mockito mocks).

| Test Class                    | Tests | Coverage                                          |
|-------------------------------|-------|---------------------------------------------------|
| UserServiceTest               |  11   | Create, get, update, deactivate — edge cases      |
| FinancialRecordServiceTest    |  10   | CRUD, filtering, null-field update safety         |
| DashBoardServiceTest          |   7   | Net balance, category totals, monthly grouping    |
| AuthServiceTest               |   3   | Login success, bad credentials, token generation  |
| GlobalExceptionHandlerTest    |   9   | HTTP 400 / 401 / 403 / 404 / 409 / 500 responses  |

---

## GitHub

🔗 [Repository Link](_your_github_url_here_)
