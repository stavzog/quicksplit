package impl;

public class Transaction {

    private final int payerId;
    private final double amount;
    private final String originalCurrency;
    private final double originalAmount;
    private final double exchangeRate;
    private final String description;

    public Transaction(
        int payerId,
        double amount,
        String originalCurrency,
        double originalAmount,
        double exchangeRate,
        String description
    ) {
        this.payerId = payerId;
        this.amount = amount;
        this.originalCurrency = originalCurrency;
        this.originalAmount = originalAmount;
        this.exchangeRate = exchangeRate;
        this.description = description;
    }

    public int getPayerId() {
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

    public String toString() {
        return String.format(
            "Payer: %d, Description: %s, Amount: %.2f USD (Original: %.2f %s, Rate: %.4f)",
            payerId,
            description,
            amount,
            originalAmount,
            originalCurrency,
            exchangeRate
        );
    }
}
