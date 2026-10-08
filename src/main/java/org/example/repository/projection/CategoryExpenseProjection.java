package org.example.repository.projection;

import org.example.model.TransactionCategory;

import java.math.BigDecimal;

/** One grouped expense total returned by the database. */
public interface CategoryExpenseProjection {
    TransactionCategory getCategory();

    BigDecimal getTotal();
}
