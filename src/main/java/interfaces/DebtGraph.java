package interfaces;
import java.util.Map;
import java.util.HashMap;

/**
 * A custom Directed Weighted Graph interface for debt simplification.
 *
 * A computational projection of your transaction log
 *
 * (Lazy Evaluation)
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

    /** * Adds a new node to the graph.
     * @param weight the weight of the node
     */
    void addNode(int weight);

    /** * Removes a node from the graph.
     * @param node the node to remove
     */
    void removeNode(int node);

    /** * @return a representation of the simplified debts.
     */
    Map<Integer, HashMap<Integer, Double>> getAdjacencyList();
}
