package impl;

import java.util.*;
import util.CurrencyService;
import util.IDGenerator;

/**
 * QuickSplitSystem is the central coordinator for managing multiple rooms.
 * It provides the logic for switching between rooms and delegates expense logging
 * and settlement calculations to the active Room instance.
 *
 * This implementation uses String user IDs (UUIDs) to ensure data integrity
 * when synchronizing across multiple distributed clients.
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

    public Room getActiveRoom() {
        return activeRoom;
    }

    /**
     * Returns a list of all available room IDs currently managed by the system.
     * @return A list of room ID strings.
     */
    public List<String> getAvailableRooms() {
        return new ArrayList<>(rooms.keySet());
    }

    /**
     * Returns all rooms managed by the system.
     * @return A collection of Room objects.
     */
    public Collection<Room> getAllRooms() {
        return rooms.values();
    }

    /**
     * Adds a new room to the system.
     * @param room The room to add.
     */
    public void addRoom(Room room) {
        rooms.put(room.getRoomId(), room);
        if (activeRoom == null) {
            activeRoom = room;
        }
    }

    /**
     * Adds a new user to the active room.
     * @param userId The user's ID.
     * @param name The user's name.
     */
    public void addUser(String userId, String name) {
        if (activeRoom != null) {
            activeRoom.addUser(userId, name);
        }
    }

    /**
     * Returns all users in the active room.
     * @return A map of user ID to name.
     */
    public Map<String, String> getUsers() {
        return activeRoom != null
            ? activeRoom.getUsers()
            : Collections.emptyMap();
    }

    /**
     * Logs an expense for the active room with automatic conversion to base currency.
     * @param payerId The ID of the payer.
     * @param amount The amount of the expense.
     * @param currency The currency of the expense.
     * @param description The description of the expense.
     * @return The logged transaction.
     */
    public Transaction logExpense(
        String payerId,
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
        return t;
    }

    /**
     * Calculates the settlement plan for the active room using the Greedy Two-Pointer algorithm.
     * @param targetCurrency The currency to settle up in.
     * @return A list of settlement transactions.
     */
    public List<Settlement> calculateSettleUp(String targetCurrency) {
        if (activeRoom == null || activeRoom.getUsers().isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, String> users = activeRoom.getUsers();
        List<Transaction> transactions = activeRoom.getTransactions();

        // accumulation Scan
        Map<String, Double> netBalances = new HashMap<>();
        for (String userId : users.keySet()) {
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

            for (String userId : users.keySet()) {
                netBalances.put(userId, netBalances.get(userId) - share);
            }
        }

        // separate into debtors and creditors
        List<UserBalance> debtors = new ArrayList<>();
        List<UserBalance> creditors = new ArrayList<>();

        for (Map.Entry<String, Double> entry : netBalances.entrySet()) {
            double bal = entry.getValue();
            if (bal < -0.001) {
                debtors.add(new UserBalance(entry.getKey(), bal));
            } else if (bal > 0.001) {
                creditors.add(new UserBalance(entry.getKey(), bal));
            }
        }

        // sort extremes
        debtors.sort(Comparator.comparingDouble(u -> u.balance));
        creditors.sort((u1, u2) -> Double.compare(u2.balance, u1.balance));

        // two pointer patching
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

    /**
     * Calculates the settlement plan in the base currency for the active room using the Greedy Two-Pointer algorithm.
     * @return A list of Settlement objects.
     */
    public List<Settlement> calculateSettleUp() {
        return calculateSettleUp(baseCurrency);
    }

    private static class UserBalance {

        String id;
        double balance;

        UserBalance(String id, double balance) {
            this.id = id;
            this.balance = balance;
        }
    }

    /**
     * Returns the base currency of the system.
     * @return The base currency.
     */
    public String getBaseCurrency() {
        return baseCurrency;
    }
}
