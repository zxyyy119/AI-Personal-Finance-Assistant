package org.example.service;

import org.example.dto.CreateTransactionRequest;
import org.example.dto.TransactionSummary;
import org.example.exception.TransactionNotFoundException;
import org.example.model.Transaction;
import org.example.model.TransactionCategory;
import org.example.model.TransactionType;
import org.example.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
        when(transactionRepository.findByType(TransactionType.INCOME)).thenReturn(List.of(
                transaction(TransactionType.INCOME, TransactionCategory.OTHER, "5000.00")));
        when(transactionRepository.findByType(TransactionType.EXPENSE)).thenReturn(List.of(
                transaction(TransactionType.EXPENSE, TransactionCategory.FOOD, "23.50"),
                transaction(TransactionType.EXPENSE, TransactionCategory.TRANSPORT, "15.00")));

        TransactionSummary summary = transactionService.getSummary();

        assertEquals(new BigDecimal("5000.00"), summary.totalIncome());
        assertEquals(new BigDecimal("38.50"), summary.totalExpense());
        assertEquals(new BigDecimal("4961.50"), summary.balance());
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
}
