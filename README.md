# Generals.io-Copy
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
