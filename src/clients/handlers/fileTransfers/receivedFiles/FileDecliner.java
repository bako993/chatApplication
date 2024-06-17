package clients.handlers.fileTransfers.receivedFiles;

import clients.handlers.ClientHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.DeclineFileTransferReq;
import communications.protocol.utils.Utils;

public class FileDecliner  {
    private final ClientHandler client;
    public FileDecliner(ClientHandler client) {
        this.client = client;
    }
    public void declineFileTransfer(int fileOption, String sender, String filename) throws JsonProcessingException {
        DeclineFileTransferReq declineFileTransferReq = new DeclineFileTransferReq(sender,filename);
        String sendFileDeclineReq = Utils.objectToMessage(declineFileTransferReq);

        client.send(sendFileDeclineReq);
        client.removeFile(fileOption);
    }
}
