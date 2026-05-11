# Quicksplit

Quicksplit is a lightweight, human-centered command-line utility for splitting group epenses. It is designed to eliminate the friction and "social overhead" that is associated with modern expense-sharing applications. Traditional tools frequently require users to download heavy mobile apps, create accounts, and navigate paywalls just to log a simple shared meal. This process discourages use of these tools. By following a Human-Centered Design (HCD) process, Quicksplit prioritizes the end-user's need for speed and simplicity. It solves the "barrier to entry" problem by providing a single-file executable that requires no installation: users simply run the tool and join a shared "room" using a unique ID.

The software utilizes a robust Object-Oriented architecture to manage data integrity while supporting high-performance processing of transaction logs for multiple concurrent users. Key features include:

- Zero-Account Collaboration: Multi-user support through "Cloud Rooms" that sync via a lite REST API, removing the need for traditional authentication.
- Optimized Debt Simplification: An algorithmic "Settle Up" feature that projects transaction data into a Directed Weighted Graph to minimize the number of payments between friends.
- Frictionless Distribution: Delivered as a standalone executable to ensure the tool is accessible to any user, regardless of their technical environment.
- Multi-Currency Support: High-speed currency conversion for international expenses. The system maintains a single "Base Currency" (USD) for the transaction log to ensure data integrity, while allowing users to input expenses in any major currency and view "Settle Up" results in their currency of choice via real-time API integration.
- Needfinding-Driven Analysis: Features such as historical spending trends and leaderboard analytics, derived directly from user interviews and real-world pain points.

## Technical Summary

Here is a technical summary of **QuickSplit** as it stands. This summary consolidates your Human-Centered Design (HCD) goals with the high-performance architectural choices you've made.

---

### **1. Technical Specifications**

* **Environment**: Java-based command-line utility (CLI).
* **Distribution**: Packaged as a **standalone JAR executable** to eliminate installation friction (a core HCD requirement).
* **Persistence**: Supports local file storage (JSON) for offline use and **External REST API integration** (JSONBin.io) for "Cloud Room" synchronization.
* **Architecture**: "DOD-in-an-OOP-Shell"—utilizing flat, contiguous data for performance while adhering to formal Java interfaces for project compliance.

---

### **2. Core Data Structures**

#### **A. The Transaction Log (The Source of Truth)**
* **Structure**: A contiguous `ArrayList` or array of `Transaction` objects.
* **Role**: Acts as the immutable history of every expense. By keeping this "flat," the system maintains high cache locality and can easily be serialized/deserialized for cloud sync.
* **Use Case**: Records the payer, the total amount,  an optional description, and the timestamp. Under the current design, the "participants" are implicitly everyone in the room.

#### **B. Net Balance Map/Array**
* **Structure**: A `Map<Integer, Double>` or a flat `double[]` (if using contiguous User IDs).
* **Role**: An intermediate data representation generated via a linear scan of the transaction log.
* **Use Case**: Stores the final "debt status" of each user (e.g., John: +\$15.00, James: -\$15.00) before the simplification algorithm runs. This is a transient data structure that is used to calculate who owes what.

#### **C. Directed Weighted Graph**
* **Structure**: A graph where vertices are the people in your room.  Edges are the instructions (e.g., "A pays B $10"). Weights are the dollar amounts. 
* **Role**: The "Projection" of the transaction log. It represents the optimized flow of money between group members. This is also a transient data structure that does not get saved in the JSON file.
* **Use Case**: The final output of the "Settle Up" command.


---

### **3. Primary Algorithms**

#### **A. The Accumulation Scan (Log Projection)**

* **Goal**: To calculate the net balance for every person in a room.
* **Process**: A single-pass linear scan ($O(N)$ complexity, where $N$ is the number of transactions). For each entry, it credits the payer and debits all participants by an equal share ($Amount / TotalUsers$).
* **Complexity**: $T(n) = O(n)$; Space $= O(V)$ (where $V$ is the number of vertices/users).

#### **B. Greedy Debt Simplification (Two-Pointer Method)**
* **Goal**: To minimize the total number of transactions required to settle the room. In a group of $V$ people, the algorithm ensures that there are at most $V-1$ transactions, but usually much fewer.
* **Process**: 
    1.  Sort the non-zero balances from most negative to most positive ($O(V \log V)$).
    2.  Use two pointers (Left for biggest debtor, Right for biggest creditor) to match and cancel out debts.
    3.  Generate a graph edge for each match.
* **Complexity**: $T(n) = O(V \log V)$; Space $= O(V)$.


---

### **4. Current Development Status**

| Component | Status | Technical Detail |
| :--- | :--- | :--- |
| **Interfaces** | **Complete** | `Transaction`, `DebtGraph`, and `QuickSplitSystem` are defined. |
| **Data Representation** | **Complete** | Synthetic dataset of 1,000+ entries generated for Grade B/A testing. |
| **Settle-Up Logic** | **In Progress** | Implementing the two-pointer greedy algorithm to project the log onto the `DebtGraph`. |
| **Cloud Sync** | **Planned** | Integrating JSONBin.io for the A+ contract "Cloud Room" feature. |

---

## Questions for Professor:

1. Does every entity need to have an interface, cause that is inefficient/bad practice/bad code. In actuallity, if we wanted to actually write good code, we would have no interfaces because our architecture does not have any inheritance. Everything is its own entity, there are no multiple types of transactions, or quicksplitSystems, or DebtGraph. Just one type of each. DON'T MAKE ME WRITE BAD CODE!
2. Check features, Make sure features are good.
3. The DebtGraph could be removed. It is there because it is a "fancy" graph data structure, but if we want to be realistic, we could do this whole thing with simple lists. What does prof think about this?
