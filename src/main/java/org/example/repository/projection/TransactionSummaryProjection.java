package org.example.repository.projection;

import java.math.BigDecimal;

/** Values calculated by one database aggregate query. */
public interface TransactionSummaryProjection {
    BigDecimal getTotalIncome();

    BigDecimal getTotalExpense();

    BigDecimal getBalance();
}
