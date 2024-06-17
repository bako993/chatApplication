package clients.handlers;

import clients.handlers.broadcasts.BroadcastReader;
import clients.handlers.fileTransfers.FileTransferReader;
import clients.handlers.guessingGames.GuessingGameReader;
import clients.handlers.logins.JoinedReader;
import clients.handlers.logins.LoginReader;
import clients.handlers.logoffs.ByeReader;
import clients.handlers.logoffs.LeftReader;
import clients.handlers.parseErrors.ParseErrorReader;
import clients.handlers.pingPongs.DscnReader;
import clients.handlers.pingPongs.PongErrorReader;
import clients.handlers.pingPongs.PingReader;
import clients.handlers.privateChats.PrivateChatReader;
import clients.handlers.onlineUsers.OnlineUserReader;
import clients.handlers.unknownCommands.UnknownCommandReader;
import clients.handlers.welcomes.WelcomeReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.*;
import java.net.Socket;
import static communications.Command.*;
import static communications.Message.MSG_13;

public class ReaderHandler extends Thread {
    private final ClientHandler clientHandler;
    private final BufferedReader reader;
    private String response;
    private final Socket socket;
    public ReaderHandler(Socket socket, ClientHandler clientHandler) {
        try {
            this.socket = socket;
            this.clientHandler = clientHandler;
            InputStream inputStream = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void run() {
        try {
            while ((response = reader.readLine()) != null) {
                handleResponse(response);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.err.println(MSG_13);
        } finally {
            closeResources();
        }
    }
    private void closeResources() {
        try {
            reader.close();
            socket.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    public String getResponse() {
        return response;
    }
    private void handleResponse(String response) throws JsonProcessingException {
        String[] responseParts = response.split(" ", 2);
        String command = responseParts[0];
        switch (command) {
            case WELCOME -> new WelcomeReader().readResponse(response);
            case LOGIN_RESP -> new LoginReader(clientHandler).readResponse(response);
            case JOINED -> new JoinedReader(clientHandler).readResponse(response);
            case LEFT -> new LeftReader().readResponse(response);
            case PING -> new PingReader(clientHandler).readResponse(response);
            case DSCN -> new DscnReader().readResponse(response);
            case PONG_ERROR -> new PongErrorReader().readResponse(response);
            case BYE_RESP -> new ByeReader().readResponse(response);
            case PARSE_ERROR -> new ParseErrorReader().readResponse(response);
            case ONLINE_USERS,
                 ONLINE_USERS_RESP -> new OnlineUserReader().readResponse(response);
            case BROADCAST,
                BROADCAST_RESP -> new BroadcastReader().readResponse(response);
            case PRIVATE,
                 PRIVATE_RESP -> new PrivateChatReader().readResponse(response);
            case FILE_TRANSFER,
                 FILE_TRANSFER_RESP,
                 ACCEPT_FILE_TRANSFER,
                 ACCEPT_FILE_TRANSFER_RESP,
                 DECLINE_FILE_TRANSFER,
                 DECLINE_FILE_TRANSFER_RESP -> new FileTransferReader(clientHandler).readResponse(response);
            case GUESSING_GAME_INVITE,
                 GUESS_RESP,
                 GUESSING_GAME_INVITE_RESP,
                 GUESSING_GAME_JOIN_RESP,
                 GUESSING_GAME_RESULT -> new GuessingGameReader(clientHandler).readResponse(response);
            default -> new UnknownCommandReader().readResponse(response);
        }
    }
}
