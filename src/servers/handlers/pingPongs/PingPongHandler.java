package servers.handlers.pingPongs;

import com.fasterxml.jackson.core.JsonProcessingException;
import static communications.ErrorCode.*;
import communications.protocol.messages.Dscn;
import communications.protocol.messages.Ping;
import communications.protocol.messages.Pong;
import communications.protocol.messages.PongError;
import communications.protocol.utils.Utils;
import servers.handlers.ResponseHandler;
import servers.handlers.ServerHandler;

public class PingPongHandler extends ResponseHandler {
    private final ServerHandler serverHandler;
    private boolean pingSent = false;
    private boolean pongReceived = false;
    public PingPongHandler(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }
    public void startPingPongThread() {
        Thread thread = new Thread(this::pingThread);
        thread.start();
    }
    private void pingThread() {
        try {
            Thread.sleep(10000);

            while (true) {
                sendPingMessage();

                Thread.sleep(3000);

                if (!pongReceived) {
                    handleDisconnection();
                    break;
                } else {
                    Pong pong = new Pong();
                    String pongMessage = Utils.objectToMessage(pong);
                    System.out.println(pongMessage);
                }

                reset();
                Thread.sleep(10000);
            }
        } catch (InterruptedException | JsonProcessingException e) {
            System.err.println(e.getMessage());
            serverHandler.handleInvalidJsonFormat();
        }
    }
    private void sendPingMessage() throws JsonProcessingException {
        sendResp(new Ping());
        pingSent = true;

    }
    private void handleDisconnection() throws JsonProcessingException {
        sendResp(new Dscn(CODE_7000));
        serverHandler.getServer().removeUser(serverHandler);
        serverHandler.closeClientConnection();
    }
    private void reset() {
        pingSent = false;
        pongReceived = false;
    }
    public void handlePongCommand(String response) throws JsonProcessingException {
        if (!pingSent) {
            handlePongError(response);
        } else {
            pongReceived = true;
        }
    }
    private void handlePongError(String response) throws JsonProcessingException {
        System.out.println(response);
        sendResp(new PongError(CODE_8000));
    }
    @Override
    public <T> void sendResp(T message) throws JsonProcessingException {
        String sendMessage = Utils.objectToMessage(message);
        serverHandler.printAndSendResponse(sendMessage);
    }
}
