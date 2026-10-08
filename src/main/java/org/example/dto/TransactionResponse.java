package org.example.dto;

import org.example.model.Transaction;
import org.example.model.TransactionCategory;
import org.example.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Stable API representation; persistence-only entity details stay inside the application. */
public record TransactionResponse(
        Long id,
        LocalDate date,
        TransactionType type,
        TransactionCategory category,
        String description,
        BigDecimal amount,
        LocalDateTime createdAt
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(transaction.getId(), transaction.getDate(), transaction.getType(),
                transaction.getCategory(), transaction.getDescription(), transaction.getAmount(),
                transaction.getCreatedAt());
    }
}
