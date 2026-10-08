package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.CreateTransactionRequest;
import org.example.dto.TransactionSummary;
import org.example.dto.TransactionResponse;
import org.example.model.Transaction;
import org.example.model.TransactionCategory;
import org.example.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;

/** HTTP endpoints for managing transactions. */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<TransactionResponse> getAllTransactions() {
        return transactionService.getAllTransactions().stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public TransactionResponse getTransaction(@PathVariable long id) {
        return TransactionResponse.from(transactionService.getTransactionById(id));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
        Transaction saved = transactionService.createTransaction(request);
        return ResponseEntity.created(URI.create("/api/transactions/" + saved.getId()))
                .body(TransactionResponse.from(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public TransactionSummary getSummary() {
        return transactionService.getSummary();
    }

    @GetMapping("/expenses/category")
    public Map<TransactionCategory, BigDecimal> getExpenseByCategory() {
        return transactionService.getExpenseByCategory();
    }
}
