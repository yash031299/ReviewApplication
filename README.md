# ReviewApp

A modern, extensible Java Swing application for managing, filtering, and analyzing product reviews. Built with a clean, maintainable architecture inspired by Hexagonal (Ports & Adapters) and classic DDD layering.

## Features
- Rich filtering: by author, title, product, store, rating, date, and time
- Flexible date/time input: ISO, yyyy/MM/dd, MM/dd/yyyy
- Combined sorting: by rating (groups), then by date within each rating group
- Pagination: fast, asynchronous UI with configurable page size
- Statistics dashboard: aggregate stats and charts
- Pluggable repositories: SQLite and in-memory
- Clean architecture: easily testable, maintainable, and extensible

## Prerequisites
- Java 17 or later
- Maven 3.6+

## Project Structure

```
ReviewApp/
├── src/
│   └── main/
│       └── java/com/reviewapp/
│           ├── Main.java                  # Application entry point
│           ├── adapter/                   # Adapter layer: all external interfaces
│           │   ├── cli/                   # Command-line adapters
│           │   ├── ingest/                # Data ingestion (e.g., JSON parsers)
│           │   ├── jdbc/                  # JDBC/SQLite repository adapter
│           │   ├── memory/                # In-memory repository adapter
│           │   └── ui/                    # Java Swing UI (components, util)
│           ├── application/               # Application/service layer
│           │   ├── dto/                   # Data transfer objects
│           │   └── service/               # Use case/business logic services
│           ├── boot/                      # Bootstrapping, config, factories
│           ├── domain/                    # Domain layer
│           │   ├── model/                 # Core domain models (Review, Filters, Statistics)
│           │   └── port/                  # Ports (interfaces) for repositories/services
│           └── util/                      # Utilities
│
├── pom.xml                               # Maven build file
├── reviews.db                            # SQLite database
└── README.md
```

## Architectural Pattern

- **Hexagonal Architecture (Ports & Adapters):**
  - The core business logic (domain, service) is isolated from infrastructure and UI concerns.
  - **Ports** (interfaces) in `domain.port` define what the app needs (e.g., `ReviewRepositoryPort`),
    and **Adapters** in `adapter.*` implement those interfaces for JDBC, memory, UI, CLI, etc.
  - Application logic in `application.service` uses only ports, never concrete adapters.
- **Layered DDD (Domain-Driven Design):**
  - `domain.model` holds the core entities and value objects (e.g., `Review`, `Filters`).
  - `application` layer exposes use cases and orchestrates business logic.
  - `adapter` layer handles all input/output (UI, DB, CLI, ingestion).
  - `boot` sets up wiring, factories, and configuration.

## Main Design Patterns Used
- **Hexagonal (Ports & Adapters):** Decouples core logic from technology-specific code.
- **Repository Pattern:** Abstracts data access for reviews (JDBC, in-memory, etc.).
- **Factory Pattern:** Used in `boot/factory` for instantiating repositories and services.
- **DTO Pattern:** For data transfer between layers (`application/dto`).
- **MVC (UI):** Swing UI components are organized for separation of concerns.
- **Builder Pattern:** Used in the model layer for building complex objects fluently and immutably.

## Getting Started

### Build & Run
```
mvn clean package
java -cp target/ReviewApp-1.0-SNAPSHOT.jar com.reviewapp.Main
```

### Usage
- Launch the app and use the filter panel to search and sort reviews.
- Date/time fields accept `yyyy-MM-dd`, `yyyy/MM/dd`, or `MM/dd/yyyy`.
- Switch between SQLite and in-memory repositories via configuration.

## UI Components

The UI is built with Java Swing and follows a modular, component-oriented approach. Key UI components include:
- **FilterReviewsPanel:** Main panel for filtering, sorting, and paginating reviews.
- **StatisticsPanel:** Displays aggregate review statistics and charts.
- **SearchPanel:** Enables keyword-based searching of reviews.
- **ReviewByIdPanel:** Fetches and displays a review by its ID.
- **Reusable Components:** Custom dialogs, tables, and utility widgets for a modern, responsive experience.

All UI logic is separated from business logic and data access, supporting maintainability and testability.

---
*Yash Vishwakarma*
