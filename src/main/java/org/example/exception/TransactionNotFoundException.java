package org.example.exception;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(long id) {
        super("Transaction with ID " + id + " was not found.");
    }
}
