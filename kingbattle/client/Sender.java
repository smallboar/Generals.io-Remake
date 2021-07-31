package kingbattle.client;

import kingbattle.server.GameEngine;
import kingbattle.util.Constants;

import java.io.*;
import java.net.Socket;


public class Sender {
    private PrintWriter sender;
    private Socket socket;
    private GameClient client;
    final static String assetPath = "C:\\Users\\Willi\\IdeaProjects\\Summer\\assets\\";
    boolean isImage = false;


    public Sender(Socket socket, GameClient client) {
        this.socket = socket;
        this.client = client;
        System.out.println("client = " + client + "  line 25 writethread");

        try {
            OutputStream output = socket.getOutputStream();
            sender = new PrintWriter(output, true);
        } catch (IOException ex) {
            System.out.println("Error getting output stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void login() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String userName = "username";
            sender.println(getLoginMessage(userName));
            System.out.println("Sent login to server");




           // socket.close();
        } catch (IOException ex) {

            System.out.println("Error writing to server: " + ex.getMessage());
        }
    }

    public String getLoginMessage(String username) {
        StringBuilder builder = new StringBuilder();
        builder.append(Constants.MESSAGE_LOGIN);
        builder.append((char) username.length());
        builder.append(username);
        return builder.toString();
    }

    public void sendMove(GameEngine.PendingMove pm) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append(Constants.MESSAGE_MOVE);
        builder.append((char)5);
        builder.append((char)pm.id);
        builder.append((char)pm.x);
        builder.append((char)pm.y);
        builder.append((char)pm.dir);
        builder.append((char)pm.movePercentage);
        //System.out.println("Sender sent clicks of " + pm.movePercentage);
        //System.out.println("Sent move of X = " + pm.x + " Y = " + pm.y + " dir = " + pm.dir);
        sender.println(builder);
    }

    public void sendReadyState(boolean isReady) throws IOException {
        //System.out.println("Sending Ready=" + isReady);
        StringBuilder builder = new StringBuilder();
        builder.append(Constants.MESSAGE_READY);
        builder.append((char)1);
        builder.append((char) (isReady ? 1 : 0));
        sender.println(builder);
    }

    public void sendDeleteMove() throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append(Constants.MESSAGE_DELETE_LAST_MOVE);
        builder.append((char)0);
        sender.println(builder);
    }

    public void sendClearQueue() throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append(Constants.MESSAGE_CLEAR_QUEUE);
        builder.append((char)0);
        sender.println(builder);
    }


}
