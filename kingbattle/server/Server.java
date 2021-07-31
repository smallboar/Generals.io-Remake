package kingbattle.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import static kingbattle.util.Constants.PORT_NUMBER;

public class Server {

    private int port;
    //private Set<String> userNames = new HashSet<>();
    private Set<PlayerThread> playerThreads = new HashSet<>();
    GameEngine ge = new GameEngine();


    public Server(int port) {
        this.port = port;
    }

    public void execute() {
        try  {
            ServerSocket serverSocket = new ServerSocket(port);

            System.out.println("Chat Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New user connected");

                PlayerThread newUser = new PlayerThread(socket, ge);
                playerThreads.add(newUser);
                newUser.start();

            }

        } catch (IOException ex) {
            System.out.println("Error in the server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = PORT_NUMBER;
        Server server = new Server(port);
        server.execute();
    }



}
