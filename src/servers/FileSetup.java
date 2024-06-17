package servers;

import servers.handlers.fileTransfers.FileHandler;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import static communications.Message.MSG_90;

public class FileSetup implements Runnable{

    private final int serverPort;
    private final Map<String, FileHandler> users;

    public FileSetup(int serverPort) {
        this.serverPort = serverPort;
        users = new HashMap<>();
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);

            while(true) {
                Socket clientSocket = serverSocket.accept();

                FileHandler fileHandler = new FileHandler(this,clientSocket);
                fileHandler.start();
                System.out.println(MSG_90 + serverPort);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    public Map<String, FileHandler> getUsers() {
        return users;
    }
    public void addUser(FileHandler fileHandler) {
        users.put(fileHandler.getUsername(),fileHandler);
    }
    public void removeUser(FileHandler fileHandler) {
        users.remove(fileHandler.getUsername());
    }
}
