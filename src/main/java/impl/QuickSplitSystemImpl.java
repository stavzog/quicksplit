package impl;

import interfaces.QuickSplitSystem;
import interfaces.Transaction;
import java.util.*;
import util.CurrencyService;

// this is the central entity of the application.
public class QuickSplitSystemImpl implements QuickSplitSystem {

    // source of truth
    private final List<Transaction> transactions = new ArrayList<>();
    private final Map<Integer, String> users = new HashMap<>();
    private final CurrencyService currencyService;
    private final String baseCurrency = "USD";

    public QuickSplitSystemImpl() {
        this.currencyService = new CurrencyService(baseCurrency);
    }

    @Override
    public void addUser(int userId, String name) {
        users.put(userId, name);
    }

    @Override
    public void logExpense(Transaction t) {
        // TODO: validate transactions
        // TODO: convert to base currency
        transactions.add(t);
    }

    /**
     * Helper method for the CLI to log an expense with automatic conversion.
     */
    public void logExpense(int payerId, double amount, String currency) {
        double rate = currencyService.getRate(currency, baseCurrency);
        double convertedAmount = amount * rate;

        Transaction t = new TransactionImpl(
            payerId,
            convertedAmount,
            currency.toUpperCase(),
            amount,
            rate,
            System.currentTimeMillis()
        );

        logExpense(t);
        System.out.println("Logged: " + t);
    }

    @Override
    public List<String> calculateSettleUp(String targetCurrency) {
        // dummy implementation
        // TODO: fix
        double rateToTarget = currencyService.getRate(
            baseCurrency,
            targetCurrency
        );

        List<String> results = new ArrayList<>();
        // Example dummy result
        double amountInTarget = 10.0 * rateToTarget;
        results.add(
            String.format(
                "User 1 owes User 2 %.2f %s",
                amountInTarget,
                targetCurrency.toUpperCase()
            )
        );

        return results;
    }

    @Override
    public List<String> calculateSettleUp() {
        return calculateSettleUp(baseCurrency);
    }

    @Override
    public void sync(String roomId) {
        // To be implemented with JSONBin.io
    }
}
