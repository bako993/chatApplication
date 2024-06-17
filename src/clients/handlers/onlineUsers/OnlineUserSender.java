package clients.handlers.onlineUsers;

import clients.handlers.ClientHandler;
import clients.messageInterfaces.ISender;
import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.OnlineUserReq;
import communications.protocol.utils.Utils;

public class OnlineUserSender implements ISender {
    private final ClientHandler client;
    public OnlineUserSender(ClientHandler client) {
        this.client = client;
    }
    @Override
    public void sendRequest() throws JsonProcessingException {
        client.send(onlineUsers());
    }
    private String onlineUsers() throws JsonProcessingException {
        OnlineUserReq onlineUserReq = new OnlineUserReq(client.getUsername());
        return Utils.objectToMessage(onlineUserReq);
    }
}
