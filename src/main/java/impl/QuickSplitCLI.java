package impl;

import java.util.*;

/**
 * QuickSplitCLI is the command-line interface for the QuickSplit application.
 * It provides a simple interactive loop to manage expenses and settle debts.
 */
public class QuickSplitCLI {

    private final QuickSplitSystem system;
    private final Scanner scanner;
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
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "new_room":
                        handleNewRoom();
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
        String roomId = generateHumanReadableId();
        System.out.println(" - new room with id " + roomId);
    }

    /**
     * Generates a human-readable room ID (e.g., "speedy-badger-42").
     * This eliminates the need for an external library while satisfying the HCD requirement
     * for memorable and friendly identifiers.
     */
    private String generateHumanReadableId() {
        String[] adjectives = {
            "speedy",
            "clever",
            "mighty",
            "brave",
            "happy",
            "swift",
            "bright",
            "gentle",
            "quick",
            "bold",
            "steady",
            "sharp",
            "approved",
            "quiet",
            "calm",
            "vivid",
            "jolly",
            "fancy",
        };
        String[] nouns = {
            "badger",
            "eagle",
            "otter",
            "falcon",
            "panda",
            "tiger",
            "wolf",
            "fox",
            "deer",
            "lynx",
            "owl",
            "buggle",
            "rabbit",
            "mouse",
            "crane",
            "heron",
            "robin",
            "sparrow",
        };
        Random random = new Random();
        String adj = adjectives[random.nextInt(adjectives.length)];
        String noun = nouns[random.nextInt(nouns.length)];
        int num = random.nextInt(99) + 1;
        return String.format("%s-%s-%d", adj, noun, num);
    }

    private void handleAdd(String[] parts) {
        if (parts.length < 4) {
            System.out.println(
                "Usage: add <amount> <user> <description> [currency]"
            );
            return;
        }

        double amount = Double.parseDouble(parts[1]);
        String userName = parts[2];
        String description = parts[3]; // Description is currently stored in Transaction log indirectly via CLI logic
        String currency = (parts.length > 4) ? parts[4] : "USD";

        // Ensure user exists
        int userId = nameToId.computeIfAbsent(userName, name -> {
            int id = nextUserId++;
            system.addUser(id, name);
            return id;
        });

        system.logExpense(userId, amount, currency, description);
    }

    private void handleSettleUp(String[] parts) {
        String targetCurrency = (parts.length > 1) ? parts[1] : "USD";
        List<String> results = system.calculateSettleUp(targetCurrency);
        System.out.println();
        for (String line : results) {
            System.out.println(" - " + line);
        }
    }

    private void handleSave(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: save <filename>");
            return;
        }
        String filename = parts[1];
        System.out.println(
            " - saving data to " + filename + " (local persistence placeholder)"
        );
        // TODO: Implement actual JSON serialization
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println(
            "  new_room                      - Create a new shared room"
        );
        System.out.println(
            "  add <amt> <user> <desc> [cur] - Log an expense (e.g., add 10 Alice food eur)"
        );
        System.out.println(
            "  settleup [currency]           - Calculate the optimized settlement"
        );
        System.out.println(
            "  save <filename>               - Save the room data locally"
        );
        System.out.println(
            "  exit                          - Quit the application"
        );
    }

    public static void main(String[] args) {
        new QuickSplitCLI().start();
    }
}
