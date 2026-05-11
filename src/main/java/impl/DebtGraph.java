package impl;

import java.util.*;

/**
 * A Directed Weighted Graph representing debt relationships between users.
 * Vertices are user IDs, and edges represent a debt (source owes target a specific weight).
 * This class is maintained for structural representation of the settlement plan
 * and potential network analysis (e.g., finding connected components or flow optimization).
 */
public class DebtGraph {

    private static class Edge {
        int target;
        double weight;

        Edge(int target, double weight) {
            this.target = target;
            this.weight = weight;
        }
    }

    // Adjacency list: maps each node (debtor) to a list of its outgoing edges (creditors)
    private final Map<Integer, List<Edge>> adjList = new HashMap<>();

    /**
     * Adds a directed edge representing a debt.
     * @param source the ID of the user who owes money (debtor)
     * @param target the ID of the user who is owed money (creditor)
     * @param weight the amount of money
     */
    public void addEdge(int source, int target, double weight) {
        if (weight <= 0) return;
        adjList.computeIfAbsent(source, k -> new ArrayList<>())
              .add(new Edge(target, weight));
    }

    /**
     * Removes a specific debt relationship.
     */
    public void removeEdge(int source, int target) {
        List<Edge> edges = adjList.get(source);
        if (edges != null) {
            edges.removeIf(edge -> edge.target == target);
        }
    }

    /**
     * Removes all debts associated with a specific user.
     */
    public void removeNode(int node) {
        adjList.remove(node);
        for (List<Edge> edges : adjList.values()) {
            edges.removeIf(edge -> edge.target == node);
        }
    }

    /**
     * Returns the number of people a specific user needs to pay.
     */
    public int getOutDegree(int node) {
        return adjList.getOrDefault(node, Collections.emptyList()).size();
    }

    /**
     * Returns the total volume of money moving out of this node.
     */
    public double getTotalDebt(int node) {
        return adjList.getOrDefault(node, Collections.emptyList())
                .stream()
                .mapToDouble(e -> e.weight)
                .sum();
    }

    /**
     * Returns a set of all user IDs present in the graph.
     */
    public Set<Integer> getAllNodes() {
        Set<Integer> nodes = new HashSet<>(adjList.keySet());
        for (List<Edge> edges : adjList.values()) {
            for (Edge e : edges) {
                nodes.add(e.target);
            }
        }
        return nodes;
    }

    /**
     * Formats the graph as a list of human-readable strings.
     */
    public List<String> toStringList(Map<Integer, String> userNames, String currency) {
        List<String> results = new ArrayList<>();
        for (Map.Entry<Integer, List<Edge>> entry : adjList.entrySet()) {
            String debtorName = userNames.getOrDefault(entry.getKey(), "User " + entry.getKey());
            for (Edge edge : entry.getValue()) {
                String creditorName = userNames.getOrDefault(edge.target, "User " + edge.target);
                results.add(String.format("%s pays %s %.2f %s",
                    debtorName, creditorName, edge.weight, currency.toUpperCase()));
            }
        }
        return results;
    }
}
