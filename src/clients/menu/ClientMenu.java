package clients.menu;

import clients.handlers.ClientHandler;
import clients.handlers.broadcasts.BroadcastSender;
import clients.handlers.fileTransfers.FileTransferSender;
import clients.handlers.fileTransfers.receivedFiles.FileDecisionSender;
import clients.handlers.fileTransfers.receivedFiles.FileReceiver;
import clients.handlers.guessingGames.*;
import clients.handlers.logins.LoginSender;
import clients.handlers.logoffs.LogoffSender;
import clients.handlers.onlineUsers.OnlineUserSender;
import clients.handlers.privateChats.PrivateChatSender;
import clients.handlers.unknownCommands.UnknownCommandSender;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.FileNotFoundException;
import java.util.Scanner;
import static communications.Message.*;

public class ClientMenu {
    private final ClientHandler client;
    private final Scanner scanner = new Scanner(System.in);
    public ClientMenu(ClientHandler client) {
        this.client = client;
    }
    public void run() {
        try {
            while (client.isConnected()) {
                String userChoice = getUserChoice();
                handleMenuOption(userChoice);
            }
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        } catch (FileNotFoundException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleMenuOption(String option) throws FileNotFoundException, JsonProcessingException {
        if (client.isServerDisconnected()) {
            System.err.println(MSG_17);
            return;
        }
        MenuOption[] menuOptions = MenuOption.values();

        for (MenuOption menuOption : menuOptions) {
            if (menuOption.getCode().equalsIgnoreCase(option)) {
                switch (menuOption) {
                    case USER_LOGIN -> new LoginSender(client).sendRequest();
                    case VIEW_ONLINE_USERS -> new OnlineUserSender(client).sendRequest();
                    case BROADCAST_MESSAGE -> new BroadcastSender(client).sendRequest();
                    case PRIVATE_MESSAGE -> new PrivateChatSender(client).sendRequest();
                    case SEND_FILE -> new FileTransferSender(client).sendRequest();
                    case VIEW_RECEIVED_FILES -> viewReceivedFiles();
                    case EXIT -> new LogoffSender(client).sendRequest();
                    case REQUEST_GUESSING_GAME -> new GuessingGameInviteSender(client).sendRequest();
                    case JOIN_GUESSING_GAME -> new GuessingGameJoinSender(client).sendRequest();
                    case PLAY_GUESSING_GAME -> new GuessingGameSender(client).sendRequest();
                    case HELP -> printCommands();
                }
                return;
            }
        }
        new UnknownCommandSender(client,option).sendRequest();
    }
    private void viewReceivedFiles() throws JsonProcessingException {
        FileReceiver fileReceiver = new FileReceiver(client);
        new FileDecisionSender(fileReceiver).sendRequest();
    }
    private void printCommands() {
        System.out.println(MSG_87);
        for (MenuOption option : MenuOption.values()) {
            System.out.println(option.getCode() + ". " + option.getDescription());
        }
        System.out.println("-------------------------------------------\n");
        System.out.println(MSG_88 + MenuOption.values().length + "): ");
    }
    private String getUserChoice() {
        return scanner.nextLine().trim();
    }
}
