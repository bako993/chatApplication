package servers.handlers.privateChats;

import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.Private;
import communications.protocol.messages.PrivateReq;
import communications.protocol.messages.PrivateResp;
import communications.protocol.utils.Utils;
import servers.handlers.ResponseHandler;
import servers.handlers.ServerHandler;
import static communications.ErrorCode.*;
import static communications.Status.*;

public class PrivateChatHandler extends ResponseHandler {
    private final ServerHandler serverHandler;
    public PrivateChatHandler(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }
    public void handlePrivateChat(String response) {
        System.out.println(response);

        try {
            PrivateReq privateReqBody = Utils.messageToObject(response);
            String receiver = privateReqBody.receiver();
            String message = privateReqBody.message();

            if (!serverHandler.checkIfUserLoggedIn()) {
                handleUserNotLoggedIn();
                return;
            }

            if (serverHandler.getUsername().equals(receiver)) {
                handleSelfMessage();
                return;
            }

            if (message.isEmpty() || message.isBlank()) {
                handleEmptyMessage();
                return;
            }

            boolean userFound = handleSendingMessageToReceiver(receiver, message);

            if (!userFound) {
                handleReceiverNotFound();
            }
        } catch (JsonProcessingException e) {
            serverHandler.handleInvalidJsonFormat();
        }
    }

    private void handleUserNotLoggedIn() throws JsonProcessingException {
        sendResp(ERROR, CODE_6000);
    }
    private void handleEmptyMessage() throws JsonProcessingException {
        sendResp(ERROR, CODE_6001);
    }
    private void handleReceiverNotFound() throws JsonProcessingException {
        sendResp(ERROR, CODE_9000);
    }
    private void handleSelfMessage() throws JsonProcessingException {
        sendResp(ERROR, CODE_9001);
    }

    private boolean handleSendingMessageToReceiver(String receiver, String message) throws JsonProcessingException {
        for (ServerHandler user : serverHandler.getServer().getUsers().values()) {
            if (receiver.equals(user.getUsername())) {
                sendResp(OK, CODE_0000);
                sendPrivateResp(user, message);
                return true;
            }
        }
        return false;
    }
    @Override
    public void sendResp(String status, int code) throws JsonProcessingException {
        PrivateResp privateResp = new PrivateResp(status, code);
        String privateRespMessage = Utils.objectToMessage(privateResp);
        serverHandler.printAndSendResponse(privateRespMessage);
    }
    @Override
    public void sendPrivateResp(ServerHandler receiver, String message) throws JsonProcessingException {
        Private aPrivate = new Private(serverHandler.getUsername(), message);
        String privateMessage = Utils.objectToMessage(aPrivate);
        serverHandler.printAndSendResponse(receiver, privateMessage);
    }
}
