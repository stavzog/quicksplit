package util;

import impl.CustomJsonSerializer;
import impl.Room;
import impl.Transaction;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Utility class to generate a synthetic dataset of 1000 transactions.
 * The output matches the custom JSON schema used by QuickSplit.
 */
public class DataGenerator {

    // this script needs to run inside the src folder. It is not part of the main QuickSplit application, though, which is why it is was moved to the docs folder.

    private static final String[] NAMES = {
        "Amina",
        "Adonis",
        "Pedro",
        "Owen",
        "Nina",
        "George",
        "Daniel",
        "Liam",
        "Elena",
        "Nikos",
        "Noah",
        "Sophia",
        "Lucas",
        "Mia",
        "Thanos",
        "Ava",
        "Oliver",
        "Isabella",
    };

    private static final String[] DESCRIPTIONS = {
        "museum entry",
        "dinner",
        "late-night food",
        "beach gear",
        "rideshare",
        "gas",
        "snacks",
        "parking",
        "coffee",
        "hotel",
        "train tickets",
        "souvenirs",
        "groceries",
        "flight",
    };

    private static final String[] CURRENCIES = {
        "USD",
        "EUR",
        "GBP",
        "JPY",
        "CAD",
        "AUD",
    };

    public static void main(String[] args) {
        Random random = new Random();
        List<Room> rooms = new ArrayList<>();

        // generate 10 rooms
        for (int i = 1; i <= 10; i++) {
            String roomId = String.format("trip-vault-%03d", i);
            Room room = new Room(roomId);

            // assign 5-8 random users to each room
            int userCount = 5 + random.nextInt(4);
            Set<String> selectedNames = new HashSet<>();
            while (selectedNames.size() < userCount) {
                selectedNames.add(NAMES[random.nextInt(NAMES.length)]);
            }

            for (String name : selectedNames) {
                room.addUser(UUID.randomUUID().toString(), name);
            }

            rooms.add(room);
        }

        // generate 1000 transactions across all rooms
        int totalTransactions = 1000;
        for (int i = 0; i < totalTransactions; i++) {
            Room room = rooms.get(random.nextInt(rooms.size()));

            // pick random payer from the room
            Map<String, String> users = room.getUsers();
            List<String> userIds = new ArrayList<>(users.keySet());
            String payerId = userIds.get(random.nextInt(userIds.size()));

            double amount = 5.0 + (random.nextDouble() * 195.0); // range $5 - $200
            String currency = CURRENCIES[random.nextInt(CURRENCIES.length)];
            String description = DESCRIPTIONS[random.nextInt(
                DESCRIPTIONS.length
            )];

            // mock conversion rates for synthetic data
            // to avoid slow api calls
            double rate = 1.0;
            if (currency.equals("EUR")) rate = 1.08;
            else if (currency.equals("GBP")) rate = 1.25;
            else if (currency.equals("JPY")) rate = 0.0065;
            else if (currency.equals("CAD")) rate = 0.73;
            else if (currency.equals("AUD")) rate = 0.66;

            double amountInUsd = amount * rate;

            Transaction t = new Transaction(
                UUID.randomUUID().toString(),
                payerId,
                amountInUsd,
                currency,
                amount,
                rate,
                description
            );

            room.logExpense(t);
        }

        // serialize the data
        String jsonOutput = CustomJsonSerializer.serializeRooms(rooms);

        // write to synthetic_transactions.json
        try (
            FileWriter writer = new FileWriter("synthetic_transactions.json")
        ) {
            writer.write(jsonOutput);
            System.out.println(
                "Successfully generated synthetic_transactions.json with 1000 transactions."
            );
            System.out.println("File location: project root");
        } catch (IOException e) {
            System.err.println(
                "Error writing synthetic data: " + e.getMessage()
            );
        }
    }
}
