package servers.handlers.fileTransfers;

import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.FileTransfer;
import communications.protocol.messages.FileTransferReq;
import communications.protocol.messages.FileTransferResp;
import communications.protocol.utils.Utils;
import servers.handlers.ResponseHandler;
import servers.handlers.ServerHandler;
import static communications.ErrorCode.*;
import static communications.Status.*;

public class FileTransferRequestHandler extends ResponseHandler {

    private final ServerHandler serverHandler;
    public FileTransferRequestHandler( ServerHandler serverHandler) {

        this.serverHandler = serverHandler;
    }

    public void handleFileTransferRequest(String response) {
        String[] responseParts = response.split(" ");

        System.out.println(response);

        try {
            if (responseParts.length < 2) {
                sendResp(ERROR, CODE_12002);
                return;
            }

            if (!serverHandler.checkIfUserLoggedIn()) {
                sendResp(ERROR, CODE_6000);
                return;
            }

            FileTransferReq fileTransferReqBody = Utils.messageToObject(response);
            String receiver = fileTransferReqBody.receiver();
            String filename = fileTransferReqBody.filename();

            if (receiver.isEmpty() || receiver.isBlank() || filename.isBlank() || filename.isEmpty()) {
                sendResp(ERROR, CODE_12002);
            } else if (receiver.equals(serverHandler.getUsername())) {
                sendResp(ERROR, CODE_12000);
            } else if (!serverHandler.getServer().getUsers().containsKey(receiver)){
                sendResp(ERROR, CODE_9000);
            } else {
                handleSuccessFileTransferResponse(response);
            }

        } catch (JsonProcessingException e) {
            serverHandler.handleInvalidJsonFormat();
        }
    }
    private void handleSuccessFileTransferResponse(String response) throws JsonProcessingException {
        String sender = serverHandler.getUsername();

        FileTransferReq fileTransferReqBody = Utils.messageToObject(response);
        String receiver = fileTransferReqBody.receiver();
        String filename = fileTransferReqBody.filename();

        sendResp(OK, CODE_0000);
        serverHandler.getServer().getUsers().get(receiver).addFile(sender + ":" + filename);
        sendFileTransferResp(sender, filename, receiver);
    }
    @Override
    public void sendResp(String status, int code) throws JsonProcessingException {
        FileTransferResp fileTransferResp = new FileTransferResp(status, code);
        String fileTransferRespMessage = Utils.objectToMessage(fileTransferResp);
        serverHandler.printAndSendResponse(fileTransferRespMessage);
    }
    @Override
    public void sendFileTransferResp(String sender, String filename, String receiver) throws JsonProcessingException {
        FileTransfer fileTransfer = new FileTransfer(sender, filename);
        String fileTransferMessage = Utils.objectToMessage(fileTransfer);
        serverHandler.getServer().getUsers().get(receiver).printAndSendResponse(fileTransferMessage);
    }
}
