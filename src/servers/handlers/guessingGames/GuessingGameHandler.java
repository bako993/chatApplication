package servers.handlers.guessingGames;

import com.fasterxml.jackson.core.JsonProcessingException;
import static communications.Command.*;
import static communications.ErrorCode.*;
import static communications.Status.*;
import communications.protocol.messages.GuessingGameInviteResp;
import communications.protocol.utils.Utils;
import servers.handlers.ResponseHandler;
import servers.handlers.ServerHandler;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class GuessingGameHandler extends ResponseHandler {
    private final int JOIN_COUNTDOWN_SECONDS = 10;
    private final int GAME_COUNTDOWN_SECONDS = 120;
    private final ServerHandler serverHandler;
    private final Set<String> joinedPlayers;
    private final Set<String> invitedPlayers;
    private int joinCountdownSeconds;
    private int gameCountdownSeconds;
    private ScheduledExecutorService joinCountdownScheduler;
    private final AtomicReference<ScheduledExecutorService> schedulerRef = new AtomicReference<>(null);
    private final AtomicReference<ScheduledExecutorService> gameSchedulerRef = new AtomicReference<>(null);
    private final AtomicBoolean isJoinCountdownFinished;
    private final AtomicBoolean isGameCountdownFinished;
    private final GameHandler gameHandler;
    private final GameInviteHandler gameInviteHandler;
    private final GameJoinHandler gameJoinHandler;
    private static GuessingGameHandler instance;
    private boolean isGameStarted = false;
    private long gameStartTime;
    public GuessingGameHandler(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;

        joinedPlayers = new HashSet<>();
        invitedPlayers = new HashSet<>();

        this.isJoinCountdownFinished = new AtomicBoolean(false);
        joinCountdownSeconds = JOIN_COUNTDOWN_SECONDS;

        this.isGameCountdownFinished = new AtomicBoolean(false);
        gameCountdownSeconds = GAME_COUNTDOWN_SECONDS;

        gameInviteHandler = new GameInviteHandler(serverHandler,this);
        gameJoinHandler = new GameJoinHandler(serverHandler,this);
        gameHandler = new GameHandler(serverHandler,this);
    }
    public static GuessingGameHandler getInstance(ServerHandler serverHandler) {
        if (instance == null) {
            instance = new GuessingGameHandler(serverHandler);
        }
        return instance;
    }
    public void resetInstance() {
        instance = null;
        isJoinCountdownFinished.set(false);
        isGameCountdownFinished.set(false);
        joinedPlayers.clear();
        invitedPlayers.clear();
    }
    public void handleGuessingGame(String response) throws JsonProcessingException {
        String[] responseParts = response.split(" ",2);
        String command = responseParts[0];

        switch (command) {
            case GUESSING_GAME_INVITE_REQ -> gameInviteHandler.handleGuessingGameRequest(response);
            case GUESSING_GAME_JOIN_REQ -> gameJoinHandler.handleJoinedPlayers(response);
            case GUESS_REQ -> gameHandler.handleGuess(response);
        }
    }
    private void startGameCountdown() throws JsonProcessingException {
        if (joinedPlayers.size() < 2) {
            sendResp(serverHandler.getUsername(), CODE_11000);
            resetInstance();
            return;
        }

        if (gameSchedulerRef.get() == null || gameSchedulerRef.get().isShutdown()) {
            ScheduledExecutorService gameCountdownScheduler = Executors.newScheduledThreadPool(1);
            isGameCountdownFinished.set(false);
            gameCountdownSeconds = GAME_COUNTDOWN_SECONDS;
            isGameStarted = true;
            gameStartTime = System.currentTimeMillis();
            gameCountdownScheduler.scheduleAtFixedRate(() -> {
                try {
                    gameCountdown();
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }, 0, 1, TimeUnit.SECONDS);
            gameSchedulerRef.set(gameCountdownScheduler);
        }
    }
    public void startJoinCountdown() {
        if (schedulerRef.get() == null || schedulerRef.get().isShutdown()) {
            joinCountdownScheduler = Executors.newScheduledThreadPool(1);
            isJoinCountdownFinished.set(false);
            joinCountdownSeconds = JOIN_COUNTDOWN_SECONDS;
            joinCountdownScheduler.scheduleAtFixedRate(() -> {
                try {
                    joinCountdown();
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }, 0, 1, TimeUnit.SECONDS);
            schedulerRef.set(joinCountdownScheduler);
        }

    }
    private void joinCountdown() throws JsonProcessingException {
        System.out.println("Time left to join: " + joinCountdownSeconds + " seconds");

        if (--joinCountdownSeconds < 0) {
            ScheduledExecutorService scheduler = schedulerRef.getAndSet(null);
            if (scheduler != null) {
                isJoinCountdownFinished.set(true);
                scheduler.shutdownNow();

                for (String invitedPlayer: invitedPlayers) {
                    if (!serverHandler.getServer().getUsers().containsKey(gameInviteHandler.getRequester())) {
                        sendResp(invitedPlayer, CODE_11001);
                    }
                }
            }
        }
        if (isJoinCountdownFinished.get()) {
            startGameCountdown();
        }
    }
    private void gameCountdown() throws JsonProcessingException {
        System.out.println("Time left of the game: " + gameCountdownSeconds + " seconds");

        boolean allPlayersGuessed = (gameHandler.getCorrectGuessCounter().get() == joinedPlayers.size());
        boolean allPlayersOffline = joinedPlayers.stream()
                .noneMatch(player -> serverHandler.getServer().getUsers().containsKey(player));

        if (--gameCountdownSeconds < 0 || allPlayersGuessed || allPlayersOffline) {
            ScheduledExecutorService scheduler = gameSchedulerRef.getAndSet(null);
            if (scheduler != null) {
                isGameCountdownFinished.set(true);
                scheduler.shutdownNow();
                gameHandler.handleGameResult();
                resetInstance();
            }
        }
    }
    public boolean isInvitationActive() {
        return joinCountdownScheduler != null && !joinCountdownScheduler.isShutdown();
    }
    public AtomicBoolean getIsJoinCountdownFinished() {
        return isJoinCountdownFinished;
    }
    public Set<String> getJoinedPlayers() {
        return joinedPlayers;
    }
    public long getGameStartTime() {
        return gameStartTime;
    }
    public boolean isGameStarted() {
        return isGameStarted;
    }
    public Set<String> getInvitedPlayers() {
        return invitedPlayers;
    }
    @Override
    public void sendResp(String username, int code) throws JsonProcessingException {
        ServerHandler user = serverHandler.getServer().getUsers().get(username);
        GuessingGameInviteResp guessingGameInviteResp = new GuessingGameInviteResp(ERROR, code);
        String inviteRespMessage = Utils.objectToMessage(guessingGameInviteResp);
        serverHandler.printAndSendResponse(user,inviteRespMessage);
    }
}

