package org.example.service;

import org.example.model.Transaction;
import org.example.model.TransactionCategory;
import org.example.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Stores transactions in memory and performs V0.1 finance calculations. */
public class TransactionService {
    private final List<Transaction> transactions = new ArrayList<>();
    private long nextId = 1;

    public void addTransaction(LocalDate date, TransactionType type, TransactionCategory category,
                               BigDecimal amount, String description) {
        transactions.add(new Transaction(nextId, date, type, category, amount, description));
        nextId++;
    }

    public boolean removeTransaction(long id) {
        Transaction transaction = findTransactionById(id);
        if (transaction == null) return false;
        transactions.remove(transaction);
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
            boolean isMatchingExpense = transaction.getType() == TransactionType.EXPENSE
                    && transaction.getCategory() == category;
            if (isMatchingExpense) total = total.add(transaction.getAmount());
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
