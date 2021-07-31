package kingbattle.client;

import kingbattle.server.GameEngine;
import kingbattle.util.Constants;
import kingbattle.util.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;


public class ReceiveThread extends Thread {
    private BufferedReader reader;
    private Socket socket;
    private ClientGraphics ui;

    public ReceiveThread(Socket socket, ClientGraphics ui) {
        this.socket = socket;
        this.ui = ui;

        try {
            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        } catch (IOException ex) {
            System.out.println("Error getting input stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        try {
            while (true) {
                Utils.Message msg = Utils.readMessage(reader);
                if (msg != null) {
                    //System.out.println("Received message type = " +  msg.type);
                    if (msg.type == Constants.MESSAGE_BOARD) {
                        ui.updateMap((GameEngine.Cell[][]) msg.body);
                        //Utils.printBoard((GameEngine.Cell[][]) msg.body);
                    }
                    if (msg.type == Constants.MESSAGE_READY) {
                        ui.startGame();
                        //System.out.println("Recieve thread started game");
                    }
                    if (msg.type == Constants.MESSAGE_SCOREBOARD) {
                        //System.out.println("Recieve thread recieved scoreboard");
                        ui.updateScore((ArrayList<GameEngine.Scores>) msg.body);
                    }
                    if (msg.type == Constants.MESSAGE_WIN) {
                        ui.endGame((int[]) msg.body);
                    }
                    if (msg.type == Constants.MESSAGE_PLAYER_INFO) {
                        ui.setPlayerInfo((Integer) msg.body);
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
