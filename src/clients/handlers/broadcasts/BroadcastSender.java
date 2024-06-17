package clients.handlers.broadcasts;

import clients.handlers.ClientHandler;
import clients.messageInterfaces.ISender;
import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.BroadcastReq;
import communications.protocol.utils.Utils;
import static communications.Message.MSG_14;

public class BroadcastSender implements ISender {
    private final ClientHandler client;
    public BroadcastSender(ClientHandler client) {
        this.client = client;
    }
    @Override
    public void sendRequest() throws JsonProcessingException {
        client.send(broadcastChat());
    }
    private String broadcastChat() throws JsonProcessingException {
        String message = client.getUserInput(MSG_14);
        BroadcastReq broadcast = new BroadcastReq(message);

        return Utils.objectToMessage(broadcast);
    }
}
