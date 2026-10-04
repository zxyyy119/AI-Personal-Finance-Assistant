# 💰 AI Personal Finance Assistant

A Java-based personal finance management application developed step by step.

This project is built as a learning project to practice Java programming, object-oriented design, Git/GitHub, data persistence, and financial application development.

## 📌 Current Version

**V0.2 — Persistent Console Finance Assistant**

## ✨ Features

- Add income transactions
- Add expense transactions
- View all transactions
- Delete transactions
- Calculate total income
- Calculate total expenses
- Calculate current balance
- View expenses by category
- Validate user input
- Save transactions to CSV
- Automatically load transactions on startup
- Persist transaction deletions
- Preserve transaction IDs across restarts
- Handle CSV fields containing commas
- Skip invalid CSV records safely

## 💾 Data Persistence

Transaction data is stored locally in:

`data/transactions.csv`

The application automatically loads saved transactions when it starts and saves changes whenever transactions are added or deleted.

## 🧱 Project Structure

```text
src/main/java/org/example/
├── App.java
├── Main.java
├── model/
│   ├── Transaction.java
│   ├── TransactionCategory.java
│   └── TransactionType.java
├── service/
│   └── TransactionService.java
└── storage/
    └── CsvTransactionRepository.java

🛠️ Technologies
- Java
- Maven project structure
- IntelliJ IDEA
- CSV
- Git
- GitHub
🚀 Version History
V0.1
Basic console-based personal finance assistant. ✅
V0.2
Added CSV-based persistent transaction storage. ✅
🗺️ Roadmap
Future Versions
- Database integration
- Spring Boot backend
- REST API
- Data visualization
- Budget management
- Financial analysis
- AI-powered personal finance features
- Web or mobile interface
🎯 Project Goal
The long-term goal of this project is to evolve from a simple Java console application into an intelligent personal finance assistant that combines:
Computer Science + Finance + AI
📖 Development Status
This project is currently under active development.
Current version: V0.2
