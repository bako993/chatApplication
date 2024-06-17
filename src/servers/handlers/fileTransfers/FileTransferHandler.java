package servers.handlers.fileTransfers;

import servers.handlers.ServerHandler;
import static communications.Command.*;

public class FileTransferHandler {
    private final ServerHandler serverHandler;
    public FileTransferHandler(ServerHandler serverHandler) {

        this.serverHandler = serverHandler;
    }
    public void handleFileTransfer(String response) {
        String[] responseParts = response.split(" ");
        String command = responseParts[0];

        switch (command) {
            case FILE_TRANSFER_REQ -> new FileTransferRequestHandler(serverHandler).handleFileTransferRequest(response);
            case ACCEPT_FILE_TRANSFER_REQ -> new AcceptFileHandler(serverHandler).handleAcceptFileTransfer(response);
            case DECLINE_FILE_TRANSFER_REQ -> new DeclineFileHandler(serverHandler).handleDeclineFileTransfer(response);
        }
    }
}
