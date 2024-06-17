package servers.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import static communications.Command.*;
import static communications.Message.MSG_89;
import static communications.Message.MSG_93;

import servers.ServerSetup;
import servers.handlers.broadcasts.BroadcastHandler;
import servers.handlers.fileTransfers.FileTransferHandler;
import servers.handlers.guessingGames.GuessingGameHandler;
import servers.handlers.logins.LoginHandler;
import servers.handlers.logoffs.LogoffHandler;
import servers.handlers.onlineUsers.OnlineUserHandler;
import servers.handlers.parseErrors.ParseErrorHandler;
import servers.handlers.pingPongs.PingPongHandler;
import servers.handlers.privateChats.PrivateChatHandler;
import servers.handlers.unknownCommands.UnknownCommandHandler;
import servers.handlers.welcomes.WelcomeHandler;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerHandler extends Thread{
    private final ServerSetup server;
    private final Socket clientSocket;
    private PrintWriter writer;
    private BufferedReader reader;
    private String username;
    private String response;
    private final List<String> files;
    private final PingPongHandler pingPongHandler;
    public ServerHandler(ServerSetup server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
        pingPongHandler = new PingPongHandler(this);
        initializeStreams();
        files = new ArrayList<>();
    }

    private void initializeStreams() {
        try {
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();

            writer = new PrintWriter(outputStream, true);
            reader = new BufferedReader(new InputStreamReader(inputStream));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }


    @Override
    public void run() {
        try {
            new WelcomeHandler(this).welcomeMessage();
            handleClientSocket();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    public void closeClientConnection() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.err.println(MSG_93 + e.getMessage());
        }
    }

    private void handleClientSocket() throws JsonProcessingException {
        try {
            while ((response = reader.readLine()) != null) {
                String[] responseParts = response.split(" ", 2);
                handleCommands(responseParts);
            }
            server.removeUser(this);
            closeClientConnection();
        } catch (IOException e) {
            handleIOException(e);
        }
    }
    private void handleIOException(IOException e) {
        server.removeUser(this);
        closeClientConnection();
        System.err.println(MSG_89 + e.getMessage());
    }

    private void handleCommands(String[] responseParts) throws JsonProcessingException {
        String command = responseParts[0];

        switch (command) {
            case LOGIN -> new LoginHandler(this,pingPongHandler).handleLogin(response);
            case ONLINE_USERS_REQ -> new OnlineUserHandler(this).displayOnlineUsers(response);
            case PONG -> pingPongHandler.handlePongCommand(response);
            case BROADCAST_REQ -> new BroadcastHandler(this).handleBroadcastChat(this,response);
            case PRIVATE_REQ -> new PrivateChatHandler(this).handlePrivateChat(response);
            case FILE_TRANSFER_REQ, ACCEPT_FILE_TRANSFER_REQ, DECLINE_FILE_TRANSFER_REQ -> new FileTransferHandler(this).handleFileTransfer(response);
            case GUESSING_GAME_INVITE_REQ, GUESSING_GAME_JOIN_REQ, GUESS_REQ -> handleGuessingGame();
            case BYE -> new LogoffHandler(this).Logoff(response);
            default -> new UnknownCommandHandler(this).unknownCommand();
        }
    }
    private void handleGuessingGame() throws JsonProcessingException {
        GuessingGameHandler guessingGameHandler = GuessingGameHandler.getInstance(this);
        guessingGameHandler.handleGuessingGame(response);
    }

    public boolean checkIfUserLoggedIn() {
        return username != null;
    }
    public void addFile(String fileString) {
        if (!files.contains(fileString)) {
            files.add(fileString);
        }
    }
    public List<String> getFiles() {
        return files;
    }
    public Socket getClientSocket() {
        return clientSocket;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getResponse() {
        return response;
    }
    public ServerSetup getServer() {
        return server;
    }
    public void printAndSendResponse(String message) {
        System.out.println(message);
        writer.println(message);
    }
    public void printAndSendResponse(ServerHandler sender, String message) {
        System.out.println(message);
        sender.writer.println(message);
    }
    public void handleInvalidJsonFormat() {
        new ParseErrorHandler(this).handleParseError();
    }
}