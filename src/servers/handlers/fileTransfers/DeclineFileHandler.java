package servers.handlers.fileTransfers;

import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.DeclineFileTransfer;
import communications.protocol.messages.DeclineFileTransferReq;
import communications.protocol.messages.DeclineFileTransferResp;
import communications.protocol.utils.Utils;
import servers.handlers.ResponseHandler;
import servers.handlers.ServerHandler;
import static communications.ErrorCode.CODE_0000;
import static communications.Status.DECLINED;
import static communications.Status.OK;

public class DeclineFileHandler extends ResponseHandler {
    private final ServerHandler serverHandler;
    public DeclineFileHandler(ServerHandler serverHandler) {

        this.serverHandler = serverHandler;
    }
    public void handleDeclineFileTransfer(String response) {
        System.out.println(response);

        try {
            DeclineFileTransferReq declineFileTransferReq = Utils.messageToObject(response);
            String sender = declineFileTransferReq.sender();
            String filename = declineFileTransferReq.filename();

            if (serverHandler.getServer().getUsers().containsKey(sender)) {
                sendResp();
                sendAcceptOrDeclineResp(sender, filename);
            } else {
                sendResp();
            }

            serverHandler.getFiles().remove(sender + ":" + filename);

        } catch (JsonProcessingException e) {
            serverHandler.handleInvalidJsonFormat();
        }
    }
    @Override
    public void sendResp() throws JsonProcessingException {
        DeclineFileTransferResp declineFileTransferResp = new DeclineFileTransferResp(OK, CODE_0000);
        String declineFileTransferRespMessage = Utils.objectToMessage(declineFileTransferResp);
        serverHandler.printAndSendResponse(declineFileTransferRespMessage);
    }
    @Override
    public void sendAcceptOrDeclineResp(String sender, String filename) throws JsonProcessingException {
        DeclineFileTransfer declineFileTransfer = new DeclineFileTransfer(DECLINED, serverHandler.getUsername(), filename);
        String declineFileTransferMessage = Utils.objectToMessage(declineFileTransfer);
        serverHandler.getServer().getUsers().get(sender).printAndSendResponse(declineFileTransferMessage);
    }
}
