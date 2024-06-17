package servers.handlers.logoffs;

import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.ByeResp;
import communications.protocol.utils.Utils;
import servers.handlers.ResponseHandler;
import servers.handlers.ServerHandler;
import servers.handlers.usersStatus.UserStatusHandler;
import static communications.Status.OK;

public class LogoffHandler extends ResponseHandler {
    private final ServerHandler serverHandler;
    public LogoffHandler(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }
    public void Logoff(String response) {
        System.out.println(response);

        try {
            sendResp();

            UserStatusHandler userStatusHandler = new UserStatusHandler(serverHandler);
            userStatusHandler.sendLeftStatusToOnlineUsers();

            serverHandler.getServer().removeUser(serverHandler);
            serverHandler.closeClientConnection();

        } catch (JsonProcessingException e) {
            serverHandler.handleInvalidJsonFormat();
        }
    }
    @Override
    public void sendResp() throws JsonProcessingException {
        ByeResp byeResp = new ByeResp(OK);
        String byeRespMessage = Utils.objectToMessage(byeResp);
        serverHandler.printAndSendResponse(byeRespMessage);
    }
}
