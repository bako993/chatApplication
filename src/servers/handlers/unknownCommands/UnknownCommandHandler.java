package servers.handlers.unknownCommands;

import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.UnknownCommand;
import communications.protocol.utils.Utils;
import servers.handlers.ResponseHandler;
import servers.handlers.ServerHandler;

public class UnknownCommandHandler extends ResponseHandler {
    private final ServerHandler serverHandler;

    public UnknownCommandHandler(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }
    public void unknownCommand() {
        System.out.println(serverHandler.getResponse());
        try {
            sendResp();
        } catch (JsonProcessingException e) {
            serverHandler.handleInvalidJsonFormat();
        }
    }
    @Override
    public void sendResp() throws JsonProcessingException {
        UnknownCommand unknownCommand = new UnknownCommand();
        String unknownCommandResp = Utils.objectToMessage(unknownCommand);
        serverHandler.printAndSendResponse(unknownCommandResp);
    }
}
