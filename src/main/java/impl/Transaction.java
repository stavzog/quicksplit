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

    /**
     * Returns the unique identifier of this transaction.
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Returns the ID of the payer involved in this transaction.
     */
    public String getPayerId() {
        return payerId;
    }

    /**
     * Returns the amount of this transaction.
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Returns the original currency of this transaction.
     */
    public String getOriginalCurrency() {
        return originalCurrency;
    }

    /**
     * Returns the original amount of this transaction.
     */
    public double getOriginalAmount() {
        return originalAmount;
    }

    /**
     * Returns the exchange rate of this transaction.
     */
    public double getExchangeRate() {
        return exchangeRate;
    }

    /**
     * Returns the description of this transaction.
     */
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
