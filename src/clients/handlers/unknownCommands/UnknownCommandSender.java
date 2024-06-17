package clients.handlers.unknownCommands;

import clients.handlers.ClientHandler;
import clients.messageInterfaces.ISender;
import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.Msg;
import communications.protocol.utils.Utils;

public class UnknownCommandSender implements ISender {
    private final String option;
    private final ClientHandler client;

    public UnknownCommandSender(ClientHandler client, String option) {
       this.client = client;
        this.option = option;
    }
    @Override
    public void sendRequest() throws JsonProcessingException {
        client.send(unknownMsg());
    }
    private String unknownMsg() throws JsonProcessingException {
        Msg msg = new Msg(option);
        return Utils.objectToMessage(msg);
    }
}
