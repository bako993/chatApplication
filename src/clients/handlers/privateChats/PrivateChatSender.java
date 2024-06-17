package clients.handlers.privateChats;

import clients.handlers.ClientHandler;
import clients.messageInterfaces.ISender;
import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.PrivateReq;
import communications.protocol.utils.Utils;
import static communications.Message.*;

public class PrivateChatSender implements ISender {
    private final ClientHandler client;
    public PrivateChatSender(ClientHandler client) {
        this.client = client;
    }
    @Override
    public void sendRequest() throws JsonProcessingException {
        client.send(privateChat());
    }
    private String privateChat() throws JsonProcessingException {
       String username = client.getUserInput(MSG_10);
       String message = client.getUserInput(MSG_14);
       PrivateReq privateReq = new PrivateReq(username, message);

       return Utils.objectToMessage(privateReq);
    }

}
