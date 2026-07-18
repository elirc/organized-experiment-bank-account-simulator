package bankapp.service;

import bankapp.model.BankAccount;
import bankapp.model.Transaction;
import bankapp.model.TransactionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BankService {
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final Map<String, BankAccount> accountsById = new LinkedHashMap<>();
    private final List<Transaction> transactions = new ArrayList<>();

    public BankService(List<BankAccount> existingAccounts, List<Transaction> existingTransactions) {
        for (BankAccount account : existingAccounts) {
            accountsById.put(account.getId(), account);
        }
        transactions.addAll(existingTransactions);
    }

    public BankAccount createAccount(String accountHolderName) {
        String cleanName = accountHolderName == null ? "" : accountHolderName.trim();
        if (cleanName.isEmpty()) {
            throw new IllegalArgumentException("Account holder name cannot be empty.");
        }

        BankAccount account = new BankAccount(
                generateId("ACC"),
                cleanName,
                ZERO,
                LocalDateTime.now()
        );
        accountsById.put(account.getId(), account);
        return account;
    }

    public List<BankAccount> getAllAccounts() {
        List<BankAccount> accounts = new ArrayList<>(accountsById.values());
        accounts.sort(Comparator.comparing(BankAccount::getCreatedAt));
        return accounts;
    }

    public BankAccount getAccountById(String accountId) {
        return accountsById.get(accountId);
    }

    public Transaction deposit(String accountId, BigDecimal amount, String description) {
        BankAccount account = requireAccount(accountId);
        BigDecimal cleanAmount = validateAmount(amount, "Deposit");

        account.increaseBalance(cleanAmount);
        Transaction transaction = createTransaction(
                account.getId(),
                TransactionType.DEPOSIT,
                cleanAmount,
                description,
                null
        );
        transactions.add(transaction);
        return transaction;
    }

    public Transaction withdraw(String accountId, BigDecimal amount, String description) {
        BankAccount account = requireAccount(accountId);
        BigDecimal cleanAmount = validateAmount(amount, "Withdrawal");

        if (account.getBalance().compareTo(cleanAmount) < 0) {
            throw new IllegalStateException("Insufficient balance for this withdrawal.");
        }

        account.decreaseBalance(cleanAmount);
        Transaction transaction = createTransaction(
                account.getId(),
                TransactionType.WITHDRAWAL,
                cleanAmount,
                description,
                null
        );
        transactions.add(transaction);
        return transaction;
    }

    public List<Transaction> transfer(String fromAccountId, String toAccountId, BigDecimal amount, String description) {
        if (fromAccountId == null || toAccountId == null) {
            throw new IllegalArgumentException("Source and destination account ids are required.");
        }
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("You cannot transfer money to the same account.");
        }

        BankAccount sourceAccount = requireAccount(fromAccountId);
        BankAccount destinationAccount = requireAccount(toAccountId);
        BigDecimal cleanAmount = validateAmount(amount, "Transfer");

        if (sourceAccount.getBalance().compareTo(cleanAmount) < 0) {
            throw new IllegalStateException("Insufficient balance for this transfer.");
        }

        sourceAccount.decreaseBalance(cleanAmount);
        destinationAccount.increaseBalance(cleanAmount);

        Transaction transferOut = createTransaction(
                sourceAccount.getId(),
                TransactionType.TRANSFER_OUT,
                cleanAmount,
                description,
                destinationAccount.getId()
        );
        Transaction transferIn = createTransaction(
                destinationAccount.getId(),
                TransactionType.TRANSFER_IN,
                cleanAmount,
                description,
                sourceAccount.getId()
        );

        transactions.add(transferOut);
        transactions.add(transferIn);

        List<Transaction> result = new ArrayList<>();
        result.add(transferOut);
        result.add(transferIn);
        return result;
    }

    public List<Transaction> getTransactionsForAccount(String accountId) {
        requireAccount(accountId);

        List<Transaction> accountTransactions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getAccountId().equals(accountId)) {
                accountTransactions.add(transaction);
            }
        }

        accountTransactions.sort(Comparator
                .comparing(Transaction::getTimestamp)
                .thenComparing(Transaction::getId));
        return accountTransactions;
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> copy = new ArrayList<>(transactions);
        copy.sort(Comparator
                .comparing(Transaction::getTimestamp)
                .thenComparing(Transaction::getId));
        return copy;
    }

    public Collection<BankAccount> getAccountsForSaving() {
        return new ArrayList<>(accountsById.values());
    }

    private BankAccount requireAccount(String accountId) {
        BankAccount account = accountsById.get(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }
        return account;
    }

    private BigDecimal validateAmount(BigDecimal amount, String actionName) {
        if (amount == null) {
            throw new IllegalArgumentException(actionName + " amount is required.");
        }

        BigDecimal cleanAmount = amount.setScale(2, RoundingMode.HALF_UP);
        if (cleanAmount.compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException(actionName + " amount must be greater than zero.");
        }
        return cleanAmount;
    }

    private Transaction createTransaction(
            String accountId,
            TransactionType type,
            BigDecimal amount,
            String description,
            String relatedAccountId
    ) {
        String cleanDescription = description == null ? null : description.trim();
        if (cleanDescription != null && cleanDescription.isEmpty()) {
            cleanDescription = null;
        }

        return new Transaction(
                generateId("TXN"),
                accountId,
                type,
                amount,
                LocalDateTime.now(),
                cleanDescription,
                relatedAccountId
        );
    }

    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}
