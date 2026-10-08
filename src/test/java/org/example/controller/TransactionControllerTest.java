package org.example.controller;

import org.example.dto.CreateTransactionRequest;
import org.example.dto.TransactionSummary;
import org.example.dto.TransactionResponse;
import org.example.exception.GlobalExceptionHandler;
import org.example.exception.TransactionNotFoundException;
import org.example.model.Transaction;
import org.example.model.TransactionCategory;
import org.example.model.TransactionType;
import org.example.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@Import(GlobalExceptionHandler.class)
class TransactionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void getAllReturnsJsonTransactions() throws Exception {
        when(transactionService.getAllTransactions()).thenReturn(List.of(transaction(1L,
                TransactionType.INCOME, TransactionCategory.OTHER, "5000.00")));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(5000.00))
                .andExpect(jsonPath("$[0].type").value("INCOME"));
    }

    @Test
    void getByIdReturnsOneTransaction() throws Exception {
        when(transactionService.getTransactionById(7L)).thenReturn(transaction(7L,
                TransactionType.EXPENSE, TransactionCategory.FOOD, "23.50"));

        mockMvc.perform(get("/api/transactions/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.category").value("FOOD"));
    }

    @Test
    void postCreatesTransactionAndReturns201() throws Exception {
        when(transactionService.createTransaction(any(CreateTransactionRequest.class)))
                .thenReturn(transaction(9L, TransactionType.INCOME, TransactionCategory.OTHER, "5000.00"));

        mockMvc.perform(post("/api/transactions")
                        .contentType("application/json")
                        .content("""
                                {"type":"INCOME","category":"OTHER","amount":5000.00,
                                 "transactionDate":"2026-10-01","description":"Salary"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/transactions/9"))
                .andExpect(jsonPath("$.id").value(9));
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/transactions/9"))
                .andExpect(status().isNoContent());
        verify(transactionService).deleteTransaction(9L);
    }

    @Test
    void summaryReturnsTotals() throws Exception {
        when(transactionService.getSummary()).thenReturn(new TransactionSummary(
                new BigDecimal("5000.00"), new BigDecimal("38.50"), new BigDecimal("4961.50")));

        mockMvc.perform(get("/api/transactions/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(5000.00))
                .andExpect(jsonPath("$.totalExpense").value(38.50))
                .andExpect(jsonPath("$.balance").value(4961.50));
    }

    @Test
    void categorySummaryReturnsJsonMap() throws Exception {
        when(transactionService.getExpenseByCategory()).thenReturn(Map.of(TransactionCategory.FOOD,
                new BigDecimal("23.50")));

        mockMvc.perform(get("/api/transactions/expenses/category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.FOOD").value(23.50));
    }

    @Test
    void nonPositiveAmountReturns400() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType("application/json")
                        .content("""
                                {"type":"EXPENSE","category":"FOOD","amount":0,
                                 "transactionDate":"2026-10-02","description":"Lunch"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void unknownTransactionReturns404() throws Exception {
        when(transactionService.getTransactionById(88L)).thenThrow(new TransactionNotFoundException(88L));

        mockMvc.perform(get("/api/transactions/88"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void invalidEnumReturns400() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType("application/json")
                        .content("""
                                {"type":"PAYMENT","category":"FOOD","amount":12.00,
                                 "transactionDate":"2026-10-02","description":"Lunch"}
                                """))
                .andExpect(status().isBadRequest());
    }

    private Transaction transaction(long id, TransactionType type, TransactionCategory category, String amount) {
        return new Transaction(id, LocalDate.parse("2026-10-01"), type, category,
                new BigDecimal(amount), "test data");
    }
}
