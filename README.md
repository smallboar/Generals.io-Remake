# Generals.io-Copy
Video Demo and Explanation: https://www.youtube.com/watch?v=_yGWeTqExs4

<img width="1434" alt="image" src="https://github.com/smallboar/Generals.io-Copy/assets/56139007/f23c5462-f272-4ce4-85ad-affada411049">

This a 2-8 player game that starts out with a randomized map with different mountains (obstacles) and cities (spawns 1 army every tick). This is a turn based game, every tick players will get
one turn. Players start out with their king, which generates one army at every tick, and can move their soldiers off of each cell that they have control over. To control more grids, the player
can move their army onto an empty cell, and if they will leave 1 army behind them to occupy the land they moved off of. Every 25 ticks, every piece of land the player owns will increase by
one army, to encourage users to expand their land control. Players can occupy cities by spending 40-50 army when they find one. These act the same as a king, spawning 1 army per tick. These
cities are hidden in the fog of war, cities and mountains both appear as mountains with a question mark, until your army gets to being 1 cell away from it. Players can capture other players 
lands and win the game by finding and capturing the other players kings. They must have more than the enemy's king does by 2, as they must leave 1 army to occupy the cell they are leaving, 
and also 1 to occupy the king after the "war". Once the king is occupied, it turns into a city for the capturer, and all of that player's land is given to the capturer, except all the armys
are halved (think of it as some rebel and desert). At the end, when one player captures everyone else, they will win. 



Just a small project that I did to practice using sockets, code for both the server and the client is included. In total it was ~1750 lines of code.

Just a disclaimer, I don't own any of the ideas/mechanics of the game, I copied it directly from generals.io, I just used this as a fun project to work on over the summer.

To run, open command prompt and cd to whichever directory you put the folders in (so if I had them in downloads, I would type this in
cmd prompt: cd Downloads\generals

Make sure that capitalization is correct, once the directory is switched to kingbattle, then you can just copy paste a run command.

To run the server, paste this (also make sure you are port forwarding correctly):
java -Dfile.encoding=UTF-8 generals.server.Server





To run the client/game, paste this (needs a server running beforehand):
java -Dfile.encoding=UTF-8 generals.client.GameClient [IP adress of server] [Port number]

So for my server, I used port 8357, and let's say my IP is 12.34.567.89, I would do this:
java -Dfile.encoding=UTF-8 generals.client.GameClient 12.34.567.89 8357



NOTE:This needs Java 16 or above to run, so make sure you have that downloaded AND put it in Path for your environment variables. If you just downloaded it,
you'll also have to restart your command prompt for it to update. You can check your java version (if you have it installed and in path) using this
command in command prompt:
java -version
