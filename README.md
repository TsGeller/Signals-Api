# Api-Signals

This is a backend application that processes signal data from a CSV file, stores it in a PostgreSQL database, and exposes the data through RESTful API endpoints.

## Requirements

Make sure you have the following installed:

- **Java 17 or higher**
- **Maven**
- **PostgreSQL** (or another compatible database) — ensure the database is set up to persist your data.

The project uses the following key dependencies (defined in the `pom.xml`):

- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- PostgreSQL Driver
- Lombok
- Spring Boot DevTools (for development)
- Spring Boot Starter Test (for unit tests)
- H2 (for testing purposes)

## Overview

The Signal API is a Spring Boot application designed to fulfill a technical test scenario. It reads signal data from a signals.csv file, persists it in a PostgreSQL database, and provides three main functionalities via API routes:

- Retrieve the full list of signals (GET /signals).
- Retrieve a specific signal by its node_id (GET /signals/{node_id}).
- Retrieve statistics from the data in file.csv (GET /signals/stats).

  The application runs in a Docker container managed by Docker Compose, ensuring portability and consistency. It includes unit tests to prevent regressions and follows best practices for configuration and security.

## Installation

### 1. Clone the repository

```bash
git clone https://github.com/your-username/your-repository.git
cd your-repository
```

### 2. Configure the application.properties

Create an application.properties file and define your database name, username, and password to enable data persistence.

## Running the application locally

```bash
mvn spring-boot:run
```

## Running with Docker

```bash
docker-compose up --build
```

### 1. Docker Compose Configuration

The docker-compose.yml file defines two services:

- db (PostgreSQL Database)
- backend (Spring Boot Application):

## Running Tests

The tests use the **H2 in-memory database** instead of PostgreSQL to avoid creating an additional database instance.  
The logic remains the same as with PostgreSQL, and all routes are tested to ensure correct functionality.

```bash
mvn test
```
