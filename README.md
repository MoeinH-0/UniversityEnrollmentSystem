# 🎓 University Enrollment System

An enterprise-grade backend platform engineered to handle academic course registration, prerequisite validation, and curriculum management. Built with a strong emphasis on maintainability and scalability, this project strictly adheres to **Layered Architecture**, **Domain-Driven Design (DDD)**, and **SOLID** principles.

## 🚀 Key Features

* **Dynamic Policy Enforcement**: Utilizes the **Strategy Pattern** for handling different student academic tiers (Regular, Probation, Honors, Guest).
* **High-Integrity Concurrency**: Manages real-time seat allocation, capacity locking, and seat recycling upon course drops.
* **CQRS-Lite Implementation**: Strict separation of read (Queries) and write (Commands) operations using immutable records.
* **Resilient Error Handling**: Centralized exception architecture (`GlobalExceptionHandler`) with consistent standard REST responses.

## 🛠 Tech Stack

* **Core**: Java 17+, Spring Boot
* **Database & ORM**: PostgreSQL, Hibernate 7, Spring Data JPA
* **API Documentation**: OpenAPI 3.0 / Swagger UI
* **Testing**: JUnit 5, AssertJ, Mockito (100% coverage across 595 tests)

## 📐 Architecture & Package Structure

The codebase is organized using a **Layered Architecture** heavily influenced by **DDD**, ensuring a clear separation of concerns across the following packages:

1. **`controller/` (Presentation Layer)**: Contains REST controllers to handle HTTP requests/responses and route them to application services.
2. **`service/` (Application Layer)**: Orchestrates business use cases, coordinates CQRS commands/queries, and acts as the bridge between controllers and the core domain.
3. **`domain/` (Core Layer)**: The heart of the software business logic. It includes:
   - `entity/`: Rich domain models encapsulating state and behavior.
   - `service.enrollment/`: Domain services handling complex business rules that span multiple entities.
4. **`repository/` (Data Layer)**: Spring Data JPA interfaces for database persistence and entity retrieval.
5. **`config/`**: Application-wide configurations such as OpenAPI/Swagger setups.

## 📚 API Documentation (Swagger UI)

This project uses **Springdoc OpenAPI 3.0** for interactive API documentation. 

Once the application is running, you can explore, test, and interact with all endpoints directly through the Swagger UI:
* **Interactive Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **OpenAPI 3.0 JSON Specification**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)
