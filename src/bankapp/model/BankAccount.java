package bankapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class BankAccount {
    private final String id;
    private final String accountHolderName;
    private BigDecimal balance;
    private final LocalDateTime createdAt;

    public BankAccount(String id, String accountHolderName, BigDecimal balance, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.accountHolderName = Objects.requireNonNull(accountHolderName, "accountHolderName must not be null");
        this.balance = Objects.requireNonNull(balance, "balance must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public String getId() {
        return id;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void increaseBalance(BigDecimal amount) {
        balance = balance.add(amount);
    }

    public void decreaseBalance(BigDecimal amount) {
        balance = balance.subtract(amount);
    }
}
