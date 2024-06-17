package servers.handlers.usersStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.Joined;
import communications.protocol.messages.Left;
import communications.protocol.utils.Utils;
import servers.handlers.ServerHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserStatusHandler {
    private final ServerHandler serverHandler;
    public UserStatusHandler(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    public <T> void sendStatusToOnlineUsers(T status) {
        Map<String, ServerHandler> users = serverHandler.getServer().getUsers();
        List<ServerHandler> userList = new ArrayList<>(users.values());

        try {
            String currentUsername = serverHandler.getUsername();

            for (ServerHandler user : userList) {
                if (user != null && currentUsername != null && !serverHandler.getUsername().equals(user.getUsername())) {
                    String statusMessageStr = Utils.objectToMessage(status);
                    user.printAndSendResponse(statusMessageStr);
                }
            }
            if (serverHandler.checkIfUserLoggedIn()) {
                String statusMessageStr = Utils.objectToMessage(status);
                serverHandler.printAndSendResponse(statusMessageStr);
            }
        } catch (JsonProcessingException e) {
            serverHandler.handleInvalidJsonFormat();
        }
    }

    public void sendJoinStatusToOnlineUsers() {
        Joined joined = new Joined(serverHandler.getUsername());
        sendStatusToOnlineUsers(joined);
    }

    public void sendLeftStatusToOnlineUsers() {
        Left left = new Left(serverHandler.getUsername());
        sendStatusToOnlineUsers(left);
    }
}
