package clients.handlers.welcomes;

import clients.messageInterfaces.IReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.Welcome;
import communications.protocol.utils.Utils;

public class WelcomeReader implements IReader {
    @Override
    public void readResponse(String response) throws JsonProcessingException {
        Welcome welcomeMsg = Utils.messageToObject(response);
        System.out.println(welcomeMsg.msg());
    }
}
