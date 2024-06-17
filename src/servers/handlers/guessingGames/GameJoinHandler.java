package servers.handlers.guessingGames;

import com.fasterxml.jackson.core.JsonProcessingException;
import static communications.ErrorCode.*;
import static communications.Status.*;
import communications.protocol.messages.GuessingGameJoinReq;
import communications.protocol.messages.GuessingGameJoinResp;
import communications.protocol.utils.Utils;
import servers.handlers.ResponseHandler;
import servers.handlers.ServerHandler;

public class GameJoinHandler extends ResponseHandler {
    private final ServerHandler serverHandler;
    private final GuessingGameHandler guessingGameHandler;

    public GameJoinHandler(ServerHandler serverHandler, GuessingGameHandler guessingGameHandler) {
        this.serverHandler = serverHandler;
        this.guessingGameHandler = guessingGameHandler;
    }

    public void handleJoinedPlayers(String response) {
        System.out.println(response);
        try {
            GuessingGameJoinReq guessingGameJoinReq = Utils.messageToObject(response);
            String player = guessingGameJoinReq.player();

            if (!guessingGameHandler.getInvitedPlayers().contains(player)) {
                sendResp(player, ERROR, CODE_11004);
                return;
            }

            if (guessingGameHandler.getIsJoinCountdownFinished().get()) {
                sendResp(player, ERROR, CODE_11003);
                return;
            }

            guessingGameHandler.getJoinedPlayers().add(player);

            if (guessingGameHandler.getJoinedPlayers().contains(player)) {
                sendResp(player, OK, CODE_0000);
            }
        } catch (JsonProcessingException e) {
            serverHandler.handleInvalidJsonFormat();
        }
    }
    @Override
    public void sendResp(String username, String status, int code) throws JsonProcessingException {
        ServerHandler playerHandler = serverHandler.getServer().getUsers().get(username);
        GuessingGameJoinResp guessingGameJoinResp = new GuessingGameJoinResp(status, code);
        String joinMessage = Utils.objectToMessage(guessingGameJoinResp);
        if (playerHandler != null) {
            playerHandler.printAndSendResponse(joinMessage);
        }
    }
}
