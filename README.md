# QuickSplit

QuickSplit is a lightweight, human-centered command-line utility for splitting group epenses. It is designed to eliminate the friction and "social overhead" that is associated with modern expense-sharing applications. Traditional tools frequently require users to download heavy mobile apps, create accounts, and navigate paywalls just to log a simple shared meal. This process discourages use of these tools. By following a Human-Centered Design (HCD) process, QuickSplit prioritizes the end-user's need for speed and simplicity. It solves the "barrier to entry" problem by providing a single-file executable that requires no installation: users simply run the tool and join a shared "room" using a unique ID.

The software utilizes a robust Object-Oriented architecture to manage data integrity while supporting high-performance processing of transaction logs for multiple concurrent users. Key features include:

- Zero-Account Collaboration: Multi-user support through "Cloud Rooms" that sync via a lite REST API, removing the need for traditional authentication.
- Optimized Debt Simplification: An algorithmic "Settle Up" feature that processes transaction data to provide an efficient settlement plan and minimize the number of payments between friends.
- Frictionless Distribution: Delivered as a standalone executable to ensure the tool is accessible to any user, regardless of their technical environment.
- Multi-Currency Support: High-speed currency conversion for international expenses. The system maintains a single "Base Currency" (USD) for the transaction log to ensure data integrity, while allowing users to input expenses in any major currency and view "Settle Up" results in their currency of choice via real-time API integration (Frankfurter API).
- Needfinding-Driven Analysis: The logic and features of the app were derived from user interviews and real-world pain points.

## Usage Example

Below is the interaction flow within the QuickSplit CLI:

```text
=== Welcome to QuickSplit ===
Enter your name: Alice

--- Room Manager ---
 1. Create a new room
 2. Join a room
Selection (1/2): 1
 - New room created and joined: speedy-badger-42

[Alice @ speedy-badger-42 (Local)] > add 30 "Group Pizza" eur
Logged: Payer: Alice, Description: Group Pizza, Amount: 32.48 USD (Rate: 1.08)

[Alice @ speedy-badger-42 (Local)] > add 15 "Taxi" usd Bob
Logged: Payer: Bob, Description: Taxi, Amount: 15.00 USD

[Alice @ speedy-badger-42 (Local)] > settleup usd

--- Settlement Plan (USD) ---
 - Bob pays Alice 8.74 USD
```

## How to Run

The project is distributed as a standalone JAR file located at the root of the directory.

### Executing the CLI
To run the application, ensure you have Java 11 or higher installed and download the `quicksplit.jar` file in the root directory. Navigate to the directory where the JAR is located and run the following command:

```bash
java -jar quicksplit.jar
```

### Building from Source
If you wish to re-compile the JAR or run the project using Gradle:

```bash
./gradlew run
```

The **Main Class** contained within the JAR is `impl.QuickSplitCLI`.

## Installation & Dependencies

### External Libraries
For production, the project is designed to be zero-dependency for the end user. All necessary logic (including JSON parsing in the included `mjson` source and networking for currency/cloud sync) is handled by standard Java 11+ libraries or source code included in the repository.

For testing, JUnit 5 is required. These dependencies are managed automatically by Gradle. To run the test suite, use:

```bash
./gradlew test
```

## API Documentation

The following public methods represent the core API of the QuickSplit engine.

### `impl.QuickSplitSystem`

The central coordinator for managing sessions and rooms. This entity holds the business logic of the app.

- **`QuickSplitSystem()`** (Constructor)
    - Initializes a new system instance with a default base currency of USD.
    - *Example:* `QuickSplitSystem system = new QuickSplitSystem();`
- **`String createRoom()`**
    - Generates a new room with a unique human-readable ID and sets it as the active room.
    - **Returns:** The human-readable ID of the newly created room (`String`).
    - *Example:* `system.createRoom() -> "swift-fox-12"`
- **`boolean joinRoom(String roomId)`**
    - Switches the active context to the room matching the provided ID if it exists in memory.
    - **Inputs:** `roomId` (`String`)
    - **Returns:** `true` if the room exists and was joined, `false` otherwise.
    - *Example:* `system.joinRoom("swift-fox-12") -> true`
- **`void addUser(String userId, String name)`**
    - Registers a user in the current active room.
    - **Inputs:** `userId` (`String`), `name` (`String`)
    - *Example:* `system.addUser("f47ac10b...", "Stavros")`
- **`Transaction logExpense(String payerId, double amount, String currency, String description)`**
    - Converts an expense to the base currency (USD) and logs it to the active room.
    - **Inputs:** `payerId` (`String`), `amount` (`double`), `currency` (`String`), `description` (`String`)
    - **Returns:** The created `Transaction` object.
    - *Example:* `system.logExpense("u1", 10.0, "EUR", "Coffee") -> Transaction (10.82 USD)`
- **`List<Settlement> calculateSettleUp(String targetCurrency)`**
    - Executes the Greedy Two-Pointer algorithm to calculate the optimized payments.
    - **Inputs:** `targetCurrency` (`String`)
    - **Returns:** A `List` of `Settlement` objects.
    - *Example:* `system.calculateSettleUp("USD") -> [Settlement(Bob pays Alice 5.00)]`

### `impl.Room`
Encapsulates data for a specific sharing group.

- **`void merge(Room other)`**
    - Merges another room's data into the current instance. It uses unique transaction IDs to ensure no duplicates are created during synchronization.
    - **Inputs:** `other` (`Room`)
    - *Example:* `roomA.merge(roomB)`

### `impl.Settlement`
A data object representing a required payment.

- **`String toString(Map<String, String> userNames)`**
    - Converts the settlement into a human-readable instruction using a provided mapping of IDs to Names.
    - **Inputs:** `userNames` (`Map<String, String>`)
    - **Returns:** A formatted string.
    - *Example:* `settlement.toString(names) -> "Alice pays Bob 12.50 USD"`

### `util.StorageService` (Interface)
The strategy interface for data persistence. Implementations include `LocalStorageService` and `CloudStorageService`.

- **`Collection<Room> save(Collection<Room> rooms)`**
    - Persists the provided rooms to storage. For cloud storage, this performs a safe merge: it loads the existing rooms from JSONbin, merges them with the room in memory, and then re-uploads the updated collection.
    - **Inputs:** `rooms` (`Collection<Room>`)
    - **Returns:** The updated collection of rooms after synchronization.
    - *Example:* `storage.save(system.getAllRooms())`
- **`Collection<Room> load()`**
    - Retrieves all rooms from the storage source.
    - **Returns:** `Collection<Room>`
    - *Example:* `storage.load() -> [Room, Room]`
- **`Room fetchRoom(String roomId)`**
    - Fetches a single specific room from the storage source.
    - **Inputs:** `roomId` (`String`)
    - **Returns:** The requested `Room` or `null`.
    - *Example:* `storage.fetchRoom("swift-fox-12") -> Room object`

---
*Developed as a Data Structures Final Project.*
