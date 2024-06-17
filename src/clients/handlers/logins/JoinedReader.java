package clients.handlers.logins;

import clients.handlers.ClientHandler;
import clients.messageInterfaces.IReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.Joined;
import communications.protocol.utils.Utils;

import static communications.Message.MSG_04;

public class JoinedReader implements IReader {
    private final ClientHandler client;
    public JoinedReader(ClientHandler client) {
        this.client = client;
    }
    @Override
    public void readResponse(String response) throws JsonProcessingException {
        Joined joinedResp = Utils.messageToObject(response);
        String username = joinedResp.username();

        if (client.getUsername() == null) {
            client.setUsername(username);
        }
        if (!client.getUsername().equals(username)) {
            System.out.println(username + " " + MSG_04);
        }
    }
}
