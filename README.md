# AI Personal Finance Assistant

A Java personal finance learning project for practicing Java, object-oriented design, persistence, and application development.

## Current version

**V0.4 — Backend quality and maintainability upgrade**

V0.1 introduced the console-based income and expense tracker. V0.2 added local CSV storage. V0.3 added the Spring Boot REST API and MySQL persistence. V0.4 keeps those behaviors while adding API response DTOs, SQL-side aggregation, Flyway schema management, and MySQL integration tests with Testcontainers.

## Features

- Add income and expense transactions
- View all transactions or retrieve one transaction by ID
- Delete transactions
- Summarize income, expenses, and balance
- Summarize expenses by category
- Validate request data and report missing transactions
- Persist V0.3 transactions in MySQL
- Import V0.2 CSV data once when the database has no transactions
- Keep the V0.1/V0.2 console implementation in the `legacy` package
- Return `TransactionResponse` DTOs instead of exposing JPA entities
- Calculate summary values and category totals in SQL
- Validate schema with Hibernate; evolve schema with Flyway migrations

## Technology

- Java 26
- Spring Boot 4 and Spring Web MVC
- Spring Data JPA / Hibernate
- Flyway
- MySQL
- Maven Wrapper
- JUnit, Spring Boot Test, and Testcontainers

## Requirements and configuration

- JDK 26
- MySQL running locally or at a configured database URL
- A MySQL database named `personal_finance` (or set another database URL)

The application reads database settings from environment variables. Set `DB_USERNAME` and `DB_PASSWORD` in your shell; optionally set `DB_URL` and `SERVER_PORT`. Do not commit credentials. `.env.example` contains empty placeholders only.

For example, enter the password securely in PowerShell rather than placing it in the command text:

```powershell
$env:DB_USERNAME = "root"
$securePassword = Read-Host "MySQL root password" -AsSecureString
$pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
try {
    $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
}
finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
}
```

## Run and test

From the project directory:

```powershell
.\mvnw.cmd spring-boot:run
```

The application listens on port 8080 by default. Successful startup reports `Started Application` in the console.

Run tests with:

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

`mvn test` runs unit and MVC tests. `mvn package` builds the application. `mvn verify` also runs the MySQL integration test and requires Docker for Testcontainers. If Docker is absent, that test is reported as skipped, not as passed.

## Flyway schema management

`src/main/resources/db/migration/V1__create_transactions_table.sql` creates the transaction table for a new, empty database. Hibernate uses `ddl-auto=validate`, so it checks the schema but does not create, alter, or delete schema objects.

For an existing V0.3 `personal_finance` database without Flyway history, first confirm that `transactions` is the existing V0.3 table. On the first V0.4 startup only, explicitly opt into baseline:

```powershell
$env:FLYWAY_BASELINE_ON_MIGRATE = "true"
```

The configured baseline version is `1`. Flyway records the existing schema as V1 and does not execute V1 against that non-empty database. This adds Flyway history metadata; it does not drop, truncate, or rewrite transaction rows. Hibernate then validates compatibility. After a successful startup, remove the one-time setting with `Remove-Item Env:FLYWAY_BASELINE_ON_MIGRATE`. Keep it unset for normal starts and new empty databases. If schema validation fails, stop and investigate instead of enabling automatic schema updates.

## REST API

Base path: `/api/transactions`

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/api/transactions` | Create an income or expense |
| `GET` | `/api/transactions` | List all transactions |
| `GET` | `/api/transactions/{id}` | Get one transaction |
| `DELETE` | `/api/transactions/{id}` | Delete one transaction |
| `GET` | `/api/transactions/summary` | Get income, expense, and balance totals |
| `GET` | `/api/transactions/expenses/category` | Get expense totals by category |

Example create request:

```json
{
  "type": "EXPENSE",
  "category": "FOOD",
  "amount": 12.50,
  "transactionDate": "2026-10-05",
  "description": "Example transaction"
}
```

Supported types are `INCOME` and `EXPENSE`. Categories are `FOOD`, `TRANSPORT`, `SHOPPING`, `ENTERTAINMENT`, `EDUCATION`, `HOUSING`, `HEALTH`, and `OTHER`.

## CSV migration

The legacy V0.2 file is `data/transactions.csv`. When the application starts, it imports valid rows only if the MySQL `transactions` table is empty. CSV IDs are legacy identifiers and are not reused as MySQL primary keys; MySQL generates new IDs. Once the database contains transactions, later starts skip the import to prevent duplicate imports. V0.2 used this CSV file to load saved console transactions on startup and persist additions and deletions. The local `data/` directory is ignored by Git.

## V0.4 design notes

- **DTO:** `TransactionResponse` defines the API response shape; the controller maps entities to it manually.
- **SQL aggregation:** JPQL uses `SUM` for income, expense, and balance, plus `GROUP BY` for expense categories. Empty totals are normalized to `BigDecimal.ZERO`.
- **Testcontainers:** `MySqlPersistenceIT` tests Flyway, repository persistence, REST endpoints, aggregation, and CSV migration against a disposable MySQL container. It does not use the real personal database.

## Project layout

```text
src/main/java/org/example/
├── App.java             # Legacy console entry point
├── Application.java     # Spring Boot entry point
├── controller/          # REST endpoints
├── dto/                 # Request and summary data
├── exception/           # API error handling
├── legacy/              # Earlier console business logic
├── model/               # Transaction entity and enums
├── repository/          # Spring Data JPA repository and aggregate projections
├── service/             # Business rules and CSV import
└── storage/             # V0.2 CSV reader/writer
src/test/java/org/example/
├── controller/          # MVC tests
├── service/             # Unit tests
└── integration/         # MySQL Testcontainers tests (*IT)
```

## Version history and roadmap

- V0.1: console-based transaction management ✅
- V0.2: CSV-based persistent transaction storage ✅
- V0.3: Spring Boot REST API and MySQL persistence ✅
- V0.4: response DTOs, SQL aggregation, Flyway, and MySQL integration tests ✅
- Later: budgeting, data visualization, financial analysis, and AI-assisted features

The long-term goal is to grow from a simple finance tracker into an intelligent personal finance assistant combining computer science, finance, and AI.
