package impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * QuickSplitCLI provides a person-centered, interactive flow for managing expenses.
 * It prioritizes user identity and streamlined transaction logging to reduce social overhead.
 */
public class QuickSplitCLI {

    private final QuickSplitSystem system;
    private final Scanner scanner;
    private final Map<String, String> nameToId = new HashMap<>();

    private String currentUserName;
    private String currentUserId;
    private String lastUsedFilename;
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

        // 2. Initial Room Action
        initialSetup();

        // 3. Main Command Loop
        mainLoop();
    }

    private void initialSetup() {
        while (true) {
            System.out.println("\nWould you like to:");
            System.out.println(" 1. Create a new room");
            System.out.println(" 2. Join an existing room");
            System.out.print("Selection (1/2): ");

            String choice = scanner.nextLine().trim();
            if (choice.equals("1")) {
                String roomId = system.createRoom();
                system.addUser(currentUserId, currentUserName);
                System.out.println(" - New room created: " + roomId);
                break;
            } else if (choice.equals("2")) {
                if (handleJoinProcess()) break;
            } else {
                System.out.println("Invalid selection.");
            }
        }
    }

    private boolean handleJoinProcess() {
        System.out.println("\nLoad from:");
        System.out.println(" 1. Local file");
        System.out.println(" 2. Cloud (JSONBin)");
        System.out.print("Selection (1/2): ");

        String source = scanner.nextLine().trim();
        if (source.equals("1")) {
            System.out.print("Enter filename (e.g., trip.json): ");
            String filename = scanner.nextLine().trim();
            try {
                String json = Files.readString(Paths.get(filename));
                system.importRoom(json);
                this.lastUsedFilename = filename;
                this.isCloudMode = false;
                syncLocalUserCache();
                System.out.println(
                    " - Successfully loaded room: " + system.getActiveRoomId()
                );
                return true;
            } catch (Exception e) {
                System.out.println(" - Error loading file: " + e.getMessage());
            }
        } else if (source.equals("2")) {
            System.out.print("Enter Cloud Room ID: ");
            String cloudId = scanner.nextLine().trim();
            System.out.println(" - Cloud sync placeholder for ID: " + cloudId);
            // This would call system.sync(cloudId) in future implementation
            this.isCloudMode = true;
            return false; // Loop back for now as cloud is not fully implemented
        }
        return false;
    }

    private void mainLoop() {
        printHelp();
        while (true) {
            String activeRoom = system.getActiveRoomId();
            System.out.print(
                "\n[" + currentUserName + " @ " + activeRoom + "] > "
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
                        handleSave();
                        break;
                    case "join":
                        initialSetup();
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

    /**
     * Optimized 'add' command: add <amt> [desc] [cur] [user]
     */
    private void handleAdd(String[] parts) {
        if (parts.length < 2) {
            System.out.println(
                "Usage: add <amount> [description] [currency] [user]"
            );
            return;
        }

        double amount = Double.parseDouble(parts[1]);
        String desc = (parts.length > 2) ? parts[2] : "expense";
        String cur = (parts.length > 3) ? parts[3] : "USD";
        String targetName = (parts.length > 4) ? parts[4] : currentUserName;

        // get or create User ID
        String userId = nameToId.computeIfAbsent(targetName, name -> {
            String uuid = UUID.randomUUID().toString();
            system.addUser(uuid, name);
            return uuid;
        });

        // ensure user is in the active room
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

        System.out.println("\n--- Transaction Log ---");
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

    private void handleSave() {
        if (isCloudMode) {
            System.out.println(" - Syncing to Cloud... (Placeholder)");
        } else {
            String filename = lastUsedFilename;
            if (filename == null) {
                System.out.print("Enter filename to save: ");
                filename = scanner.nextLine().trim();
                if (!filename.endsWith(".json")) filename += ".json";
                lastUsedFilename = filename;
            }

            try {
                String json = system.exportActiveRoom();
                Files.writeString(Paths.get(filename), json);
                System.out.println(" - Successfully saved to " + filename);
            } catch (IOException e) {
                System.out.println(" - Error saving: " + e.getMessage());
            }
        }
    }

    private void syncLocalUserCache() {
        Map<String, String> roomUsers = system.getUsers();
        for (Map.Entry<String, String> entry : roomUsers.entrySet()) {
            nameToId.put(entry.getValue(), entry.getKey());
            // If the user's name is already in the room, use that ID for current session
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
            "  save                           - Persist data (local/cloud)"
        );
        System.out.println(
            "  join                           - Switch or create a new room"
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
