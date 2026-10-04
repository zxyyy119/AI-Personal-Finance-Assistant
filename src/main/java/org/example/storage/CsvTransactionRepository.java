package org.example.storage;

import org.example.model.Transaction;
import org.example.model.TransactionCategory;
import org.example.model.TransactionType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Reads and writes transactions in a small, human-readable CSV file. */
public class CsvTransactionRepository {
    private static final String DEFAULT_FILE_PATH = "data/transactions.csv";
    private static final String HEADER = "id,date,type,category,amount,description";

    private final File file;

    public CsvTransactionRepository() {
        this(DEFAULT_FILE_PATH);
    }

    public CsvTransactionRepository(String filePath) {
        this.file = new File(filePath);
    }

    public List<Transaction> loadTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        if (!file.exists()) {
            return transactions;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (firstLine && line.equals(HEADER)) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;
                readLineSafely(line, transactions);
            }
        } catch (IOException exception) {
            System.out.println("Could not load transactions: " + exception.getMessage());
        }
        return transactions;
    }

    public void saveTransactions(List<Transaction> transactions) {
        File parentDirectory = file.getParentFile();
        if (parentDirectory != null && !parentDirectory.exists() && !parentDirectory.mkdirs()) {
            System.out.println("Could not create data directory: " + parentDirectory.getPath());
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(HEADER);
            writer.newLine();
            for (Transaction transaction : transactions) {
                writer.write(toCsvLine(transaction));
                writer.newLine();
            }
        } catch (IOException exception) {
            System.out.println("Could not save transactions: " + exception.getMessage());
        }
    }

    private void readLineSafely(String line, List<Transaction> transactions) {
        try {
            List<String> fields = parseCsvLine(line);
            if (fields.size() != 6) {
                throw new IllegalArgumentException("expected 6 fields");
            }

            long id = Long.parseLong(fields.get(0));
            LocalDate date = LocalDate.parse(fields.get(1));
            TransactionType type = TransactionType.valueOf(fields.get(2));
            TransactionCategory category = TransactionCategory.valueOf(fields.get(3));
            BigDecimal amount = new BigDecimal(fields.get(4));
            if (id <= 0 || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("id and amount must be positive");
            }
            transactions.add(new Transaction(id, date, type, category, amount, fields.get(5)));
        } catch (RuntimeException exception) {
            System.out.println("Skipped invalid CSV row: " + line);
        }
    }

    private String toCsvLine(Transaction transaction) {
        return transaction.getId() + ","
                + transaction.getDate() + ","
                + transaction.getType() + ","
                + transaction.getCategory() + ","
                + transaction.getAmount() + ","
                + escapeCsv(transaction.getDescription());
    }

    private String escapeCsv(String value) {
        String escapedValue = value.replace("\"", "\"\"");
        if (escapedValue.contains(",") || escapedValue.contains("\"")
                || escapedValue.contains("\n") || escapedValue.contains("\r")) {
            return "\"" + escapedValue + "\"";
        }
        return escapedValue;
    }

    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean insideQuotes = false;

        for (int index = 0; index < line.length(); index++) {
            char currentCharacter = line.charAt(index);
            if (currentCharacter == '"') {
                if (insideQuotes && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    field.append('"');
                    index++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (currentCharacter == ',' && !insideQuotes) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(currentCharacter);
            }
        }

        if (insideQuotes) {
            throw new IllegalArgumentException("unclosed CSV quote");
        }
        fields.add(field.toString());
        return fields;
    }
}
