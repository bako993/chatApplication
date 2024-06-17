package clients.handlers.fileTransfers.receivedFiles;

import clients.handlers.ClientHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import static communications.Message.*;

public class FileReceiver {
    private final FileAccepter fileAccepter;
    private final FileDecliner fileDecliner;
    private static final String ACCEPT_OPTION = "y";
    private static final String DECLINE_OPTION = "n";
    private final ClientHandler client;
    public FileReceiver(ClientHandler client) {
        this.client = client;
        fileAccepter = new FileAccepter(client);
        fileDecliner = new FileDecliner(client);
    }
    public void handleAcceptOrDecline() throws JsonProcessingException {
        do {
            if (client.getFiles().isEmpty()) {
                System.out.println(MSG_24);
                break;
            }

            System.out.println(MSG_65);
            for (int i = 0; i < client.getFiles().size(); i++) {
                String[] fileParts = client.getFiles().get(i).split(":");
                System.out.println((i + 1) + MSG_66 + fileParts[1] + MSG_67 + fileParts[0]);
            }

            System.out.println(MSG_68);
            String userInput = client.getUserInput(MSG_69);

            int selectedOption;
            try {
                selectedOption = Integer.parseInt(userInput);
            } catch (NumberFormatException e) {
                selectedOption = -1;
            }

            if (selectedOption == 0) {
                break;
            }

            if (selectedOption < 0 || selectedOption > client.getFiles().size()) {
                System.out.println(MSG_28 + " " + client.getFiles().size() + ".\n");
            } else {
                acceptOrDeclineFileMenu(selectedOption - 1);
                break;
            }
        } while (true);
    }
    private void acceptOrDeclineFileMenu(int fileOption) throws JsonProcessingException {
        while (true) {
            String[] fileParts = client.getFiles().get(fileOption).split(":");
            String sender = fileParts[0];
            String filename = fileParts[1];

            System.out.println(MSG_70 + sender);
            System.out.println(MSG_71 + filename +"\n");

            System.out.println(MSG_72);
            System.out.println(MSG_73 + "(" + ACCEPT_OPTION + ")");
            System.out.println(MSG_74 + "(" + DECLINE_OPTION + ")");
            String choice = client.getUserInput(MSG_69).toLowerCase();

            switch (choice) {
                case ACCEPT_OPTION -> {
                    fileAccepter.acceptFileTransfer(fileOption, sender, filename);
                    return;
                }
                case DECLINE_OPTION -> {
                    fileDecliner.declineFileTransfer(fileOption, sender, filename);
                    return;
                }
                default -> System.out.println(MSG_75 + ACCEPT_OPTION + MSG_76 + DECLINE_OPTION  + "\"\n");
            }
        }
    }
}
