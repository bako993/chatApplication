package clients.handlers.unknownCommands;

import clients.messageInterfaces.IReader;
import static communications.Message.MSG_09;

public class UnknownCommandReader implements IReader {
    @Override
    public void readResponse(String response) {
        System.out.println(MSG_09);
    }
}
