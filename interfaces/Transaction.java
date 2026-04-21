/**
 * Interface representing a single expense transaction.
 * Part of the QuickSplit data structure proposal.
 */
public interface Transaction {
    /** @return the ID of the person who paid */
    int getPayerId();

    /** @return the total amount of the transaction */
    double getAmount();

    /** @return the epoch timestamp when the transaction occurred */
    long getTimestamp();
}
