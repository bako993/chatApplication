package clients.handlers;

import clients.ClientSetup;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class ClientHandler {
    private final ClientSetup clientSetup;
    private final List<String> files;
    private final Scanner scanner = new Scanner(System.in);
    private String username;
    private boolean loggedIn = false;
    public ClientHandler(ClientSetup clientSetup) {
        this.clientSetup = clientSetup;
        files = new ArrayList<>();
    }
    public List<String> getFiles() {
        return files;
    }
    public void addFile(String fileString) {
        if (!files.contains(fileString)) {
            files.add(fileString);
        }
    }
    public void removeFile(int filePosition) {
        files.remove(filePosition);
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public boolean isLoggedIn() {
        return loggedIn;
    }
    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }
    public boolean isConnected() {
        return clientSetup.getSocket() != null && clientSetup.getSocket().isConnected();
    }
    public boolean isServerDisconnected() {
        return !isConnected() || (clientSetup.getReaderHandler() != null && !clientSetup.getReaderHandler().isAlive());
    }
    public void send(String command) {
        clientSetup.getSenderHandler().sendCommand(command);
    }
    public String getUserInput(String prompt) {
        System.out.println(prompt);
        return scanner.nextLine();
    }
}
