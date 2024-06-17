package servers.handlers.guessingGames;

import com.fasterxml.jackson.core.JsonProcessingException;
import static communications.ErrorCode.*;
import static communications.Status.*;
import communications.protocol.messages.GuessingGameInvite;
import communications.protocol.messages.GuessingGameInviteReq;
import communications.protocol.messages.GuessingGameInviteResp;
import communications.protocol.utils.Utils;
import servers.handlers.ResponseHandler;
import servers.handlers.ServerHandler;

public class GameInviteHandler extends ResponseHandler {
    private final ServerHandler serverHandler;
    private final GuessingGameHandler guessingGameHandler;
    private String requester;

    public GameInviteHandler(ServerHandler serverHandler, GuessingGameHandler guessingGameHandler) {
        this.serverHandler = serverHandler;
        this.guessingGameHandler = guessingGameHandler;
    }

    public void handleGuessingGameRequest(String response) {
        System.out.println(response);

        try {
            if (guessingGameHandler.isInvitationActive() || guessingGameHandler.getJoinedPlayers().size() > 1) {
                GuessingGameInviteReq guessingGameInviteReq = Utils.messageToObject(response);
                String requester = guessingGameInviteReq.requester();
                sendResp(requester, ERROR, CODE_11002);
                return;
            }

            guessingGameHandler.startJoinCountdown();

            GuessingGameInviteReq guessingGameInviteReq = Utils.messageToObject(response);
            requester = guessingGameInviteReq.requester();

            sendResp(requester, OK, CODE_0000);
            sendResp();
        } catch (JsonProcessingException e) {
            serverHandler.handleInvalidJsonFormat();
        }
    }
    public String getRequester() {
        return requester;
    }
    @Override
    public void sendResp() throws JsonProcessingException {
        for (ServerHandler user : serverHandler.getServer().getUsers().values()) {
            guessingGameHandler.getInvitedPlayers().add(user.getUsername());
            GuessingGameInvite guessingGameInvite = new GuessingGameInvite(requester);
            String guessingGameInviteMessage = Utils.objectToMessage(guessingGameInvite);
            user.printAndSendResponse(guessingGameInviteMessage);
        }
    }
    @Override
    public void sendResp(String username, String status, int code) throws JsonProcessingException {
        ServerHandler user = serverHandler.getServer().getUsers().get(username);
        GuessingGameInviteResp guessingGameInviteResp = new GuessingGameInviteResp(status, code);
        String guessingGameInviteRespMessage = Utils.objectToMessage(guessingGameInviteResp);
        if (user != null) {
            user.printAndSendResponse(guessingGameInviteRespMessage);
        }
    }
}
