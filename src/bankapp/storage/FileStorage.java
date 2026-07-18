package bankapp.storage;

import bankapp.model.BankAccount;
import bankapp.model.Transaction;
import bankapp.model.TransactionType;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileStorage {
    private static final String SEPARATOR = "|";

    private final Path dataDirectory;
    private final Path accountsFile;
    private final Path transactionsFile;

    public FileStorage(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.accountsFile = dataDirectory.resolve("accounts.txt");
        this.transactionsFile = dataDirectory.resolve("transactions.txt");
    }

    public List<BankAccount> loadAccounts() throws IOException {
        List<BankAccount> accounts = new ArrayList<>();
        if (!Files.exists(accountsFile)) {
            return accounts;
        }

        List<String> lines = Files.readAllLines(accountsFile, StandardCharsets.UTF_8);
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }

            List<String> fields = splitLine(line);
            if (fields.size() != 4) {
                throw new IOException("Invalid account record: " + line);
            }

            BankAccount account = new BankAccount(
                    fields.get(0),
                    fields.get(1),
                    new BigDecimal(fields.get(2)),
                    LocalDateTime.parse(fields.get(3))
            );
            accounts.add(account);
        }
        return accounts;
    }

    public List<Transaction> loadTransactions() throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        if (!Files.exists(transactionsFile)) {
            return transactions;
        }

        List<String> lines = Files.readAllLines(transactionsFile, StandardCharsets.UTF_8);
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }

            List<String> fields = splitLine(line);
            if (fields.size() != 7) {
                throw new IOException("Invalid transaction record: " + line);
            }

            Transaction transaction = new Transaction(
                    fields.get(0),
                    fields.get(1),
                    TransactionType.valueOf(fields.get(2)),
                    new BigDecimal(fields.get(3)),
                    LocalDateTime.parse(fields.get(4)),
                    emptyToNull(fields.get(5)),
                    emptyToNull(fields.get(6))
            );
            transactions.add(transaction);
        }
        return transactions;
    }

    public void saveAccounts(Collection<BankAccount> accounts) throws IOException {
        Files.createDirectories(dataDirectory);

        List<String> lines = new ArrayList<>();
        for (BankAccount account : accounts) {
            lines.add(String.join(
                    SEPARATOR,
                    escape(account.getId()),
                    escape(account.getAccountHolderName()),
                    escape(account.getBalance().toPlainString()),
                    escape(account.getCreatedAt().toString())
            ));
        }

        Files.write(accountsFile, lines, StandardCharsets.UTF_8);
    }

    public void saveTransactions(List<Transaction> transactions) throws IOException {
        Files.createDirectories(dataDirectory);

        List<String> lines = new ArrayList<>();
        for (Transaction transaction : transactions) {
            lines.add(String.join(
                    SEPARATOR,
                    escape(transaction.getId()),
                    escape(transaction.getAccountId()),
                    escape(transaction.getType().name()),
                    escape(transaction.getAmount().toPlainString()),
                    escape(transaction.getTimestamp().toString()),
                    escape(nullToEmpty(transaction.getDescription())),
                    escape(nullToEmpty(transaction.getRelatedAccountId()))
            ));
        }

        Files.write(transactionsFile, lines, StandardCharsets.UTF_8);
    }

    private List<String> splitLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean escaping = false;

        for (int i = 0; i < line.length(); i++) {
            char currentChar = line.charAt(i);

            if (escaping) {
                currentField.append(unescapeCharacter(currentChar));
                escaping = false;
            } else if (currentChar == '\\') {
                escaping = true;
            } else if (currentChar == '|') {
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(currentChar);
            }
        }

        if (escaping) {
            currentField.append('\\');
        }

        fields.add(currentField.toString());
        return fields;
    }

    private String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("|", "\\|")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private char unescapeCharacter(char value) {
        if (value == 'n') {
            return '\n';
        }
        if (value == 'r') {
            return '\r';
        }
        return value;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String emptyToNull(String value) {
        return value.isEmpty() ? null : value;
    }
}
