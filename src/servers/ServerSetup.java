package servers;

import servers.handlers.ServerHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static communications.Message.MSG_91;
import static communications.Message.MSG_92;

public class ServerSetup {
    private final int serverPort;
    private final Map<String, ServerHandler> users;
    public ServerSetup(int serverPort) {
        this.serverPort = serverPort;
        users =  new HashMap<>();
    }
    public void startServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            System.out.println(MSG_91 + serverPort);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ServerHandler serverHandler = new ServerHandler(this, clientSocket);
                serverHandler.start();
            }
        } catch (IOException e) {
            System.err.println(MSG_92 + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
    public Map<String, ServerHandler> getUsers() {
        return users;
    }
    public void addUser(ServerHandler user) {
        users.put(user.getUsername(),user);
    }
    public void removeUser(ServerHandler user) {
        users.remove(user.getUsername(),user);
    }
}
