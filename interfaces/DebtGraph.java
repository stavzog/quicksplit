import java.util.HashMap;

/**
 * A custom Directed Weighted Graph interface for debt simplification.
 */
public interface DebtGraph {
    /** * Adds an edge representing debt.
     * @param from the person who owes
     * @param to the person owed
     * @param weight the amount owed
     */
    void addEdge(int from, int to, double weight);

    /** * Simplifies the graph to minimize the total number of transactions.
     */
    void simplifyDebts();

    /** * @return a representation of the simplified debts.
     */
    Map<Integer, HashMap<Integer, Double>> getAdjacencyList();
}
