package impl;

import java.util.*;
import util.CurrencyService;

/**
 * QuickSplitSystem is the central coordinator for managing users and transactions.
 * It provides the logic for logging expenses and calculating the optimized settlement plan
 * without the need for an external graph data structure, adhering to simplified architecture.
 */
public class QuickSplitSystem {

    // The transaction log acts as the source of truth for the room.
    private final List<Transaction> transactions = new ArrayList<>();
    private final Map<Integer, String> users = new HashMap<>();
    private final CurrencyService currencyService;
    private final String baseCurrency = "USD";

    public QuickSplitSystem() {
        this.currencyService = new CurrencyService(baseCurrency);
    }

    public void addUser(int userId, String name) {
        users.put(userId, name);
    }

    public void logExpense(Transaction t) {
        transactions.add(t);
    }

    /**
     * Helper method for the CLI to log an expense with automatic conversion to the base currency.
     */
    public void logExpense(
        int payerId,
        double amount,
        String currency,
        String description
    ) {
        double rate = currencyService.getRate(currency, baseCurrency);
        double convertedAmount = amount * rate;

        Transaction t = new Transaction(
            payerId,
            convertedAmount,
            currency.toUpperCase(),
            amount,
            rate,
            description
        );

        logExpense(t);
        System.out.println("Logged: " + t);
    }

    /**
     * Internal helper to represent a person's net balance during settlement.
     */
    private static class UserBalance {

        int id;
        double balance;

        UserBalance(int id, double balance) {
            this.id = id;
            this.balance = balance;
        }
    }

    /**
     * Calculates the minimum number of transactions needed to settle all debts.
     * This implements the Greedy Debt Simplification (Two-Pointer Method).
     *
     * @param targetCurrency The currency in which to display the settlement amounts.
     * @return A list of strings describing who pays whom and how much.
     */
    public List<String> calculateSettleUp(String targetCurrency) {
        if (users.isEmpty()) {
            return Collections.singletonList("No users in the room.");
        }

        // calculate the net balance for every person.
        Map<Integer, Double> netBalances = new HashMap<>();
        for (Integer userId : users.keySet()) {
            netBalances.put(userId, 0.0);
        }

        int totalUsers = users.size();
        for (Transaction t : transactions) {
            double totalAmount = t.getAmount();
            double share = totalAmount / totalUsers;

            netBalances.put(
                t.getPayerId(),
                netBalances.get(t.getPayerId()) + totalAmount
            );

            for (Integer userId : users.keySet()) {
                netBalances.put(userId, netBalances.get(userId) - share);
            }
        }

        // separate users into those who owe money and those who are owed.
        List<UserBalance> debtors = new ArrayList<>();
        List<UserBalance> creditors = new ArrayList<>();

        for (Map.Entry<Integer, Double> entry : netBalances.entrySet()) {
            double bal = entry.getValue();
            // account for floating point inaccuracies
            if (bal < -0.001) {
                debtors.add(new UserBalance(entry.getKey(), bal));
            } else if (bal > 0.001) {
                creditors.add(new UserBalance(entry.getKey(), bal));
            }
        }

        // simplification algorithm
        // match debtors with creditors until balances are zeroed.
        debtors.sort(Comparator.comparingDouble(u -> u.balance));
        creditors.sort((u1, u2) -> Double.compare(u2.balance, u1.balance));

        List<String> settlements = new ArrayList<>();
        double rateToTarget = currencyService.getRate(
            baseCurrency,
            targetCurrency
        );

        int d = 0; // debtor pointer
        int c = 0; // creditor pointer

        while (d < debtors.size() && c < creditors.size()) {
            UserBalance debtor = debtors.get(d);
            UserBalance creditor = creditors.get(c);

            // the amount to settle is the minimum of the debt and the credit available.
            double amountToSettle = Math.min(-debtor.balance, creditor.balance);

            String debtorName = users.get(debtor.id);
            String creditorName = users.get(creditor.id);
            double displayAmount = amountToSettle * rateToTarget;

            settlements.add(
                String.format(
                    "%s pays %s %.2f %s",
                    debtorName,
                    creditorName,
                    displayAmount,
                    targetCurrency.toUpperCase()
                )
            );

            // update balances
            debtor.balance += amountToSettle;
            creditor.balance -= amountToSettle;

            // check for settled balances
            if (Math.abs(debtor.balance) < 0.001) d++;
            if (Math.abs(creditor.balance) < 0.001) c++;
        }

        if (settlements.isEmpty()) {
            settlements.add("All settled up! No transactions needed.");
        }

        return settlements;
    }

    public List<String> calculateSettleUp() {
        return calculateSettleUp(baseCurrency);
    }

    public void sync(String roomId) {
        // Future implementation for JSONBin.io cloud synchronization.
    }
}
