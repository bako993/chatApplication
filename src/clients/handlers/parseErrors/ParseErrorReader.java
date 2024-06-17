package clients.handlers.parseErrors;

import clients.messageInterfaces.IReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import static communications.Message.MSG_77;

public class ParseErrorReader implements IReader {
    @Override
    public void readResponse(String response) throws JsonProcessingException {
        System.out.println(MSG_77);
    }
}
