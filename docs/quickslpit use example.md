```text
=== Welcome to QuickSplit ===
Enter your name: Stavros

--- Room Manager ---
 1. Create a new room
 2. Join a room
Selection (1/2): 1
 - New room created and joined: speedy-badger-42

[Stavros @ speedy-badger-42 (Local)] > add 10 food
Logged: Payer: Stavros, Description: food, Amount: 10.00 USD

[Stavros @ speedy-badger-42 (Local)] > add 20 gas eur Gavin
Logged: Payer: Gavin, Description: gas, Amount: 21.65 USD

[Stavros @ speedy-badger-42 (Local)] > add 3 gum jpy Ali
Logged: Payer: Ali, Description: gum, Amount: 0.02 USD

[Stavros @ speedy-badger-42 (Local)] > log

--- Transaction Log for [speedy-badger-42] ---
 - Stavros spent 10.00 USD on 'food'
 - Gavin spent 21.65 USD on 'gas'
 - Ali spent 0.02 USD on 'gum'

[Stavros @ speedy-badger-42 (Local)] > settleup usd

--- Settlement Plan (USD) ---
 - Ali pays Gavin 10.54 USD
 - Stavros pays Gavin 0.56 USD

[Stavros @ speedy-badger-42 (Local)] > save

You haven't specified where to save your data yet.
 1. Local file
 2. Cloud (JSONBin)
Selection (1/2): 1
Enter filename to save to: roadtrip.json
 - Saving all rooms locally...
 - Local Save Successful.

[Stavros @ speedy-badger-42 (Local)] > exit
Goodbye, Stavros!
```

## join local room

```text
=== Welcome to QuickSplit ===
Enter your name: Stavros

--- Room Manager ---
 1. Create a new room
 2. Join a room
Selection (1/2): 2

Where should we look for rooms?
 1. A local file (.json)
 2. The Cloud (JSONBin)
Selection (1/2): 1
Enter filename (e.g., trips.json): roadtrip.json
 - File loaded.

Available Rooms in Memory:
 1. speedy-badger-42
 2. Load a different file/source
Selection: 1
 - Joined: speedy-badger-42

[Stavros @ speedy-badger-42 (Local)] > 
```

## cloud flow

```text
=== Welcome to QuickSplit ===
Enter your name: Gavin

--- Room Manager ---
 1. Create a new room
 2. Join a room
Selection (1/2): 2

Where should we look for rooms?
 1. A local file (.json)
 2. The Cloud (JSONBin)
Selection (1/2): 2
Enter Room ID to fetch from Cloud: speedy-badger-42
 - Fetching room from cloud...
 - Successfully joined cloud room: speedy-badger-42

[Gavin @ speedy-badger-42 (Cloud)] > add 15 snacks
Logged: Payer: Gavin, Description: snacks, Amount: 15.00 USD

[Gavin @ speedy-badger-42 (Cloud)] > sync
 - Fetching latest cloud data for safe merge...
 - Pushing merged vault back to cloud...
 - Cloud Sync Successful.
```
