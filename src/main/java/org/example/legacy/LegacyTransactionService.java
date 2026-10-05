package org.example.legacy;

import org.example.model.Transaction;
import org.example.model.TransactionCategory;
import org.example.model.TransactionType;
import org.example.storage.CsvTransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** V0.2 console service retained for the legacy App entry point. */
public class LegacyTransactionService {
    private final List<Transaction> transactions = new ArrayList<>();
    private final CsvTransactionRepository repository;
    private long nextId = 1;

    public LegacyTransactionService() {
        this(new CsvTransactionRepository());
    }

    public LegacyTransactionService(CsvTransactionRepository repository) {
        this.repository = repository;
        transactions.addAll(repository.loadTransactions());
        for (Transaction transaction : transactions) {
            if (transaction.getId() >= nextId) nextId = transaction.getId() + 1;
        }
    }

    public void addTransaction(LocalDate date, TransactionType type, TransactionCategory category,
                               BigDecimal amount, String description) {
        transactions.add(new Transaction(nextId, date, type, category, amount, description));
        nextId++;
        repository.saveTransactions(transactions);
    }

    public boolean removeTransaction(long id) {
        Transaction transaction = findTransactionById(id);
        if (transaction == null) return false;
        transactions.remove(transaction);
        repository.saveTransactions(transactions);
        return true;
    }

    public Transaction findTransactionById(long id) {
        for (Transaction transaction : transactions) {
            if (transaction.getId() == id) return transaction;
        }
        return null;
    }

    public void showTransactions() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions have been recorded yet.");
            return;
        }
        System.out.println("\n========== All Transactions ==========");
        for (Transaction transaction : transactions) System.out.println(transaction);
    }

    public BigDecimal calculateTotalIncome() { return calculateTotalByType(TransactionType.INCOME); }
    public BigDecimal calculateTotalExpense() { return calculateTotalByType(TransactionType.EXPENSE); }
    public BigDecimal calculateBalance() { return calculateTotalIncome().subtract(calculateTotalExpense()); }

    public BigDecimal calculateExpenseByCategory(TransactionCategory category) {
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction transaction : transactions) {
            if (transaction.getType() == TransactionType.EXPENSE && transaction.getCategory() == category) {
                total = total.add(transaction.getAmount());
            }
        }
        return total;
    }

    public void showExpenseByCategory() {
        System.out.println("\n========== Expenses by Category ==========");
        for (TransactionCategory category : TransactionCategory.values()) {
            System.out.println(category + ": " + calculateExpenseByCategory(category));
        }
    }

    private BigDecimal calculateTotalByType(TransactionType type) {
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction transaction : transactions) {
            if (transaction.getType() == type) total = total.add(transaction.getAmount());
        }
        return total;
    }
}
