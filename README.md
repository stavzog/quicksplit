# Quicksplit

Quicksplit is a lightweight, human-centered command-line utility for splitting group epenses. It is designed to eliminate the friction and "social overhead" that is associated with modern expense-sharing applications. Traditional tools frequently require users to download heavy mobile apps, create accounts, and navigate paywalls just to log a simple shared meal. This process discourages use of these tools. By following a Human-Centered Design (HCD) process, Quicksplit prioritizes the end-user's need for speed and simplicity. It solves the "barrier to entry" problem by providing a single-file executable that requires no installation: users simply run the tool and join a shared "room" using a unique ID.

The software utilizes a robust Object-Oriented architecture to manage data integrity while supporting high-performance processing of transaction logs for multiple concurrent users. Key features include:

- Zero-Account Collaboration: Multi-user support through "Cloud Rooms" that sync via a lite REST API, removing the need for traditional authentication.
- Optimized Debt Simplification: An algorithmic "Settle Up" feature that projects transaction data into a Directed Weighted Graph to minimize the number of payments between friends.
- Frictionless Distribution: Delivered as a standalone executable to ensure the tool is accessible to any user, regardless of their technical environment.
- Needfinding-Driven Analysis: Features such as historical spending trends and leaderboard analytics, derived directly from user interviews and real-world pain points.
