package interfaces;

/**
 * Interface representing a single expense transaction.
 * Part of the QuickSplit data structure proposal.
 */
public interface Transaction {
    /** @return the ID of the person who paid */
    int getPayerId();

    /** @return the total amount of the transaction in the base currency */
    double getAmount();

    /** @return the original currency of the transaction (e.g., "EUR") */
    String getOriginalCurrency();

    /** @return the original amount before conversion */
    double getOriginalAmount();

    /** @return the exchange rate applied (original -> base) */
    double getExchangeRate();

    /** @return the epoch timestamp when the transaction occurred */
    long getTimestamp();
}
