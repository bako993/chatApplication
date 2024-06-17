package clients.handlers.guessingGames;

import clients.handlers.ClientHandler;
import clients.messageInterfaces.ISender;
import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.GuessingGameJoinReq;
import communications.protocol.utils.Utils;
import static communications.Message.*;

public class GuessingGameJoinSender implements ISender {
    private final ClientHandler client;
    public GuessingGameJoinSender(ClientHandler client) {
        this.client = client;
    }
    @Override
    public void sendRequest() throws JsonProcessingException {
        String joinRequest = guessingGameJoinedUser();
        if (joinRequest != null) {
            client.send(joinRequest);
        }
    }
    private String guessingGameJoinedUser() throws JsonProcessingException {
        if (!client.isLoggedIn()) {
            System.out.println(MSG_16);
            return null;
        }
        GuessingGameJoinReq guessingGameJoinReq = new GuessingGameJoinReq(client.getUsername());

        return Utils.objectToMessage(guessingGameJoinReq);
    }
}
