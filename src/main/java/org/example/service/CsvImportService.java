package org.example.service;

import org.example.model.Transaction;
import org.example.repository.TransactionRepository;
import org.example.storage.CsvTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** Imports legacy CSV transactions once, when the new database is empty. */
@Component
public class CsvImportService implements ApplicationRunner {
    private final TransactionRepository transactionRepository;
    private final CsvTransactionRepository csvRepository;

    @Autowired
    public CsvImportService(TransactionRepository transactionRepository) {
        this(transactionRepository, new CsvTransactionRepository());
    }

    CsvImportService(TransactionRepository transactionRepository, CsvTransactionRepository csvRepository) {
        this.transactionRepository = transactionRepository;
        this.csvRepository = csvRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        importIfDatabaseIsEmpty();
    }

    @Transactional
    public int importIfDatabaseIsEmpty() {
        if (transactionRepository.count() > 0) {
            System.out.println("CSV import skipped: the database already contains transactions.");
            return 0;
        }

        List<Transaction> oldTransactions = csvRepository.loadTransactions();
        if (oldTransactions.isEmpty()) {
            System.out.println("CSV import skipped: no saved CSV transactions were found.");
            return 0;
        }

        // CSV IDs belong to the V0.2 file, not to the MySQL identity column.
        // Build fresh entities so Hibernate sees null IDs and lets MySQL assign them.
        List<Transaction> newTransactions = oldTransactions.stream()
                .map(transaction -> new Transaction(transaction.getDate(), transaction.getType(),
                        transaction.getCategory(), transaction.getAmount(), transaction.getDescription()))
                .collect(Collectors.toList());
        transactionRepository.saveAll(newTransactions);
        System.out.println("Imported " + oldTransactions.size() + " transaction(s) from the V0.2 CSV file.");
        return oldTransactions.size();
    }
}
