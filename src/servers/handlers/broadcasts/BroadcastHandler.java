package servers.handlers.broadcasts;

import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.Broadcast;
import communications.protocol.messages.BroadcastReq;
import communications.protocol.messages.BroadcastResp;
import communications.protocol.utils.Utils;
import servers.handlers.ResponseHandler;
import servers.handlers.ServerHandler;
import static communications.ErrorCode.*;
import static communications.ErrorCode.CODE_0000;
import static communications.Status.*;

public class BroadcastHandler extends ResponseHandler {
    private final ServerHandler serverHandler;

    public BroadcastHandler(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    public void handleBroadcastChat(ServerHandler sender, String response) {
        try {
            System.out.println(response);

            if (!serverHandler.checkIfUserLoggedIn()) {
                sendResp(sender, ERROR, CODE_6000);
                return;
            }

            BroadcastReq message = Utils.messageToObject(response);
            String bodyReader = message.message();

            if (bodyReader.isEmpty() || bodyReader.isBlank()) {
                sendResp(sender, ERROR, CODE_6001);
                return;
            }

            sendResp(sender, OK, CODE_0000);
            sendBroadcastResp(sender.getUsername(), bodyReader);

        } catch (JsonProcessingException e) {
            serverHandler.handleInvalidJsonFormat();
        }
    }
    @Override
    public void sendResp(ServerHandler sender, String status, int code) throws JsonProcessingException {
        BroadcastResp broadcastResp = new BroadcastResp(status, code);
        String broadcastRespMessage = Utils.objectToMessage(broadcastResp);
        serverHandler.printAndSendResponse(sender, broadcastRespMessage);
    }
    @Override
    public void sendBroadcastResp(String senderUsername, String message) throws JsonProcessingException {
        for (ServerHandler user : serverHandler.getServer().getUsers().values()) {
            if (!senderUsername.equals(user.getUsername())) {
                Broadcast broadcast = new Broadcast(senderUsername, message);
                String broadcastMessage = Utils.objectToMessage(broadcast);
                user.printAndSendResponse(broadcastMessage);
            }
        }
    }
}
