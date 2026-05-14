# QuickSplit: Architecture & Performance Report

This document outlines the software architecture, design patterns, and algorithmic performance characteristics of **QuickSplit**, a lightweight, human-centered command-line utility for expense sharing and debt simplification.

## System Architecture (Clean Architecture / MVC)

QuickSplit is built on a strict separation of concerns, ensuring that domain logic, user interaction, and data persistence are fully decoupled. The system avoids tightly coupled "God Objects" by implementing a variation of the Model-View-Controller (MVC) pattern.

### The Engine (`QuickSplitSystem` & `Room`)
The core domain model consists of the `QuickSplitSystem`, `Room`, `Transaction`, and `Settlement` classes. 
* These classes contain absolutely no file I/O, networking, or JSON serialization logic. 
* `QuickSplitSystem` acts as the session manager, coordinating which `Room` is currently active.

### The Controller (`QuickSplitCLI`)
The `QuickSplitCLI` manages the interactive shell. It is responsible for user prompting, session caching (mapping friendly names to UUIDs), and formatting output. It acts as the bridge between the user, the `QuickSplitSystem` engine, and the persistence layer.

### The Data Boundary (`StorageService`)
Data persistence is abstracted behind the `StorageService` interface using the **Strategy Pattern**. This allows the CLI to seamlessly swap between storage mediums without changing its core logic.
* `LocalStorageService`: Handles standard `.json` file I/O using Java `nio`.
* `CloudStorageService`: Implements a safe, bidirectional synchronization flow using the `HttpClient` to communicate with JSONBin.io.

### Serialization (`CustomJsonSerializer`)
Instead of relying on reflection-based libraries (like Gson or Jackson) which introduce "magic" and heavy dependencies, QuickSplit uses a purpose-built `CustomJsonSerializer` powered by the minimal `mjson` library. This guarantees absolute control over the JSON schema and produces highly compact, predictable output.

---

## Algorithmic Performance & Optimization

QuickSplit integrates some Data-Oriented Design (DOD) principles along with its OOP base, opting for flat data structures and contiguous memory over heavily nested Object-Oriented "wrapper" structures.

### The Accumulation Scan: $O(N)$
To calculate who owes what, the system projects the flat `List<Transaction>` into a temporary net balance map. 
* **Time Complexity:** $O(N)$ where $N$ is the number of transactions. The system iterates through the log exactly once in a single linear scan.
* **Space Complexity:** $O(V)$ where $V$ is the number of users (vertices), as we only store a running double for each participant.

### Greedy Debt Simplification: $O(V \log V)$
Instead of mapping debts to an explicit Directed Weighted Graph data structure (which introduces heavy pointer overhead and cache misses), the system optimizes settlement using the **Two-Pointer Method**.
1. **Partitioning:** Users are divided into "Debtors" (negative balance) and "Creditors" (positive balance).
2. **Sorting:** Both lists are sorted by absolute value magnitude. Complexity: $O(V \log V)$.
3. **Matching (Two-Pointer):** The largest debtor pays the largest creditor. Because at least one balance is reduced to zero in every step, this resolves in at most $V - 1$ transactions. Complexity: $O(V)$.

**Overall Settle-Up Complexity:** $O(N + V \log V)$. Because $N$ (transactions) is typically much larger than $V$ (users), this algorithm is highly performant and cache-friendly.

### Cloud Sync Merge: $O(T)$
JSONBin.io requires updating the entire JSON payload at once. To prevent data loss when multiple users sync simultaneously, QuickSplit implements an intelligent merge.
* The system loads the cloud's transaction log and inserts the `transactionId`s (UUIDs) into a `HashSet`.
* It then scans the local transactions and appends any that are missing from the Set.
* **Time Complexity:** $O(T_c + T_l)$ where $T_c$ is cloud transactions and $T_l$ is local transactions. The $O(1)$ lookup time of the `HashSet` guarantees fast, collision-free merging.

---

## Data Integrity & Distributed Systems Support

To support the "Cloud Room" feature where multiple instances of QuickSplit operate independently, the system employs **Universally Unique Identifiers (UUIDs)**.

* **Collision Prevention:** If two users add a friend offline, integer-based IDs (`1`, `2`) would collide during sync. By using UUIDs (`f47ac10b-58cc...`), QuickSplit guarantees global uniqueness.
* **Human-Centered Abstraction:** The user never sees the UUID. The `QuickSplitCLI` maintains a local `nameToId` cache, allowing the user to simply type `add 10 Stavros`, while the system maps "Stavros" to the correct UUID for background processing.

## Summary

By discarding bloated graph representations and reflection-based serialization, QuickSplit achieves a highly optimized runtime footprint. The combination of $O(V \log V)$ greedy simplification, isolated MVC boundaries, and distributed-safe merge algorithms makes it an enterprise-grade utility masked inside a lightweight, zero-dependency executable.
