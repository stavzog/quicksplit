package impl;

import java.util.*;

/**
 * Encapsulates all data related to a specific expense-sharing room.
 * This includes the room's unique identifier, the users participating in the room,
 * and the history of transactions.
 *
 * This class uses String IDs for users to ensure uniqueness across
 * different devices during cloud synchronization.
 */
public class Room {

    private final String roomId;
    private final Map<String, String> users = new HashMap<>();
    private final List<Transaction> transactions = new ArrayList<>();

    public Room(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    /**
     * Adds a user to this specific room.
     * @param userId Unique identifier for the user (UUID string).
     * @param name Display name of the user.
     */
    public void addUser(String userId, String name) {
        users.put(userId, name);
    }

    /**
     * Logs a transaction within this room.
     * @param t The transaction to record.
     */
    public void logExpense(Transaction t) {
        transactions.add(t);
    }

    /**
     * Returns an unmodifiable view of the users in this room.
     */
    public Map<String, String> getUsers() {
        return Collections.unmodifiableMap(users);
    }

    /**
     * Returns an unmodifiable view of the transaction history for this room.
     */
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    /**
     * Merges another Room object's data into this one.
     * It adds any missing users and appends any transactions that do not
     * already exist in this room based on their unique transaction IDs.
     *
     * @param other The room containing data to merge into this one.
     */
    public void merge(Room other) {
        if (!this.roomId.equals(other.getRoomId())) {
            throw new IllegalArgumentException(
                "Cannot merge rooms with different IDs."
            );
        }

        // merge users
        for (Map.Entry<String, String> entry : other.getUsers().entrySet()) {
            this.users.putIfAbsent(entry.getKey(), entry.getValue());
        }

        Set<String> existingTxIds = new HashSet<>();
        for (Transaction t : this.transactions) {
            existingTxIds.add(t.getTransactionId());
        }

        // add only transactions that are not already present
        for (Transaction t : other.getTransactions()) {
            if (!existingTxIds.contains(t.getTransactionId())) {
                this.transactions.add(t);
            }
        }
    }

    @Override
    public String toString() {
        return String.format(
            "Room[ID: %s, Users: %d, Transactions: %d]",
            roomId,
            users.size(),
            transactions.size()
        );
    }
}
