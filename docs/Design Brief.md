
# Quicksplit

Quicksplit is a lightweight, human-centered command-line utility for splitting group expenses. It is designed to eliminate the friction and "social overhead" that is associated with modern expense-sharing applications. Traditional tools frequently require users to download heavy mobile apps, create accounts, and navigate paywalls just to log a simple shared meal. This process discourages use of these tools. By following a Human-Centered Design (HCD) process, Quicksplit prioritizes the end-user's need for speed and simplicity. It solves the "barrier to entry" problem by providing a single-file executable that requires no installation: users simply run the tool and join a shared "room" using a unique ID.

The software utilizes a robust Object-Oriented architecture to manage data integrity while supporting high-performance processing of transaction logs for multiple concurrent users. Key features include:

- **Zero-Account Collaboration**: Multi-user support through "Cloud Rooms" that sync via a lite REST API, removing the need for traditional authentication.
- **Optimized Debt Simplification**: An algorithmic "Settle Up" feature that utilizes a greedy two-pointer strategy to minimize the total number of payments between friends.
- **Frictionless Distribution**: Delivered as a standalone executable to ensure the tool is accessible to any user, regardless of their technical environment.
- **Multi-Currency Support**: High-speed currency conversion for international expenses. The system maintains a single "Base Currency" (USD) for the transaction log to ensure data integrity, while allowing users to input expenses in any major currency and view "Settle Up" results in their currency of choice via real-time API integration.
- **Distributed Data Integrity**: Uses Universally Unique Identifiers (UUIDs) for users and transactions to ensure collision-free synchronization across different devices.

## Technical Summary

This summary consolidates the Human-Centered Design (HCD) goals with the high-performance architectural choices implemented in QuickSplit.

---

### **Technical Specifications**

* **Environment**: Java-based command-line utility (CLI).
* **Distribution**: Packaged as a **standalone JAR executable** to eliminate installation friction.
* **Persistence**: Supports local file storage (JSON) and **External REST API integration** (JSONBin.io) for "Cloud Room" synchronization.
* **Architecture**: Separated the domain logic (QuickSplitSystem) from the interactive shell (CLI) and abstracting data persistence (StorageService).

---

### **Core Data Structures**

#### **A. The Transaction Log (The Source of Truth)**
* **Structure**: A flat `ArrayList` of `Transaction` objects.
* **Role**: Acts as the immutable history of every expense. Each transaction includes a unique ID, payer ID, amount, and description.
* **Use Case**: Used for persistence and as the input for the accumulation scan.

#### **B. Net Balance Map**
* **Structure**: A `Map<String, Double>` mapping User UUIDs to their current balance.
* **Role**: An intermediate data representation generated via a linear scan of the transaction log.
* **Use Case**: Stores the final "debt status" of each user (e.g., Alice: +$15.00, Bob: -$15.00) before the simplification algorithm runs.

#### **C. Settlement Collection**
* **Structure**: A `List<Settlement>` objects.
* **Role**: The optimized projection of the transaction log. It represents the most efficient flow of money between group members.
* **Use Case**: The final output of the "Settle Up" command.

### **Data Format and Serialization**

In order to avoid using heavy external libraries for serialization, we came up with an efficient json schema for out data and we coded a custom serializer that converts transaction and room data into JSON. By taking control of the serialization process, we could avoid the overhead of reflection-based libraries while also using a data format that is more efficient and easier to work with for our app. Below is the JSON schema:

``` json
{
  "rooms":[
    {
      "transactions":[
        {
          "amount":10.0,
          "originalAmount":10.0,
          "exchangeRate":1.0,
          "description":"expense",
          "originalCurrency":"USD",
          "userId":"4cc2feab-ce2a-46d8-9000-f8364a4a1239"
        },
        /* ... */
      ],
      "roomId":"steady-wolf-26",
      "users":{
        "4cc2feab-ce2a-46d8-9000-f8364a4a1239":"Alice",
        "d64286fc-3a1e-4f1d-9d02-fb91e68bd33b":"Bob",
        /* ... */
      }
    }
  ]
}
```

Note: at the beginning of the project we created a synthetic csv [dataset](old_dataset_schema) with LLMs. However, we realized that JSON would be a much easier format to work with, especially because of JSONBin.io.

### **Synthetic Data Generation**

We generated synthetic transaction data to test our application using a custom data generator that simulates real-world expense scenarios. The script can be found at `docs/DataGenerator.java`. The dataset matches the JSON format that is used by the application. It is located in the project root as `synthetic_transactions.json`.

---

### **Algorithms**

#### **A. The Accumulation Scan (Log Projection)**

* **Goal**: To calculate the net balance for every person in a room.
* **Process**: A single-pass linear scan ($O(N)$ complexity, where $N$ is the number of transactions). For each entry, it credits the payer and debits all participants by an equal share.
* **Complexity**: $T(n) = O(n)$; Space $= O(V)$ (where $V$ is the number of users).

#### **B. Greedy Debt Simplification (Two-Pointer Method)**
* **Goal**: To minimize the total number of transactions required to settle the room. In a group of $V$ people, the algorithm ensures that there are at most $V-1$ transactions.
* **Process**: 
    1.  Sort the non-zero balances from most negative (debtors) to most positive (creditors) ($O(V \log V)$).
    2.  Use two pointers (Left for biggest debtor, Right for biggest creditor) to match and cancel out debts.
    3.  Generate a `Settlement` instruction for each match.
* **Complexity**: $T(n) = O(V \log V)$; Space $= O(V)$.

#### **C. Safe Bidirectional Merge**
* **Goal**: To synchronize local data with the cloud without overwriting other users' work.
* **Process**: Uses a `HashSet` of unique transaction IDs to perform an additive merge of the local and remote logs.
* **Complexity**: $T(n) = O(T_{total})$ where $T$ is the number of transactions.


---

### **Final Project Status**

| Component | Status | Technical Detail |
| :--- | :--- | :--- |
| **Engine** | **Complete** | `QuickSplitSystem` and `Room` classes manage core logic. |
| **Persistence** | **Complete** | `LocalStorageService` and `CloudStorageService` implemented via Strategy Pattern. |
| **Settle-Up Logic** | **Complete** | Two-pointer greedy algorithm implemented with $O(V \log V)$ efficiency. |
| **Cloud Sync** | **Complete** | JSONBin.io integration with safe merging and optimized JSONPath fetching. |
| **CLI Shell** | **Complete** | Context-aware interactive loop with automatic user identification. |

---

## Implementation Philosophy

QuickSplit follows Clean Architecture principles. The `System` handles the data models and math, the `CLI` handles user interaction and formatting, and the `StorageService` provides a unified interface for data boundaries. By avoiding heavy external dependencies and reflection, the project remains fast, predictable, and cache-friendly.