package clients.handlers.logoffs;

import clients.messageInterfaces.IReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.Left;
import communications.protocol.utils.Utils;
import static communications.Message.MSG_05;

public class LeftReader implements IReader {
    @Override
    public void readResponse(String response) throws JsonProcessingException {
        Left leftResp = Utils.messageToObject(response);
        String username = leftResp.username();
        System.out.println(username + " " + MSG_05);
    }
}
