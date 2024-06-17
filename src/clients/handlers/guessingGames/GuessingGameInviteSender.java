package clients.handlers.guessingGames;

import clients.handlers.ClientHandler;
import clients.messageInterfaces.ISender;
import com.fasterxml.jackson.core.JsonProcessingException;
import static communications.Message.*;
import communications.protocol.messages.GuessingGameInviteReq;
import communications.protocol.utils.Utils;

public class GuessingGameInviteSender  implements ISender {
    private final ClientHandler client;
    public GuessingGameInviteSender(ClientHandler client) {
        this.client = client;
    }
    @Override
    public void sendRequest() throws JsonProcessingException {
        String inviteRequest = requestGuessingGame();
        if (inviteRequest != null) {
            client.send(inviteRequest);
        }
    }
    public String requestGuessingGame() throws JsonProcessingException {
        if (!client.isLoggedIn()) {
            System.out.println(MSG_16);
            return null;
        }
        GuessingGameInviteReq guessingGameInviteReq = new GuessingGameInviteReq(client.getUsername());

        return Utils.objectToMessage(guessingGameInviteReq);
    }
}
