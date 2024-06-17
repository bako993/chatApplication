package servers.handlers.guessingGames;

import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.GuessingGameResult;
import communications.protocol.utils.Utils;
import servers.handlers.ServerHandler;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GameResultHandler {
    private final ServerHandler serverHandler;
    private final List<PlayerInfo> playerInfos;
    public GameResultHandler(ServerHandler serverHandler, List<PlayerInfo> playerInfos) {
        this.serverHandler = serverHandler;
        this.playerInfos = playerInfos;
    }
    public void gameResult() {
        try {
            playerInfos.sort(Comparator.comparingLong(PlayerInfo::timeTaken));
            List<ResultInfo> resultsList = new ArrayList<>();

            for (int i = 0; i < playerInfos.size(); i++) {
                PlayerInfo result = playerInfos.get(i);
                ResultInfo resultEntry = new ResultInfo(i + 1, result.player(), (i == 0), result.timeTaken());
                resultsList.add(resultEntry);
            }

            GuessingGameResult guessingGameResult = new GuessingGameResult(resultsList);
            String sendGameResult = Utils.objectToMessage(guessingGameResult);

            for (PlayerInfo result : playerInfos) {
                String player = result.player();

                ServerHandler playerHandler = serverHandler.getServer().getUsers().get(player);
                if (playerHandler != null) {
                    playerHandler.printAndSendResponse(sendGameResult);
                }
            }
        } catch (JsonProcessingException e) {
            serverHandler.handleInvalidJsonFormat();
        }
    }
}