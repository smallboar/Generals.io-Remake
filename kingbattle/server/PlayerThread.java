package kingbattle.server;

import kingbattle.util.Constants;
import kingbattle.util.Utils;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class PlayerThread extends Thread{

    private Socket socket;
    private PrintWriter sender;
    GameEngine ge;
    int playerId;

    public PlayerThread(Socket socket, GameEngine ge) {
        this.socket = socket;
        this.ge = ge;

    }


    public void run() {
        try {
            System.out.println("UserThread started");
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            OutputStream output = socket.getOutputStream();
            //sender = new PrintWriter(output, true);
            sender = new PrintWriter(output, true);

            //Read username and send initial board
            System.out.println("Reading username from client");
            Utils.Message loginMessage = Utils.readMessage(reader);
            playerId = ge.addPlayer((String) loginMessage.body, this);
            System.out.println("Username = " + (String) loginMessage.body);
            sendBoard();


            for (int i = 0; i < 1000000; i++) { // used to be a while(true) but made it this so that it could run
                Utils.Message recievedMessage = Utils.readMessage(reader);
                if(recievedMessage != null) {
                    if (recievedMessage.type == Constants.MESSAGE_READY) {
                        ge.setReady((Boolean)recievedMessage.body, playerId);
                        System.out.println("Player " + playerId + " is ready");
                    }
                    if (recievedMessage.type == Constants.MESSAGE_MOVE) {
                        ge.addMove(playerId, (GameEngine.PendingMove) recievedMessage.body);
                    }
                    if (recievedMessage.type == Constants.MESSAGE_DELETE_LAST_MOVE) {
                        ge.deleteLastMove(playerId);
                    }
                    if (recievedMessage.type == Constants.MESSAGE_CLEAR_QUEUE) {
                        ge.clearQueue(playerId);
                    }
                }
            }

            ge.removePlayer(playerId);
            socket.close();

        } catch (Exception ex) {
            System.out.println("Error in UserThread: " + ex.getMessage());
            if(playerId > 0) {
                ge.removePlayer(playerId);
            }
            ex.printStackTrace();
        }

    }



    /**
     * Sends a message to the UserThread.
     */
    void sendBoard() {
        String message = Utils.boardToMessage(ge.getBoardForPlayer(playerId));
        //sender.println(message);
        sender.println(message);

        //System.out.println("Sent map to player " + playerId + " Message length is " + (int)message.charAt(1));

    }

    void sendStart() {
        StringBuilder builder = new StringBuilder();
        builder.append(Constants.MESSAGE_READY);
        builder.append((char)1);
        builder.append((char)1);
        sender.println(builder);
    }

    void sendScoreBoard(int[] scores) {
        String message = Utils.scoreBoardToMessage(scores);
        sender.println(message);
        //System.out.println("Sent scoreboard of " + message);
    }

    public void sendWin(int winnerId) {
        StringBuilder builder = new StringBuilder();
        builder.append(Constants.MESSAGE_WIN);
        builder.append((char) 2); //length 2
        builder.append(winnerId); //winner
        if(playerId == winnerId){
            builder.append((char)1); //this player won
        }
        else {
            builder.append((char) 0); // this player lost
        }
        sender.println(builder);
        System.out.println("Sent winner of " + winnerId);
    }

    void sendPlayerInfo(GameEngine.PlayerInfo p) {
        StringBuilder builder = new StringBuilder();
        builder.append(Constants.MESSAGE_PLAYER_INFO);
        builder.append((char) 1); // remember to change this to 2 when i send last
        if (p.movesQueue.isEmpty()){
            builder.append((char) 0);
        }
        else {
            builder.append((char) p.movesQueue.getFirst().id);
            //builder.append((char) p.movesQueue.getLast().id);
        }
        sender.println(builder);
    }
}
