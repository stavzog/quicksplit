package impl;

import java.util.*;
import util.CloudStorageService;
import util.LocalStorageService;
import util.StorageService;

/**
 * QuickSplitCLI provides a context-aware interactive flow for managing expenses.
 * It integrates with the StorageService interface to abstract data persistence,
 * seamlessly handling both local file saving and live cloud synchronization.
 */
public class QuickSplitCLI {

    private final QuickSplitSystem system;
    private final Scanner scanner;
    private final Map<String, String> nameToId = new HashMap<>();

    private String currentUserName;
    private String currentUserId;
    private StorageService storageService;
    private boolean isCloudMode = false;

    public QuickSplitCLI() {
        this.system = new QuickSplitSystem();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("=== Welcome to QuickSplit ===");

        // 1. Identify the user
        System.out.print("Enter your name: ");
        this.currentUserName = scanner.nextLine().trim();
        if (currentUserName.isEmpty()) currentUserName = "Anonymous";

        // Setup initial user ID (UUID)
        this.currentUserId = UUID.randomUUID().toString();
        nameToId.put(currentUserName, currentUserId);

        // 2. Initial Setup
        roomManagerFlow();

        // 3. Main Command Loop
        mainLoop();
    }

    /**
     * The core Room Manager flow.
     * Consolidates logic into two intuitive choices: Create or Join.
     */
    private void roomManagerFlow() {
        while (true) {
            System.out.println("\n--- Room Manager ---");
            System.out.println(" 1. Create a new room");
            System.out.println(" 2. Join a room");
            System.out.print("Selection (1/2): ");

            String choice = scanner.nextLine().trim();
            if (choice.equals("1")) {
                String roomId = system.createRoom();
                system.addUser(currentUserId, currentUserName);
                System.out.println(" - New room created and joined: " + roomId);
                break;
            } else if (choice.equals("2")) {
                if (handleJoinContextually()) break;
            } else {
                System.out.println("Invalid selection.");
            }
        }
    }

    /**
     * Context-aware joining logic based on current system state.
     */
    private boolean handleJoinContextually() {
        List<String> availableRooms = system.getAvailableRooms();

        // SCENARIO A: currently in cloud mode
        if (isCloudMode && storageService != null) {
            System.out.print("Enter Room ID to fetch from Cloud: ");
            String roomId = scanner.nextLine().trim();

            // try joining local first
            if (system.joinRoom(roomId)) {
                System.out.println(
                    " - Switched to local cache of room: " + roomId
                );
                syncLocalUserCache();
                return true;
            }

            // otherwise, fetch from the cloud using the storage service
            try {
                System.out.println(" - Fetching room from cloud...");
                Room fetchedRoom = storageService.fetchRoom(roomId);

                if (fetchedRoom != null) {
                    system.addRoom(fetchedRoom);
                    system.joinRoom(roomId);
                    syncLocalUserCache();
                    System.out.println(
                        " - Successfully joined cloud room: " + roomId
                    );
                    return true;
                } else {
                    System.out.println(
                        " - Room '" + roomId + "' not found in Cloud."
                    );
                    return false;
                }
            } catch (Exception e) {
                System.out.println(" - Cloud Error: " + e.getMessage());
                return false;
            }
        }

        // SCENARIO B: local data already in memory
        if (!availableRooms.isEmpty()) {
            System.out.println("\nAvailable Rooms in Memory:");
            for (int i = 0; i < availableRooms.size(); i++) {
                System.out.println(
                    " " + (i + 1) + ". " + availableRooms.get(i)
                );
            }
            System.out.println(
                " " +
                    (availableRooms.size() + 1) +
                    ". Load a different file/source"
            );
            System.out.print("Selection: ");

            try {
                int selection = Integer.parseInt(scanner.nextLine().trim());
                if (selection > 0 && selection <= availableRooms.size()) {
                    String selectedId = availableRooms.get(selection - 1);
                    system.joinRoom(selectedId);
                    syncLocalUserCache();
                    System.out.println(" - Joined: " + selectedId);
                    return true;
                } else if (selection == availableRooms.size() + 1) {
                    return handleLoadSourceSelection();
                }
            } catch (Exception e) {
                System.out.println("Invalid selection.");
            }
            return false;
        }

        // SCENARIO C: nothing in memory yet
        return handleLoadSourceSelection();
    }

    /**
     * Final fallback: Asks where to look for data if nothing is known.
     */
    private boolean handleLoadSourceSelection() {
        System.out.println("\nWhere should we look for rooms?");
        System.out.println(" 1. A local file (.json)");
        System.out.println(" 2. The Cloud (JSONBin)");
        System.out.print("Selection (1/2): ");

        String choice = scanner.nextLine().trim();
        if (choice.equals("1")) {
            System.out.print("Enter filename (e.g., trips.json): ");
            String filename = scanner.nextLine().trim();

            storageService = new LocalStorageService();
            storageService.setTarget(filename);
            isCloudMode = false;

            try {
                Collection<Room> loadedRooms = storageService.load();
                for (Room room : loadedRooms) {
                    system.addRoom(room);
                }
                System.out.println(" - File loaded.");
                return handleJoinContextually(); // Recurse to show the newly loaded rooms
            } catch (Exception e) {
                System.out.println(" - Error loading file: " + e.getMessage());
            }
        } else if (choice.equals("2")) {
            storageService = new CloudStorageService();
            storageService.setTarget(null); // use default bin setup in service
            isCloudMode = true;
            return handleJoinContextually(); // Recurse to enter cloud ID
        }
        return false;
    }

    private void mainLoop() {
        printHelp();
        while (true) {
            String activeRoom = system.getActiveRoomId();
            String mode = isCloudMode ? "Cloud" : "Local";
            System.out.print(
                "\n[" +
                    currentUserName +
                    " @ " +
                    (activeRoom != null ? activeRoom : "No Room") +
                    " (" +
                    mode +
                    ")] > "
            );

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "add":
                        handleAdd(parts);
                        break;
                    case "log":
                        handleViewLog();
                        break;
                    case "settleup":
                        handleSettleUp(parts);
                        break;
                    case "save":
                    case "sync":
                        handleSave();
                        break;
                    case "join":
                        handleJoinCommand(parts);
                        break;
                    case "help":
                        printHelp();
                        break;
                    case "exit":
                    case "quit":
                        System.out.println("Goodbye, " + currentUserName + "!");
                        return;
                    default:
                        System.out.println(
                            "Unknown command. Type 'help' for usage."
                        );
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void handleJoinCommand(String[] parts) {
        if (parts.length > 1) {
            String roomId = parts[1];
            if (system.joinRoom(roomId)) {
                System.out.println(" - Switched to room: " + roomId);
                syncLocalUserCache();
            } else {
                System.out.println(
                    " - Room '" + roomId + "' not found locally."
                );
                roomManagerFlow();
            }
        } else {
            roomManagerFlow();
        }
    }

    /**
     * Optimized 'add' command: add <amt> [desc] [cur] [user]
     */
    private void handleAdd(String[] parts) {
        if (system.getActiveRoomId() == null) {
            System.out.println(" - Error: Join or create a room first.");
            return;
        }
        if (parts.length < 2) {
            System.out.println(
                "Usage: add <amount> [description] [currency] [user]"
            );
            return;
        }

        double amount = Double.parseDouble(parts[1]);
        String desc = (parts.length > 2) ? parts[2] : "expense";
        String cur = (parts.length > 3) ? parts[3].toUpperCase() : "USD";
        String targetName = (parts.length > 4) ? parts[4] : currentUserName;

        String userId = nameToId.computeIfAbsent(targetName, name -> {
            String uuid = UUID.randomUUID().toString();
            system.addUser(uuid, name);
            return uuid;
        });

        if (!system.getUsers().containsKey(userId)) {
            system.addUser(userId, targetName);
        }

        system.logExpense(userId, amount, cur, desc);
    }

    private void handleViewLog() {
        Room room = system.getActiveRoom();
        if (room == null) return;

        List<Transaction> txs = room.getTransactions();
        Map<String, String> userNames = room.getUsers();

        System.out.println(
            "\n--- Transaction Log for [" + room.getRoomId() + "] ---"
        );
        if (txs.isEmpty()) {
            System.out.println(" No transactions recorded.");
        } else {
            for (Transaction t : txs) {
                String name = userNames.getOrDefault(t.getPayerId(), "Unknown");
                System.out.printf(
                    " - %s spent %.2f USD on '%s' (Original: %.2f %s)\n",
                    name,
                    t.getAmount(),
                    t.getDescription(),
                    t.getOriginalAmount(),
                    t.getOriginalCurrency()
                );
            }
        }
    }

    private void handleSettleUp(String[] parts) {
        if (system.getActiveRoom() == null) return;

        String targetCurrency = (parts.length > 1) ? parts[1] : "USD";
        List<Settlement> results = system.calculateSettleUp(targetCurrency);

        System.out.println(
            "\n--- Settlement Plan (" + targetCurrency.toUpperCase() + ") ---"
        );
        if (results.isEmpty()) {
            System.out.println(" All settled up!");
        } else {
            for (Settlement s : results) {
                System.out.println(" - " + s.toString(system.getUsers()));
            }
        }
    }

    private void ensureStorageService() {
        if (storageService != null) return;

        System.out.println(
            "\nYou haven't specified where to save your data yet."
        );
        System.out.println(" 1. Local file");
        System.out.println(" 2. Cloud (JSONBin)");
        System.out.print("Selection (1/2): ");

        String choice = scanner.nextLine().trim();
        if (choice.equals("2")) {
            storageService = new CloudStorageService();
            storageService.setTarget(null);
            isCloudMode = true;
        } else {
            storageService = new LocalStorageService();
            System.out.print("Enter filename to save to: ");
            String filename = scanner.nextLine().trim();
            storageService.setTarget(filename);
            isCloudMode = false;
        }
    }

    private void handleSave() {
        ensureStorageService();

        try {
            System.out.println(
                isCloudMode
                    ? " - Syncing all rooms to the cloud..."
                    : " - Saving all rooms locally..."
            );

            // Execute the persistence strategy defined by the underlying service
            Collection<Room> syncedRooms = storageService.save(
                system.getAllRooms()
            );

            // Reload the newly merged state back into the system
            for (Room room : syncedRooms) {
                system.addRoom(room);
            }

            System.out.println(
                isCloudMode
                    ? " - Cloud Sync Successful."
                    : " - Local Save Successful."
            );
        } catch (Exception e) {
            System.out.println(" - Storage Error: " + e.getMessage());
        }
    }

    private void syncLocalUserCache() {
        Map<String, String> roomUsers = system.getUsers();
        for (Map.Entry<String, String> entry : roomUsers.entrySet()) {
            nameToId.put(entry.getValue(), entry.getKey());
            if (entry.getValue().equalsIgnoreCase(currentUserName)) {
                this.currentUserId = entry.getKey();
            }
        }
    }

    private void printHelp() {
        System.out.println("\nCommands:");
        System.out.println(
            "  add <amt> [desc] [cur] [user]  - Log an expense (defaults to you)"
        );
        System.out.println(
            "  log                            - View transaction history"
        );
        System.out.println(
            "  settleup [currency]            - Show optimized payments"
        );
        System.out.println(
            "  save/sync                      - Persist ALL rooms via active storage"
        );
        System.out.println(
            "  join [room_id]                 - Switch room or open room manager"
        );
        System.out.println("  help                           - Show this menu");
        System.out.println(
            "  exit                           - Close QuickSplit"
        );
    }

    public static void main(String[] args) {
        new QuickSplitCLI().start();
    }
}
