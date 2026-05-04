package impl;

import interfaces.Transaction;

public class TransactionImpl implements Transaction {
    private final int payerId;
    private final double amount;
    private final String originalCurrency;
    private final double originalAmount;
    private final double exchangeRate;
    private final long timestamp;

    public TransactionImpl(int payerId, double amount, String originalCurrency, 
                           double originalAmount, double exchangeRate, long timestamp) {
        this.payerId = payerId;
        this.amount = amount;
        this.originalCurrency = originalCurrency;
        this.originalAmount = originalAmount;
        this.exchangeRate = exchangeRate;
        this.timestamp = timestamp;
    }

    @Override
    public int getPayerId() {
        return payerId;
    }

    @Override
    public double getAmount() {
        return amount;
    }

    @Override
    public String getOriginalCurrency() {
        return originalCurrency;
    }

    @Override
    public double getOriginalAmount() {
        return originalAmount;
    }

    @Override
    public double getExchangeRate() {
        return exchangeRate;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("Payer: %d, Amount: %.2f USD (Original: %.2f %s, Rate: %.4f)", 
                payerId, amount, originalAmount, originalCurrency, exchangeRate);
    }
}
