package bankapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Transaction {
    private final String id;
    private final String accountId;
    private final TransactionType type;
    private final BigDecimal amount;
    private final LocalDateTime timestamp;
    private final String description;
    private final String relatedAccountId;

    public Transaction(
            String id,
            String accountId,
            TransactionType type,
            BigDecimal amount,
            LocalDateTime timestamp,
            String description,
            String relatedAccountId
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.accountId = Objects.requireNonNull(accountId, "accountId must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
        this.description = description;
        this.relatedAccountId = relatedAccountId;
    }

    public String getId() {
        return id;
    }

    public String getAccountId() {
        return accountId;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public String getRelatedAccountId() {
        return relatedAccountId;
    }
}
