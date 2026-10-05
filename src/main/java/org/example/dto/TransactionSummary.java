package org.example.dto;

import java.math.BigDecimal;

/** Totals returned by the transaction summary endpoint. */
public record TransactionSummary(BigDecimal totalIncome, BigDecimal totalExpense, BigDecimal balance) {
}
