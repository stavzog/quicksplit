package impl;

/**
 * Represents a single settlement transaction calculated by the Settle Up algorithm.
 * Using a data object instead of a String prevents "flimsy" architecture and allows
 * for easier analytics or UI rendering.
 */
public class Settlement {

    private final int debtorId;
    private final int creditorId;
    private final double amount;
    private final String currency;

    public Settlement(int debtorId, int creditorId, double amount, String currency) {
        this.debtorId = debtorId;
        this.creditorId = creditorId;
        this.amount = amount;
        this.currency = currency;
    }

    public int getDebtorId() {
        return debtorId;
    }

    public int getCreditorId() {
        return creditorId;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    /**
     * Converts the settlement into a human-readable string given a map of user names.
     *
     * @param userNames A map containing user IDs and their corresponding display names.
     * @return A formatted string like "Alice pays Bob 10.50 USD"
     */
    public String toString(java.util.Map<Integer, String> userNames) {
        String debtorName = userNames.getOrDefault(debtorId, "User " + debtorId);
        String creditorName = userNames.getOrDefault(creditorId, "User " + creditorId);
        return String.format(
            "%s pays %s %.2f %s",
            debtorName,
            creditorName,
            amount,
            currency.toUpperCase()
        );
    }

    @Override
    public String toString() {
        return String.format(
            "User %d pays User %d %.2f %s",
            debtorId,
            creditorId,
            amount,
            currency.toUpperCase()
        );
    }
}
