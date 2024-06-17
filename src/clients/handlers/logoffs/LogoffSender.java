package clients.handlers.logoffs;

import clients.handlers.ClientHandler;
import clients.messageInterfaces.ISender;
import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.Bye;
import communications.protocol.utils.Utils;

public class LogoffSender implements ISender {
    private final ClientHandler client;
    public LogoffSender(ClientHandler client) {
        this.client = client;
    }
    @Override
    public void sendRequest() throws JsonProcessingException {
        client.send(leaveChat());
    }
    private String leaveChat() throws JsonProcessingException {
        Bye bye = new Bye();
        return Utils.objectToMessage(bye);
    }
}
