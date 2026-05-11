package impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * QuickSplitCLI is the command-line interface for the QuickSplit application.
 * It provides a simple interactive loop to manage expenses and settle debts across multiple rooms.
 */
public class QuickSplitCLI {

    private final QuickSplitSystem system;
    private final Scanner scanner;
    // Map names to IDs within the session for easier command entry
    private final Map<String, Integer> nameToId = new HashMap<>();
    private int nextUserId = 1;

    public QuickSplitCLI() {
        this.system = new QuickSplitSystem();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("QuickSplit CLI - Simplified Expense Sharing");
        System.out.println("Type 'help' for a list of commands.");

        while (true) {
            String activeRoom = system.getActiveRoomId();
            String prompt = (activeRoom != null)
                ? "[" + activeRoom + "] > "
                : "> ";
            System.out.print("\n" + prompt);

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "new_room":
                        handleNewRoom();
                        break;
                    case "join":
                        handleJoin(parts);
                        break;
                    case "add":
                        handleAdd(parts);
                        break;
                    case "settleup":
                        handleSettleUp(parts);
                        break;
                    case "save":
                        handleSave(parts);
                        break;
                    case "load":
                        handleLoad(parts);
                        break;
                    case "help":
                        printHelp();
                        break;
                    case "exit":
                    case "quit":
                        System.out.println("Goodbye!");
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

    private void handleNewRoom() {
        String roomId = system.createRoom();
        System.out.println(" - new room created and joined with id: " + roomId);
    }

    private void handleJoin(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: join <room_id>");
            return;
        }
        String roomId = parts[1];
        if (system.joinRoom(roomId)) {
            System.out.println(" - successfully joined room: " + roomId);
        } else {
            System.out.println(" - error: room '" + roomId + "' not found.");
        }
    }

    private void handleAdd(String[] parts) {
        if (system.getActiveRoomId() == null) {
            System.out.println(" - error: join or create a room first.");
            return;
        }

        if (parts.length < 4) {
            System.out.println(
                "Usage: add <amount> <user> <description> [currency]"
            );
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(parts[1]);
        } catch (NumberFormatException e) {
            System.out.println(" - error: invalid amount.");
            return;
        }

        String userName = parts[2];
        String description = parts[3];
        String currency = (parts.length > 4) ? parts[4] : "USD";

        // Ensure user exists in current session scope
        int userId = nameToId.computeIfAbsent(userName, name -> {
            int id = nextUserId++;
            system.addUser(id, name);
            return id;
        });

        // Ensure user is registered in the active room's specific map
        if (!system.getUsers().containsKey(userId)) {
            system.addUser(userId, userName);
        }

        system.logExpense(userId, amount, currency, description);
    }

    private void handleSettleUp(String[] parts) {
        if (system.getActiveRoomId() == null) {
            System.out.println(" - error: join or create a room first.");
            return;
        }

        String targetCurrency = (parts.length > 1) ? parts[1] : "USD";
        List<Settlement> results = system.calculateSettleUp(targetCurrency);

        System.out.println();
        if (results.isEmpty()) {
            System.out.println(" - All settled up! No transactions needed.");
        } else {
            for (Settlement s : results) {
                System.out.println(" - " + s.toString(system.getUsers()));
            }
        }
    }

    private void handleSave(String[] parts) {
        if (system.getActiveRoomId() == null) {
            System.out.println(" - error: No active room to save.");
            return;
        }
        if (parts.length < 2) {
            System.out.println("Usage: save <filename>");
            return;
        }

        String filename = parts[1];
        if (!filename.endsWith(".json")) {
            filename += ".json";
        }

        try {
            String json = system.exportActiveRoom();
            Files.writeString(Paths.get(filename), json);
            System.out.println(
                " - successfully saved room '" +
                    system.getActiveRoomId() +
                    "' to " +
                    filename
            );
        } catch (IOException e) {
            System.out.println(" - error saving file: " + e.getMessage());
        }
    }

    private void handleLoad(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: load <filename>");
            return;
        }

        String filename = parts[1];
        try {
            String json = Files.readString(Paths.get(filename));
            system.importRoom(json);

            // Re-sync nameToId map with loaded users to maintain session consistency
            Map<Integer, String> loadedUsers = system.getUsers();
            for (Map.Entry<Integer, String> entry : loadedUsers.entrySet()) {
                nameToId.put(entry.getValue(), entry.getKey());
                if (entry.getKey() >= nextUserId) {
                    nextUserId = entry.getKey() + 1;
                }
            }

            System.out.println(
                " - successfully loaded room: " + system.getActiveRoomId()
            );
        } catch (IOException e) {
            System.out.println(" - error loading file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(" - error parsing room data: " + e.getMessage());
        }
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println(
            "  new_room                      - Create and join a new shared room"
        );
        System.out.println(
            "  join <id>                     - Join an existing room by ID"
        );
        System.out.println(
            "  add <amt> <user> <desc> [cur] - Log an expense (e.g., add 10 Alice food eur)"
        );
        System.out.println(
            "  settleup [currency]           - Calculate the optimized settlement"
        );
        System.out.println(
            "  save <filename>               - Save the current room data to a JSON file"
        );
        System.out.println(
            "  load <filename>               - Load room data from a JSON file"
        );
        System.out.println(
            "  exit                          - Quit the application"
        );
    }

    public static void main(String[] args) {
        new QuickSplitCLI().start();
    }
}
