package clients.handlers.pingPongs;

import clients.messageInterfaces.IReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import static communications.Message.MSG_20;

public class DscnReader implements IReader {
    @Override
    public void readResponse(String response) throws JsonProcessingException {
        System.out.println(MSG_20);
    }
}
