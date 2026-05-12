package impl;

import java.util.UUID;

public class Transaction {

    private final String transactionId;
    private final String payerId;
    private final double amount;
    private final String originalCurrency;
    private final double originalAmount;
    private final double exchangeRate;
    private final String description;

    /**
     * Constructor for creating a completely new transaction.
     * Automatically generates a unique transaction ID.
     */
    public Transaction(
        String payerId,
        double amount,
        String originalCurrency,
        double originalAmount,
        double exchangeRate,
        String description
    ) {
        this.transactionId = UUID.randomUUID().toString();
        this.payerId = payerId;
        this.amount = amount;
        this.originalCurrency = originalCurrency;
        this.originalAmount = originalAmount;
        this.exchangeRate = exchangeRate;
        this.description = description;
    }

    /**
     * Constructor for loading an existing transaction (e.g., from JSON or Cloud).
     */
    public Transaction(
        String transactionId,
        String payerId,
        double amount,
        String originalCurrency,
        double originalAmount,
        double exchangeRate,
        String description
    ) {
        this.transactionId =
            transactionId != null
                ? transactionId
                : UUID.randomUUID().toString();
        this.payerId = payerId;
        this.amount = amount;
        this.originalCurrency = originalCurrency;
        this.originalAmount = originalAmount;
        this.exchangeRate = exchangeRate;
        this.description = description;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getPayerId() {
        return payerId;
    }

    public double getAmount() {
        return amount;
    }

    public String getOriginalCurrency() {
        return originalCurrency;
    }

    public double getOriginalAmount() {
        return originalAmount;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format(
            "Payer: %s, Description: %s, Amount: %.2f USD (Original: %.2f %s, Rate: %.4f)",
            payerId,
            description,
            amount,
            originalAmount,
            originalCurrency,
            exchangeRate
        );
    }
}
