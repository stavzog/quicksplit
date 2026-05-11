package impl;

import java.util.*;
import mjson.Json;

/**
 * Encapsulates all data related to a specific expense-sharing room.
 * This includes the room's unique identifier, the users participating in the room,
 * and the history of transactions.
 */
public class Room {

    private final String roomId;
    private final Map<Integer, String> users = new HashMap<>();
    private final List<Transaction> transactions = new ArrayList<>();

    public Room(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    /**
     * Adds a user to this specific room.
     * @param userId Unique identifier for the user.
     * @param name Display name of the user.
     */
    public void addUser(int userId, String name) {
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
    public Map<Integer, String> getUsers() {
        return Collections.unmodifiableMap(users);
    }

    /**
     * Returns an unmodifiable view of the transaction history for this room.
     */
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
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

    public Json toJson() {
        Json usersJson = Json.object();
        for (Map.Entry<Integer, String> entry : users.entrySet()) {
            usersJson.set(entry.getKey().toString(), entry.getValue());
        }

        Json transactionsJson = Json.array();
        for (Transaction t : transactions) {
            transactionsJson.add(t.toJson());
        }

        return Json.object()
            .set("roomId", roomId)
            .set("users", usersJson)
            .set("transactions", transactionsJson);
    }

    public static Room fromJson(Json json) {
        Room room = new Room(json.at("roomId").asString());

        Json usersJson = json.at("users");
        for (Map.Entry<String, Json> entry : usersJson.asJsonMap().entrySet()) {
            room.addUser(
                Integer.parseInt(entry.getKey()),
                entry.getValue().asString()
            );
        }

        Json transactionsJson = json.at("transactions");
        for (Json tJson : transactionsJson.asJsonList()) {
            room.logExpense(Transaction.fromJson(tJson));
        }

        return room;
    }
}
