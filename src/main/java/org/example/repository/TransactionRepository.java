package org.example.repository;

import org.example.model.Transaction;
import org.example.model.TransactionCategory;
import org.example.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Spring Data creates the database operations for this interface. */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByType(TransactionType type);

    List<Transaction> findByTypeAndCategory(TransactionType type, TransactionCategory category);
}
