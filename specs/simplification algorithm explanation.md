This is the algorithm described in [[Design Brief#**B. Greedy Debt Simplification (Two-Pointer Method)**]]

Think of it like matching the person who is "most broke" with the person who is "owed the most."

 Step 1: Sort the balances
 We take the results of your Accumulation Scan and sort them from most negative to most positive:
 
 \[David: -\$8, Charlie: -\$7, Bob: +\$5, Alice: +\$10\]

 Step 2: Use two pointers
 - Left Pointer: At the biggest debtor (David: -\$8)
 - Right Pointer: At the biggest creditor (Alice: +\$10)

 Step 3: Match them (Greedy Choice)
 We look at David and Alice. David owes \$8, and Alice is owed \$10.
 - Action: David pays Alice \$8.
 - Result: David's balance is now \$0 (he's done!). Alice's balance is now +\$2 (she's still owed \$2).
 - Graph: We add an edge to our DebtGraph: David -> Alice (\$8).

 Step 4: Repeat until everyone is at \$0
 Now our list looks like this:
 [Charlie: -\$7, Bob: +\$5, Alice: +\$2] (David is gone)

 - Next Match: Charlie (-\$7) and Bob (+\$5).
 - Action: Charlie pays Bob $5.
 - Result: Bob is now \$0. Charlie still owes \$2.
 - Graph: Add edge Charlie -> Bob ($5).
 - Final Match: Charlie (-\$2) and Alice (+\$2).
 - Action: Charlie pays Alice \$2.
 - Graph: Add edge Charlie -> Alice (\$2).
