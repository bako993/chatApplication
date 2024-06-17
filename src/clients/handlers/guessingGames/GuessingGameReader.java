package clients.handlers.guessingGames;

import clients.handlers.ClientHandler;
import clients.messageInterfaces.IReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import static communications.Command.*;
import static communications.ErrorCode.*;
import static communications.Message.*;
import static communications.Status.*;
import communications.protocol.messages.*;
import communications.protocol.utils.Utils;
import servers.handlers.guessingGames.ResultInfo;

public class GuessingGameReader implements IReader {
    private static boolean inGame = true;
    private static boolean entryRequirementMet = true;
    private final ClientHandler client;
    public GuessingGameReader(ClientHandler client) {
        this.client = client;
    }
    public static boolean isInGame() {
        return inGame;
    }
    public static void setInGame(boolean inGame) {
        GuessingGameReader.inGame = inGame;
    }
    public static boolean isEntryRequirementMet() {
        return entryRequirementMet;
    }
    public static void setEntryRequirementMet(boolean entryRequirementMet) {
        GuessingGameReader.entryRequirementMet = entryRequirementMet;
    }
    @Override
    public void readResponse(String response) throws JsonProcessingException {
        String[] parts = response.split(" ", 2);
        String command = parts[0];

        switch (command) {
            case GUESSING_GAME_INVITE_RESP -> handleInviteResponse(response);
            case GUESSING_GAME_INVITE -> handleInvite(response);
            case GUESSING_GAME_JOIN_RESP -> handleJoinResponse(response);
            case GUESS_RESP -> handleUserGuessResponse(response);
            case GUESSING_GAME_RESULT -> handleGameResultResponse(response);
        }
    }

    private void handleGameResultResponse(String response) throws JsonProcessingException {
        GuessingGameResult guessingGameResult = Utils.messageToObject(response);
        System.out.println(MSG_64);
        for (ResultInfo result: guessingGameResult.gameResults()) {
            int position = result.position();
            String player = result.player();
            boolean winner = result.winner();
            long timeTaken = result.timeTaken();
            String winnerStatus = winner ? "winner, " : "";
            System.out.println(position + ". " + player + " (" + winnerStatus + timeTaken + "ms)");
        }
    }

    private void handleUserGuessResponse(String response) throws JsonProcessingException {
        GuessResp guessResp = Utils.messageToObject(response);

        switch (guessResp.status()) {
            case OK -> correctGuessResp();
            case ERROR -> {
                switch (guessResp.code()) {
                    case CODE_11008 -> System.out.println(MSG_50);
                    case CODE_11009 -> System.out.println(MSG_51);
                    case CODE_11010 -> System.out.println(MSG_48);
                    case CODE_11005 -> waitForPlayersToJoinResp();
                    case CODE_11006 -> promptUserToJoinResp();
                    case CODE_11004 -> requireInvitationAndRegistrationResp();
                    case CODE_11007 -> userAlreadyGuessedResp();
                }
            }
        }
    }
    private void correctGuessResp() {
        System.out.println(MSG_49);
        setInGame(false);
    }
    private void waitForPlayersToJoinResp() {
        System.out.println(MSG_47);
        setEntryRequirementMet(false);
    }
    private void promptUserToJoinResp() {
        System.out.println(MSG_52);
        setEntryRequirementMet(false);
    }
    private void requireInvitationAndRegistrationResp() {
        System.out.println(MSG_53);
        setEntryRequirementMet(false);
    }

    private void userAlreadyGuessedResp() {
        System.out.println(MSG_54);
        setEntryRequirementMet(false);
    }

    private void handleJoinResponse(String response) throws JsonProcessingException {
        GuessingGameJoinResp guessingGameJoinResp = Utils.messageToObject(response);
        String status = guessingGameJoinResp.status();
        int code = guessingGameJoinResp.code();

        switch (status) {
            case OK -> System.out.println(MSG_44 + "\n" + MSG_45);
            case ERROR -> {
                switch (code) {
                    case CODE_11003 -> System.out.println(MSG_43);
                    case CODE_11004 -> System.out.println(MSG_46);
                }
            }
        }
    }
    private void handleInviteResponse(String response) throws JsonProcessingException {
        GuessingGameInviteResp guessingGameInviteResp = Utils.messageToObject(response);
        String status = guessingGameInviteResp.status();
        int code = guessingGameInviteResp.code();
        switch (status) {
            case OK -> System.out.println(MSG_40);
            case ERROR -> {
                switch (code) {
                    case CODE_11002 -> System.out.println(MSG_55);
                    case CODE_11000 -> System.out.println(MSG_56);
                    case CODE_11001 -> System.out.println(MSG_57);
                }
            }
        }
    }
    private void handleInvite(String  response) throws JsonProcessingException {
        GuessingGameInvite guessingGameInvite = Utils.messageToObject(response);
        String requester = guessingGameInvite.requester();
        if (requester == null) {
            return;
        }
        if (!requester.equals(client.getUsername())) {
            System.out.println(requester + " " + MSG_42);
        }
    }
}
