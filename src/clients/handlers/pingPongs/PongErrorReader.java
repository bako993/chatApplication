package clients.handlers.pingPongs;

import clients.messageInterfaces.IReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import static communications.Message.MSG_21;

public class PongErrorReader implements IReader {
    @Override
    public void readResponse(String response) throws JsonProcessingException {
        System.out.println(MSG_21);
    }
}
