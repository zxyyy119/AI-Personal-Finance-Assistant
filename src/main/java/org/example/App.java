package org.example;

import org.example.model.TransactionCategory;
import org.example.model.TransactionType;
import org.example.legacy.LegacyTransactionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/** Legacy V0.2 console entry point; V0.3 starts from Application. */
public class App {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final LegacyTransactionService transactionService = new LegacyTransactionService();

    public static void main(String[] args) {
        System.out.println("Welcome to AI Personal Finance Assistant - V0.2 (legacy console)");
        boolean running = true;
        while (running) {
            printMenu();
            String choice = SCANNER.nextLine().trim();
            switch (choice) {
                case "1" -> addTransaction(TransactionType.INCOME);
                case "2" -> addTransaction(TransactionType.EXPENSE);
                case "3" -> transactionService.showTransactions();
                case "4" -> removeTransaction();
                case "5" -> showSummary();
                case "6" -> transactionService.showExpenseByCategory();
                case "0" -> running = false;
                default -> System.out.println("Invalid menu option. Please enter a number from 0 to 6.");
            }
        }
        System.out.println("Goodbye!");
    }

    private static void printMenu() {
        System.out.println("\n========== Menu ==========");
        System.out.println("1. Add income");
        System.out.println("2. Add expense");
        System.out.println("3. View all transactions");
        System.out.println("4. Delete a transaction");
        System.out.println("5. View income, expense, and balance");
        System.out.println("6. View expenses by category");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    private static void addTransaction(TransactionType type) {
        LocalDate date = readDate();
        TransactionCategory category = readCategory();
        BigDecimal amount = readAmount();
        System.out.print("Description: ");
        String description = SCANNER.nextLine().trim();
        transactionService.addTransaction(date, type, category, amount, description);
        System.out.println(type + " transaction added successfully.");
    }

    private static LocalDate readDate() {
        while (true) {
            System.out.print("Date (YYYY-MM-DD, or press Enter for today): ");
            String input = SCANNER.nextLine().trim();
            if (input.isEmpty()) return LocalDate.now();
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException exception) {
                System.out.println("Invalid date. Example: 2026-10-02");
            }
        }
    }

    private static TransactionCategory readCategory() {
        TransactionCategory[] categories = TransactionCategory.values();
        System.out.println("Categories:");
        for (int index = 0; index < categories.length; index++) {
            System.out.println((index + 1) + ". " + categories[index]);
        }
        while (true) {
            System.out.print("Choose a category number: ");
            try {
                int categoryNumber = Integer.parseInt(SCANNER.nextLine().trim());
                if (categoryNumber >= 1 && categoryNumber <= categories.length) return categories[categoryNumber - 1];
            } catch (NumberFormatException exception) {
                // The message below covers non-numeric input too.
            }
            System.out.println("Invalid category number. Please try again.");
        }
    }

    private static BigDecimal readAmount() {
        while (true) {
            System.out.print("Amount: ");
            try {
                BigDecimal amount = new BigDecimal(SCANNER.nextLine().trim());
                if (amount.compareTo(BigDecimal.ZERO) > 0) return amount;
            } catch (NumberFormatException exception) {
                // The message below covers invalid numeric input too.
            }
            System.out.println("Amount must be a positive number, for example: 25.50");
        }
    }

    private static void removeTransaction() {
        System.out.print("Enter the transaction ID to delete: ");
        try {
            long id = Long.parseLong(SCANNER.nextLine().trim());
            if (transactionService.removeTransaction(id)) System.out.println("Transaction deleted successfully.");
            else System.out.println("No transaction found with ID " + id + ".");
        } catch (NumberFormatException exception) {
            System.out.println("Invalid ID. Please enter a whole number.");
        }
    }

    private static void showSummary() {
        System.out.println("\n========== Financial Summary ==========");
        System.out.println("Total income : " + transactionService.calculateTotalIncome());
        System.out.println("Total expense: " + transactionService.calculateTotalExpense());
        System.out.println("Balance      : " + transactionService.calculateBalance());
    }
}
