package org.example.service;

import org.example.model.Transaction;
import org.example.repository.TransactionRepository;
import org.example.storage.CsvTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CsvImportServiceTest {
    @TempDir
    Path tempDirectory;

    @Test
    void importsLegacyRowsWithoutReusingTheirIds() throws Exception {
        Path csvFile = tempDirectory.resolve("transactions.csv");
        java.nio.file.Files.writeString(csvFile,
                "id,date,type,category,amount,description\n"
                        + "987,2026-10-01,INCOME,OTHER,100.00,legacy\n");
        TransactionRepository database = mock(TransactionRepository.class);
        when(database.count()).thenReturn(0L);
        when(database.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        CsvImportService importer = new CsvImportService(database,
                new CsvTransactionRepository(csvFile.toString()));

        assertEquals(1, importer.importIfDatabaseIsEmpty());

        ArgumentCaptor<Iterable<Transaction>> rows = ArgumentCaptor.forClass(Iterable.class);
        verify(database).saveAll(rows.capture());
        Transaction imported = rows.getValue().iterator().next();
        assertNull(imported.getId());
        assertEquals("100.00", imported.getAmount().toPlainString());
    }

    @Test
    void skipsCsvImportWhenDatabaseAlreadyContainsTransactions() {
        TransactionRepository database = mock(TransactionRepository.class);
        when(database.count()).thenReturn(1L);
        CsvImportService importer = new CsvImportService(database,
                new CsvTransactionRepository(tempDirectory.resolve("transactions.csv").toString()));

        assertEquals(0, importer.importIfDatabaseIsEmpty());
        verify(database, never()).saveAll(anyList());
    }
}
