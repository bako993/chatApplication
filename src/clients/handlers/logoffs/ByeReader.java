package clients.handlers.logoffs;

import clients.messageInterfaces.IReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import static communications.Message.MSG_03;

public class ByeReader implements IReader {
    @Override
    public void readResponse(String response) throws JsonProcessingException {
        System.out.println(MSG_03);
        System.exit(1);
    }
}
