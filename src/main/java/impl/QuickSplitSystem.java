package impl;

import java.util.*;
import mjson.Json;
import util.CurrencyService;
import util.IDGenerator;

/**
 * QuickSplitSystem is the central coordinator for managing multiple rooms.
 * It provides the logic for switching between rooms and delegates expense logging
 * and settlement calculations to the active Room instance.
 */
public class QuickSplitSystem {

    private final Map<String, Room> rooms = new HashMap<>();
    private Room activeRoom;
    private final CurrencyService currencyService;
    private final String baseCurrency = "USD";

    public QuickSplitSystem() {
        this.currencyService = new CurrencyService(baseCurrency);
    }

    /**
     * Creates a new room with a unique human-readable ID and sets it as the active room.
     * @return The ID of the newly created room.
     */
    public String createRoom() {
        String roomId = IDGenerator.generate();

        // ensure uniqueness
        while (rooms.containsKey(roomId)) {
            roomId = IDGenerator.generate();
        }
        Room newRoom = new Room(roomId);
        rooms.put(roomId, newRoom);
        activeRoom = newRoom;
        return roomId;
    }

    /**
     * Joins an existing room by its ID.
     * @param roomId The ID of the room to join.
     * @return true if the room exists and was joined, false otherwise.
     */
    public boolean joinRoom(String roomId) {
        if (rooms.containsKey(roomId)) {
            activeRoom = rooms.get(roomId);
            return true;
        }
        return false;
    }

    /**
     * Gets the ID of the currently active room.
     * @return The active room ID or null if no room is active.
     */
    public String getActiveRoomId() {
        return activeRoom != null ? activeRoom.getRoomId() : null;
    }

    /**
     * Exports the active room to a JSON string.
     * @return The JSON representation of the room.
     */
    public String exportActiveRoom() {
        if (activeRoom == null) {
            throw new IllegalStateException("No active room to export.");
        }
        return activeRoom.toJson().toString();
    }

    /**
     * Imports a room from a JSON string and sets it as active.
     * @param jsonString The JSON data representing a room.
     */
    public void importRoom(String jsonString) {
        Json json = Json.read(jsonString);
        Room room = Room.fromJson(json);
        rooms.put(room.getRoomId(), room);
        activeRoom = room;
    }

    public void addUser(int userId, String name) {
        if (activeRoom != null) {
            activeRoom.addUser(userId, name);
        }
    }

    public Map<Integer, String> getUsers() {
        return activeRoom != null
            ? activeRoom.getUsers()
            : Collections.emptyMap();
    }

    /**
     * Logs an expense for the active room with automatic conversion to base currency.
     */
    public void logExpense(
        int payerId,
        double amount,
        String currency,
        String description
    ) {
        if (activeRoom == null) {
            throw new IllegalStateException(
                "No active room. Create or join a room first."
            );
        }

        double rate = currencyService.getRate(currency, baseCurrency);
        double convertedAmount = amount * rate;

        Transaction t = new Transaction(
            payerId,
            convertedAmount,
            currency.toUpperCase(),
            amount,
            rate,
            description
        );

        activeRoom.logExpense(t);
        System.out.println("Logged: " + t);
    }

    /**
     * Calculates the settlement plan for the active room using the Greedy Two-Pointer algorithm.
     */
    public List<Settlement> calculateSettleUp(String targetCurrency) {
        if (activeRoom == null || activeRoom.getUsers().isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, String> users = activeRoom.getUsers();
        List<Transaction> transactions = activeRoom.getTransactions();

        // 1. Accumulation Scan
        Map<Integer, Double> netBalances = new HashMap<>();
        for (Integer userId : users.keySet()) {
            netBalances.put(userId, 0.0);
        }

        int totalUsers = users.size();
        for (Transaction t : transactions) {
            double totalAmount = t.getAmount();
            double share = totalAmount / totalUsers;

            netBalances.put(
                t.getPayerId(),
                netBalances.get(t.getPayerId()) + totalAmount
            );

            for (Integer userId : users.keySet()) {
                netBalances.put(userId, netBalances.get(userId) - share);
            }
        }

        // 2. Separate into Debtors and Creditors
        List<UserBalance> debtors = new ArrayList<>();
        List<UserBalance> creditors = new ArrayList<>();

        for (Map.Entry<Integer, Double> entry : netBalances.entrySet()) {
            double bal = entry.getValue();
            if (bal < -0.001) {
                debtors.add(new UserBalance(entry.getKey(), bal));
            } else if (bal > 0.001) {
                creditors.add(new UserBalance(entry.getKey(), bal));
            }
        }

        // 3. Sort extremes
        debtors.sort(Comparator.comparingDouble(u -> u.balance));
        creditors.sort((u1, u2) -> Double.compare(u2.balance, u1.balance));

        // 4. Two-Pointer Matching
        List<Settlement> settlements = new ArrayList<>();
        double rateToTarget = currencyService.getRate(
            baseCurrency,
            targetCurrency
        );

        int d = 0;
        int c = 0;
        while (d < debtors.size() && c < creditors.size()) {
            UserBalance debtor = debtors.get(d);
            UserBalance creditor = creditors.get(c);

            double amountToSettle = Math.min(-debtor.balance, creditor.balance);
            double displayAmount = amountToSettle * rateToTarget;

            settlements.add(
                new Settlement(
                    debtor.id,
                    creditor.id,
                    displayAmount,
                    targetCurrency
                )
            );

            debtor.balance += amountToSettle;
            creditor.balance -= amountToSettle;

            if (Math.abs(debtor.balance) < 0.001) d++;
            if (Math.abs(creditor.balance) < 0.001) c++;
        }

        return settlements;
    }

    public List<Settlement> calculateSettleUp() {
        return calculateSettleUp(baseCurrency);
    }

    private static class UserBalance {

        int id;
        double balance;

        UserBalance(int id, double balance) {
            this.id = id;
            this.balance = balance;
        }
    }
}
