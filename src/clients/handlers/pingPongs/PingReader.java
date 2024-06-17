package clients.handlers.pingPongs;

import clients.handlers.ClientHandler;
import clients.messageInterfaces.IReader;
import com.fasterxml.jackson.core.JsonProcessingException;

public class PingReader implements IReader {
    private final ClientHandler clientHandler;

    public PingReader(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    @Override
    public void readResponse(String response) throws JsonProcessingException {
        new PongSender(clientHandler).sendRequest();
    }
}
