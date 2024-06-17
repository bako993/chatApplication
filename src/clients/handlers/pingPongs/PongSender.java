package clients.handlers.pingPongs;

import clients.handlers.ClientHandler;
import clients.messageInterfaces.ISender;
import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.Pong;
import communications.protocol.utils.Utils;

public class PongSender implements ISender {
    private final ClientHandler client;
    public PongSender(ClientHandler client) {
        this.client = client;
    }
    @Override
    public void sendRequest() throws JsonProcessingException {
        Pong pong = new Pong();
        String sendPong = Utils.objectToMessage(pong);
        client.send(sendPong);
    }
}
