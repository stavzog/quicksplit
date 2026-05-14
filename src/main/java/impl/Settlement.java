package impl;

/**
 * Represents a single settlement transaction calculated by the Settle Up algorithm.
 * Using a data object instead of a String prevents "flimsy" architecture and allows
 * for easier analytics or UI rendering.
 */
public class Settlement {

    private final String debtorId;
    private final String creditorId;
    private final double amount;
    private final String currency;

    public Settlement(
        String debtorId,
        String creditorId,
        double amount,
        String currency
    ) {
        this.debtorId = debtorId;
        this.creditorId = creditorId;
        this.amount = amount;
        this.currency = currency;
    }

    /**
     * Returns the ID of the debtor involved in this settlement.
     */
    public String getDebtorId() {
        return debtorId;
    }

    /**
     * Returns the ID of the creditor involved in this settlement.
     */
    public String getCreditorId() {
        return creditorId;
    }

    /**
     * Returns the amount of this settlement.
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Returns the currency of this settlement.
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Converts the settlement into a human-readable string given a map of user names.
     *
     * @param userNames A map containing user IDs and their corresponding display names.
     * @return A formatted string like "Alice pays Bob 10.50 USD"
     */
    public String toString(java.util.Map<String, String> userNames) {
        String debtorName = userNames.getOrDefault(
            debtorId,
            "User " + debtorId
        );
        String creditorName = userNames.getOrDefault(
            creditorId,
            "User " + creditorId
        );
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
            "User %s pays User %s %.2f %s",
            debtorId,
            creditorId,
            amount,
            currency.toUpperCase()
        );
    }
}
