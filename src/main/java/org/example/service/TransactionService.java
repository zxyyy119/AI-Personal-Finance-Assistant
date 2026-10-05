package org.example.service;

import org.example.dto.CreateTransactionRequest;
import org.example.dto.TransactionSummary;
import org.example.exception.TransactionNotFoundException;
import org.example.model.Transaction;
import org.example.model.TransactionCategory;
import org.example.model.TransactionType;
import org.example.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Applies finance rules while delegating persistence to TransactionRepository. */
@Service
@Transactional(readOnly = true)
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction createTransaction(CreateTransactionRequest request) {
        Transaction transaction = new Transaction(request.transactionDate(), request.type(), request.category(),
                request.amount(), request.description() == null ? "" : request.description().trim());
        return transactionRepository.save(transaction);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Transaction getTransactionById(long id) {
        return transactionRepository.findById(id).orElseThrow(() -> new TransactionNotFoundException(id));
    }

    @Transactional
    public void deleteTransaction(long id) {
        transactionRepository.delete(getTransactionById(id));
    }

    public TransactionSummary getSummary() {
        BigDecimal totalIncome = sum(transactionRepository.findByType(TransactionType.INCOME));
        BigDecimal totalExpense = sum(transactionRepository.findByType(TransactionType.EXPENSE));
        return new TransactionSummary(totalIncome, totalExpense, totalIncome.subtract(totalExpense));
    }

    public Map<TransactionCategory, BigDecimal> getExpenseByCategory() {
        Map<TransactionCategory, BigDecimal> totals = new EnumMap<>(TransactionCategory.class);
        for (TransactionCategory category : TransactionCategory.values()) {
            totals.put(category, sum(transactionRepository.findByTypeAndCategory(TransactionType.EXPENSE, category)));
        }
        return totals;
    }

    private BigDecimal sum(List<Transaction> transactions) {
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction transaction : transactions) {
            total = total.add(transaction.getAmount());
        }
        return total;
    }
}
