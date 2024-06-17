package clients.handlers.guessingGames;

import clients.handlers.ClientHandler;
import clients.messageInterfaces.ISender;
import com.fasterxml.jackson.core.JsonProcessingException;
import static communications.Message.*;
import communications.protocol.messages.GuessReq;
import communications.protocol.utils.Utils;

public class GuessingGameSender implements ISender{
    private final ClientHandler client;
    public GuessingGameSender(ClientHandler client) {
        this.client = client;
    }
    @Override
    public void sendRequest() {
        playGuessingGame();
    }
    private void playGuessingGame() {

        if (!client.isLoggedIn()) {
            System.out.println(MSG_16);
            return;
        }
        System.out.println(MSG_59);

        while (true) {
            try {
                int guess = Integer.parseInt(client.getUserInput(""));

                GuessReq guessReq = new GuessReq(client.getUsername(), guess);
                String sendUserGuess = Utils.objectToMessage(guessReq);

                if (!GuessingGameReader.isEntryRequirementMet()) {
                    System.out.println(MSG_60);
                    GuessingGameReader.setEntryRequirementMet(true);
                    break;
                }
                if (!GuessingGameReader.isInGame()) {
                    System.out.println(MSG_61);
                    GuessingGameReader.setInGame(true);
                    break;
                }

                client.send(sendUserGuess);

            } catch (NumberFormatException | JsonProcessingException e) {
                System.out.println(MSG_62);
            }
        }
    }
}
