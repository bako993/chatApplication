package servers.handlers.fileTransfers;

import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.AcceptFileTransfer;
import communications.protocol.messages.AcceptFileTransferReq;
import communications.protocol.messages.AcceptFileTransferResp;
import communications.protocol.utils.Utils;
import servers.handlers.ResponseHandler;
import servers.handlers.ServerHandler;
import static communications.ErrorCode.*;
import static communications.Status.*;
import static communications.Status.ERROR;

public class AcceptFileHandler extends ResponseHandler {

    private final ServerHandler serverHandler;
    public AcceptFileHandler( ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }
    public void handleAcceptFileTransfer(String response) {
        try {
            AcceptFileTransferReq acceptFileTransferReq = Utils.messageToObject(response);
            String sender = acceptFileTransferReq.sender();

            if (serverHandler.getServer().getUsers().containsKey(sender)) {
                handleAccept(response);
            } else {
                System.out.println(response);
                sendResp(ERROR, CODE_9000);
            }
        } catch (JsonProcessingException e) {
            serverHandler.handleInvalidJsonFormat();
        }
    }
    private void handleAccept(String response) throws JsonProcessingException {
        System.out.println(response);

        AcceptFileTransferReq acceptFileTransferReq = Utils.messageToObject(response);
        String sender = acceptFileTransferReq.sender();
        String filename = acceptFileTransferReq.filename();

        if (serverHandler.getServer().getUsers().get(sender) != null) {
            if (serverHandler.getFiles().contains(sender + ":" + filename)) {

                sendResp(OK, CODE_0000);
                sendAcceptOrDeclineResp(sender, filename);

                serverHandler.getFiles().remove(sender + ":" + filename);
            } else {
                sendResp(ERROR, CODE_12001);
            }
        } else {
            sendResp(ERROR, CODE_9000);
        }
    }
    @Override
    public void sendResp(String status, int code) throws JsonProcessingException {
        AcceptFileTransferResp acceptFileTransferResp = new AcceptFileTransferResp(status, code);
        String acceptFileTransferRespMessage = Utils.objectToMessage(acceptFileTransferResp);
        serverHandler.printAndSendResponse(acceptFileTransferRespMessage);
    }
    @Override
    public void sendAcceptOrDeclineResp(String sender, String filename) throws JsonProcessingException {
        AcceptFileTransfer acceptFileTransfer = new AcceptFileTransfer(ACCEPTED, serverHandler.getUsername(), filename);
        String acceptFileTransferMessage = Utils.objectToMessage(acceptFileTransfer);
        serverHandler.getServer().getUsers().get(sender).printAndSendResponse(acceptFileTransferMessage);
    }
}
