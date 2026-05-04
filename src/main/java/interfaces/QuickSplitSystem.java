package interfaces;

import java.util.List;
import java.util.Map;

/**
 * The central management interface for the QuickSplit application.
 */
public interface QuickSplitSystem {
    /** * Registers a user in the system.
     * @param userId the ID of the user.
     * @param name the name of the user.
     */
    void addUser(int userId, String name);

    /** * Logs a new expense to the system's primary data storage.
     */
    void logExpense(Transaction t);

    /** * Generates a temporary DebtGraph to calculate the most efficient settle-up.
     * @param targetCurrency the currency to display the results in.
     * @return a list of human-readable instructions (e.g., "Ali owes Gavin $10").
     */
    List<String> calculateSettleUp(String targetCurrency);

    /** * Generates a temporary DebtGraph to calculate the most efficient settle-up in the base currency.
     * @return a list of human-readable instructions.
     */
    List<String> calculateSettleUp();

    /** * Synchronizes the room data with the cloud storage service.
     * Required for the A+ grading contract.
     * @param roomId the mnemonic human-readable ID (e.g., "happy-blue-mountain").
     */
    void sync(String roomId);
}
