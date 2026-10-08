package org.example.repository;

import org.example.model.Transaction;
import org.example.model.TransactionCategory;
import org.example.model.TransactionType;
import org.example.repository.projection.CategoryExpenseProjection;
import org.example.repository.projection.TransactionSummaryProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Spring Data creates the database operations for this interface. */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("""
            select sum(case when t.type = :incomeType then t.amount else 0 end) as totalIncome,
                   sum(case when t.type = :expenseType then t.amount else 0 end) as totalExpense,
                   sum(case when t.type = :incomeType then t.amount else 0 end)
                     - sum(case when t.type = :expenseType then t.amount else 0 end) as balance
            from Transaction t
            """)
    TransactionSummaryProjection calculateSummary(
            @Param("incomeType") TransactionType incomeType,
            @Param("expenseType") TransactionType expenseType);

    @Query("""
            select t.category as category, sum(t.amount) as total
            from Transaction t
            where t.type = :type
            group by t.category
            """)
    List<CategoryExpenseProjection> sumByCategory(@Param("type") TransactionType type);
}
