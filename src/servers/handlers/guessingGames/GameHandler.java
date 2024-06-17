package servers.handlers.guessingGames;

import com.fasterxml.jackson.core.JsonProcessingException;

import static communications.ErrorCode.*;
import static communications.Status.*;
import communications.protocol.messages.GuessReq;
import communications.protocol.messages.GuessResp;
import communications.protocol.utils.Utils;
import servers.handlers.ResponseHandler;
import servers.handlers.ServerHandler;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GameHandler extends ResponseHandler {
    private final ServerHandler serverHandler;
    private final GuessingGameHandler guessingGameHandler;
    private final List<PlayerInfo> playerInfos = new ArrayList<>();
    private final AtomicInteger correctGuessCounter = new AtomicInteger(0);
    private final Random random = new Random();
    private Integer randomNumber = null;
    private final GameResultHandler gameResultHandler;
    public GameHandler(ServerHandler serverHandler, GuessingGameHandler guessingGameHandler) {
        this.serverHandler = serverHandler;
        this.guessingGameHandler = guessingGameHandler;

        this.gameResultHandler = new GameResultHandler(serverHandler,playerInfos);
    }

    public void handleGuess(String response) throws JsonProcessingException {
        System.out.println(response);
        GuessReq guessReq = Utils.messageToObject(response);
        String player = guessReq.player();
        int guess = guessReq.guess();

        try {

            if (!guessingGameHandler.getInvitedPlayers().contains(player)) {
                sendResp(player, ERROR, CODE_11004);
                return;
            }
            if (!guessingGameHandler.getJoinedPlayers().contains(player)) {
                sendResp(player, ERROR, CODE_11006);
                return;
            }

            if (!isValidGuess(guess)) {
                sendResp(player,ERROR, CODE_11010);
                return;
            }

            if (!guessingGameHandler.isGameStarted()){
                sendResp(player, ERROR, CODE_11005);
                return;
            }

            handleGuessResponse(player, guess);

        } catch (JsonProcessingException e) {
            serverHandler.handleInvalidJsonFormat();
        }
    }
    private void handleGuessResponse(String player, int guess) throws JsonProcessingException {

        if (randomNumber == null) {
            randomNumber = random.nextInt(50) + 1;
        }

        if (playerInfos.stream().anyMatch(result -> result.player().equals(player))) {
            sendResp(player, ERROR, CODE_11007);
            return;
        }

        if (guess == randomNumber) {
            sendResp(player, OK, CODE_0000);

            long endTime = System.currentTimeMillis();
            long timeTaken = endTime - guessingGameHandler.getGameStartTime();

            playerInfos.add(new PlayerInfo(player, timeTaken));
            correctGuessCounter.incrementAndGet();

        } else if (guess < randomNumber) {
            sendResp(player, ERROR, CODE_11008);
        } else {
            sendResp(player, ERROR, CODE_11009);
        }
    }
    private boolean isValidGuess(int guess) {
        return guess >= 1 && guess <= 50;
    }
    public AtomicInteger getCorrectGuessCounter() {
        return correctGuessCounter;
    }
    public void handleGameResult() {
        gameResultHandler.gameResult();
    }
    @Override
    public void sendResp(String username, String status, int code) throws JsonProcessingException {
        ServerHandler user = serverHandler.getServer().getUsers().get(username);
        GuessResp guessResp = new GuessResp(status, code);
        String guessRespMessage = Utils.objectToMessage(guessResp);
        if (user != null) {
            user.printAndSendResponse(guessRespMessage);
        }
    }
}
