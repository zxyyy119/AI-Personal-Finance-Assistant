package org.example.integration;

import org.example.dto.CreateTransactionRequest;
import org.example.dto.TransactionResponse;
import org.example.dto.TransactionSummary;
import org.example.model.Transaction;
import org.example.model.TransactionCategory;
import org.example.model.TransactionType;
import org.example.repository.TransactionRepository;
import org.example.service.CsvImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Runs a complete API and migration scenario against a disposable real MySQL container. */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MySqlPersistenceIT {
    private static final Path CSV_FIXTURE = Path.of("target/test-data/transactions-it.csv").toAbsolutePath();

    @Container
    private static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withDatabaseName("finance_it");

    static {
        try {
            Files.createDirectories(CSV_FIXTURE.getParent());
            Files.writeString(CSV_FIXTURE,
                    "id,date,type,category,amount,description\n"
                            + "9001,2026-10-01,EXPENSE,SHOPPING,7.50,legacy fixture\n");
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    @Autowired
    private TestRestTemplate http;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CsvImportService csvImportService;

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", MYSQL::getJdbcUrl);
        properties.add("spring.datasource.username", MYSQL::getUsername);
        properties.add("spring.datasource.password", MYSQL::getPassword);
        properties.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
        properties.add("app.csv.path", () -> CSV_FIXTURE.toString());
    }

    @Test
    void flywayCsvImportRepositoryAndRestApiWorkWithMySql() {
        // Spring context startup proves the V1 Flyway migration created a valid schema.
        assertEquals(1, transactionRepository.count());
        Transaction imported = transactionRepository.findAll().getFirst();
        assertNotEquals(9001L, imported.getId()); // legacy CSV ID is not the MySQL primary key
        assertEquals(0, csvImportService.importIfDatabaseIsEmpty());
        assertEquals(1, transactionRepository.count()); // startup re-entry does not duplicate CSV rows

        LocalDate date = LocalDate.of(2026, 10, 8);
        ResponseEntity<TransactionResponse> income = http.postForEntity("/api/transactions",
                new CreateTransactionRequest(TransactionType.INCOME, TransactionCategory.OTHER,
                        new BigDecimal("100.00"), date, "integration income"), TransactionResponse.class);
        assertEquals(HttpStatus.CREATED, income.getStatusCode());
        assertNotNull(income.getBody());
        Long incomeId = income.getBody().id();
        assertNotEquals(9001L, incomeId);

        ResponseEntity<TransactionResponse> expense = http.postForEntity("/api/transactions",
                new CreateTransactionRequest(TransactionType.EXPENSE, TransactionCategory.FOOD,
                        new BigDecimal("2.50"), date, "integration expense"), TransactionResponse.class);
        assertEquals(HttpStatus.CREATED, expense.getStatusCode());
        assertNotNull(expense.getBody());
        Long expenseId = expense.getBody().id();

        ResponseEntity<TransactionResponse[]> all = http.getForEntity("/api/transactions", TransactionResponse[].class);
        assertEquals(HttpStatus.OK, all.getStatusCode());
        assertEquals(3, all.getBody().length);
        ResponseEntity<TransactionResponse> one = http.getForEntity(
                "/api/transactions/" + incomeId, TransactionResponse.class);
        assertEquals("integration income", one.getBody().description());

        TransactionSummary summary = http.getForObject("/api/transactions/summary", TransactionSummary.class);
        assertEquals(new BigDecimal("100.00"), summary.totalIncome());
        assertEquals(new BigDecimal("10.00"), summary.totalExpense());
        assertEquals(new BigDecimal("90.00"), summary.balance());

        ResponseEntity<Map<String, BigDecimal>> categoryResponse = http.exchange(
                "/api/transactions/expenses/category", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() { });
        assertEquals(new BigDecimal("2.50"), categoryResponse.getBody().get("FOOD"));
        assertEquals(new BigDecimal("7.50"), categoryResponse.getBody().get("SHOPPING"));

        http.delete("/api/transactions/" + expenseId);
        assertEquals(2, transactionRepository.count());
        TransactionSummary afterDelete = http.getForObject("/api/transactions/summary", TransactionSummary.class);
        assertEquals(new BigDecimal("7.50"), afterDelete.totalExpense());
    }
}
