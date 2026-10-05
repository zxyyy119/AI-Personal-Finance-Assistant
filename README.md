# AI Personal Finance Assistant

A Java personal finance learning project for practicing Java, object-oriented design, persistence, and application development.

## Current version

**V0.3 — Spring Boot REST API with MySQL persistence**

V0.1 introduced the console-based income and expense tracker. V0.2 added local CSV storage. V0.3 adds a Spring Boot REST API backed by MySQL and imports existing V0.2 CSV records into an empty database.

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

## Technology

- Java 26
- Spring Boot 4 and Spring Web MVC
- Spring Data JPA / Hibernate
- MySQL
- Maven Wrapper
- JUnit and Spring Boot Test

## Requirements and configuration

- JDK 26
- MySQL running locally or at a configured database URL
- A MySQL database named `personal_finance` (or set another database URL)

The application reads database settings from environment variables. Set `DB_USERNAME` and `DB_PASSWORD` in your shell; optionally set `DB_URL` and `SERVER_PORT`. Do not commit credentials. `.env.example` contains empty placeholders only.

For example, in PowerShell:

```powershell
$env:DB_URL = "jdbc:mysql://127.0.0.1:3306/personal_finance"
$env:DB_USERNAME = "your-database-user"
$env:DB_PASSWORD = "your-database-password"
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
```

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
├── repository/          # Spring Data JPA repository
├── service/             # Business rules and CSV import
└── storage/             # V0.2 CSV reader/writer
```

## Version history and roadmap

- V0.1: console-based transaction management ✅
- V0.2: CSV-based persistent transaction storage ✅
- V0.3: Spring Boot REST API and MySQL persistence ✅
- Later: budgeting, data visualization, financial analysis, and AI-assisted features

The long-term goal is to grow from a simple finance tracker into an intelligent personal finance assistant combining computer science, finance, and AI.
