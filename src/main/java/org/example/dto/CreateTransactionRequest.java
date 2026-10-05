package org.example.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import org.example.model.TransactionCategory;
import org.example.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Validated JSON body used to create a transaction. */
public record CreateTransactionRequest(
        @NotNull TransactionType type,
        @NotNull TransactionCategory category,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull LocalDate transactionDate,
        @Size(max = 500) String description
) {
}
