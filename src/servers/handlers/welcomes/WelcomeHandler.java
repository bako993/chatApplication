package servers.handlers.welcomes;

import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.Welcome;
import communications.protocol.utils.Utils;
import servers.handlers.ResponseHandler;
import servers.handlers.ServerHandler;
import static communications.Message.MSG_00;

public class WelcomeHandler extends ResponseHandler {
    private final ServerHandler serverHandler;
    public WelcomeHandler(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }
    public void welcomeMessage() {
        try {
            sendResp();
        } catch (JsonProcessingException e) {
            serverHandler.handleInvalidJsonFormat();
        }
    }
    @Override
    public void sendResp() throws JsonProcessingException {
        Welcome welcome = new Welcome(MSG_00);
        String welcomeMsg = Utils.objectToMessage(welcome);
        serverHandler.printAndSendResponse(welcomeMsg);
    }
}
