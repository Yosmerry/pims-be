# Personal Inventory Management System — Backend

PIMS Backend is a Java REST API that allows each user to organize personal
assets using categories, locations, inventory details, and item images.

> [!WARNING]
> **Assignment disclaimer:** This application was created for a job assignment
> and demonstration purposes. It is a simplified implementation and is not yet
> intended for enterprise production use. Production deployment would require
> additional security review, secret management, rate limiting, scalable object
> storage, observability, concurrency hardening, backup and disaster recovery,
> CI/CD controls, and infrastructure hardening.

## Documentation

### Foundation

- [Application flow summary](docs/foundation/summary.md)
- [Entity relationship diagram](docs/foundation/pims-erd.jpg)
- [Flyway database schema](src/main/resources/db/migration/V1__create_initial_schema.sql)

### API Contracts

- [All API contracts](docs/api/)
- [Authentication API](docs/api/AuthApi.md)
- [Category API](docs/api/CategoryApi.md)
- [Location API](docs/api/LocationApi.md)
- [Inventory API](docs/api/InventoryApi.md)
- [Image API](docs/api/ImageApi.md)

## Implemented Scope

- User registration, login, refresh token, logout, and current-user profile.
- Stateless JWT authentication with a persisted, hashed refresh token.
- Per-user category and location CRUD.
- Per-user inventory CRUD with filters, sorting, and pagination.
- WebP image upload, gallery retrieval, content retrieval, and deletion.
- Maximum five images per item and 100 KB per image.
- Soft deletion, audit fields, TSID internal IDs, and public sequence codes.
- PostgreSQL schema migration with Flyway.
- OpenAPI documentation and Swagger UI.

## Technology

- Java 21
- Spring Boot 4
- Spring Security and JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven
- springdoc-openapi

The API base path is `/api/v1`. When the application is running, Swagger UI is
available at `/swagger-ui.html`.
