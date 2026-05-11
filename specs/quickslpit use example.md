> new_room
 - new room with id approved-buggle-6
 
> add 10 Stavros food
> add 20 Gavin gas eur
> add 3 Ali gum yen
> settleup usd

 - Stavros owes 1 usd to Gavin
 - Ali owes 8 usd to Gavin

 > save roadrip.json

 When the tool loads it asks for the users name. Then it asks if they want to join a room or create a new one. Joining a room will ask if they want to load from the cloud or a local file. It will also ask for a room id. Then the user is shown available actions in the room once they have joined: add transaction, see log, settle up, save (to file or local depending on how they loaded), join other room, help, exit. The difference is that the add command automatically uses the name provided at the beginnning. It is assumed that each user is adding their own transactions. The currency and description arguments should be optional. If a user wishes to add transactions of another individual they should add an optional user name parameter to the add command.
