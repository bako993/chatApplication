package clients.handlers.fileTransfers.receivedFiles;

import clients.handlers.ClientHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.AcceptFileTransferReq;
import communications.protocol.utils.Utils;

public class FileAccepter {
    private final ClientHandler client;
    public FileAccepter(ClientHandler client) {
        this.client = client;
    }
    public void acceptFileTransfer(int fileOption, String sender, String filename) throws JsonProcessingException {
        AcceptFileTransferReq acceptFileTransferReq = new AcceptFileTransferReq(sender,filename);
        String sendfileAcceptReq = Utils.objectToMessage(acceptFileTransferReq);

        client.send(sendfileAcceptReq);
        client.removeFile(fileOption);
    }
}
