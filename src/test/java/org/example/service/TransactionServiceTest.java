package org.example.service;

import org.example.dto.CreateTransactionRequest;
import org.example.dto.TransactionSummary;
import org.example.exception.TransactionNotFoundException;
import org.example.model.Transaction;
import org.example.model.TransactionCategory;
import org.example.model.TransactionType;
import org.example.repository.TransactionRepository;
import org.example.repository.projection.CategoryExpenseProjection;
import org.example.repository.projection.TransactionSummaryProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void createTransactionSavesTheRequestedValues() {
        CreateTransactionRequest request = new CreateTransactionRequest(TransactionType.INCOME,
                TransactionCategory.OTHER, new BigDecimal("5000.00"), LocalDate.parse("2026-10-01"), "Salary");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction created = transactionService.createTransaction(request);

        assertEquals(TransactionType.INCOME, created.getType());
        assertEquals(new BigDecimal("5000.00"), created.getAmount());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void summaryAddsIncomeAndExpenseAndCalculatesBalance() {
        when(transactionRepository.calculateSummary(TransactionType.INCOME, TransactionType.EXPENSE))
                .thenReturn(summaryProjection(new BigDecimal("5000.00"),
                        new BigDecimal("38.50"), new BigDecimal("4961.50")));

        TransactionSummary summary = transactionService.getSummary();

        assertEquals(new BigDecimal("5000.00"), summary.totalIncome());
        assertEquals(new BigDecimal("38.50"), summary.totalExpense());
        assertEquals(new BigDecimal("4961.50"), summary.balance());
        verify(transactionRepository).calculateSummary(TransactionType.INCOME, TransactionType.EXPENSE);
    }

    @Test
    void emptySummaryUsesZeroInsteadOfNull() {
        when(transactionRepository.calculateSummary(TransactionType.INCOME, TransactionType.EXPENSE))
                .thenReturn(summaryProjection(null, null, null));

        TransactionSummary summary = transactionService.getSummary();

        assertEquals(BigDecimal.ZERO, summary.totalIncome());
        assertEquals(BigDecimal.ZERO, summary.totalExpense());
        assertEquals(BigDecimal.ZERO, summary.balance());
    }

    @Test
    void categoryStatisticsUseOneGroupedQueryAndKeepMissingCategoriesAtZero() {
        CategoryExpenseProjection food = categoryProjection(TransactionCategory.FOOD, new BigDecimal("23.50"));
        CategoryExpenseProjection transport = categoryProjection(TransactionCategory.TRANSPORT,
                new BigDecimal("15.00"));
        when(transactionRepository.sumByCategory(TransactionType.EXPENSE)).thenReturn(List.of(food, transport));

        Map<TransactionCategory, BigDecimal> totals = transactionService.getExpenseByCategory();

        assertEquals(new BigDecimal("23.50"), totals.get(TransactionCategory.FOOD));
        assertEquals(new BigDecimal("15.00"), totals.get(TransactionCategory.TRANSPORT));
        assertEquals(BigDecimal.ZERO, totals.get(TransactionCategory.SHOPPING));
        verify(transactionRepository).sumByCategory(TransactionType.EXPENSE);
    }

    @Test
    void missingTransactionProducesNotFoundException() {
        when(transactionRepository.findById(42L)).thenReturn(Optional.empty());
        assertThrows(TransactionNotFoundException.class, () -> transactionService.getTransactionById(42L));
    }

    private Transaction transaction(TransactionType type, TransactionCategory category, String amount) {
        return new Transaction(1, LocalDate.parse("2026-10-01"), type, category,
                new BigDecimal(amount), "test data");
    }

    private TransactionSummaryProjection summaryProjection(BigDecimal income, BigDecimal expense, BigDecimal balance) {
        return new TransactionSummaryProjection() {
            @Override public BigDecimal getTotalIncome() { return income; }
            @Override public BigDecimal getTotalExpense() { return expense; }
            @Override public BigDecimal getBalance() { return balance; }
        };
    }

    private CategoryExpenseProjection categoryProjection(TransactionCategory category, BigDecimal total) {
        return new CategoryExpenseProjection() {
            @Override public TransactionCategory getCategory() { return category; }
            @Override public BigDecimal getTotal() { return total; }
        };
    }
}
