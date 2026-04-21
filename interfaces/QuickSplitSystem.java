import java.util.List;
import java.util.Map;

/**
 * The central management interface for the QuickSplit application.
 */
public interface QuickSplitSystem {
    /** * Logs a new expense to the system's primary data storage.
     */
    void logExpense(Transaction t);

    /** * Generates a temporary DebtGraph to calculate the most efficient settle-up.
     * @return a list of human-readable instructions (e.g., "Ali owes Gavin $10").
     */
    List<String> calculateSettleUp();

    /** * Synchronizes the room data with the cloud storage service.
     * Required for the A+ grading contract.
     * @param roomId the mnemonic human-readable ID (e.g., "happy-blue-mountain").
     */
    void sync(String roomId);

    /** * Analyzes the transaction log for spending trends.
     * @return a map of analysis results (e.g., "Total Spent" -> 1500.0).
     */
    Map<String, Double> getAnalytics();
}
