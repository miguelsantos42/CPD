# CPD Projects

CPD Projects of group T04G14.

Group members:
1. Miguel Santos(up202008450@up.pt)
2. Rafael Cerqueira(up201910200@up.pt)


# Run Code
- To run the code you need to compile all the files using `javac *.java`
- Then you need to use:
    - `java GameServer 8000` for the Server side
    - `java GameClient localhost 8000` for the Client side

# Game Description
The game implemented has the following logic:
- It's a 1v1.
- The server chooses a number from 1 to 100.
- The player has to try to guess the number by turn and whoever guesses the number first wins.
- Has the player gives hints, the server responds with "Almost there!" if player is within 5 numbers distance, "Close!" if its within 15 and "Far!".
- If a player disconnects from a game it has 10s to reconnect.
- Multiple games can be played.
- When game ends a player can relogin to play again.




