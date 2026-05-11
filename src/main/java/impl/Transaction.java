package impl;

import mjson.Json;

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

    public Json toJson() {
        return Json.object()
            .set("payerId", payerId)
            .set("amount", amount)
            .set("originalCurrency", originalCurrency)
            .set("originalAmount", originalAmount)
            .set("exchangeRate", exchangeRate)
            .set("description", description);
    }

    public static Transaction fromJson(Json json) {
        return new Transaction(
            json.at("payerId").asInteger(),
            json.at("amount").asDouble(),
            json.at("originalCurrency").asString(),
            json.at("originalAmount").asDouble(),
            json.at("exchangeRate").asDouble(),
            json.at("description").asString()
        );
    }

    @Override
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
