package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Represents one income or expense record. */
public class Transaction {
    private final long id;
    private final LocalDate date;
    private final TransactionType type;
    private final TransactionCategory category;
    private final BigDecimal amount;
    private final String description;

    public Transaction(long id, LocalDate date, TransactionType type, TransactionCategory category,
                       BigDecimal amount, String description) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.description = description;
    }

    public long getId() { return id; }
    public LocalDate getDate() { return date; }
    public TransactionType getType() { return type; }
    public TransactionCategory getCategory() { return category; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return "ID=" + id + ", date=" + date + ", type=" + type + ", category=" + category
                + ", amount=" + amount + ", description=" + description;
    }
}
