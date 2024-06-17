package servers.handlers.parseErrors;

import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.ParseError;
import communications.protocol.utils.Utils;
import servers.handlers.ResponseHandler;
import servers.handlers.ServerHandler;

public class ParseErrorHandler extends ResponseHandler{
    private final ServerHandler serverHandler;
    public ParseErrorHandler(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }
    public void handleParseError() {
        try {
            sendResp();
        } catch (JsonProcessingException e) {
            serverHandler.handleInvalidJsonFormat();
        }
    }
    @Override
    public void sendResp() throws JsonProcessingException {
        ParseError parseError = new ParseError();
        String parseErrorResp = Utils.objectToMessage(parseError);
        serverHandler.printAndSendResponse(parseErrorResp);
    }
}
