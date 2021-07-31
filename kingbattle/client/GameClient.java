package kingbattle.client;


import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import static kingbattle.util.Constants.PORT_NUMBER;

public class GameClient {
    private String hostname;
    private int port;
    Sender sender;
    static ClientGraphics ui;

    public GameClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void execute() {
        try {
            Socket socket = new Socket(hostname, port);

            System.out.println("Connected to the chat server");

            new ReceiveThread(socket, ui).start();
            sender = new Sender(socket, this);
            sender.login();
            ui.setSender(sender);

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
        }

    }

    public static void main(String[] args) {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        System.out.println("Java version" + System.getProperty("java.version"));
        String hostname =  "127.0.0.1";
        int port = PORT_NUMBER;
        if(args.length >= 1){
           hostname = args[0];
        }
        if(args.length >= 2){
            port = Integer.parseInt(args[1]);
        }

        GameClient client = new GameClient(hostname, port);
        ui = new ClientGraphics(client);
        System.out.println("Client = " + client);
        client.execute();


    }

}

