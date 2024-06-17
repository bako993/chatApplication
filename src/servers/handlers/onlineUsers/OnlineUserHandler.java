package servers.handlers.onlineUsers;

import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.OnlineUserResp;
import communications.protocol.messages.OnlineUsers;
import communications.protocol.utils.Utils;
import servers.handlers.ResponseHandler;
import servers.handlers.ServerHandler;
import java.util.ArrayList;
import java.util.List;
import static communications.ErrorCode.*;
import static communications.Status.*;

public class OnlineUserHandler extends ResponseHandler {
    private final ServerHandler serverHandler;

    public OnlineUserHandler(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }
    public void displayOnlineUsers(String response) {
        System.out.println(response);

        try {
            if (!serverHandler.checkIfUserLoggedIn()) {
                sendResp(ERROR, CODE_6000);
                return;
            }

            StringBuilder stringBuilder = new StringBuilder();
            for (ServerHandler client: serverHandler.getServer().getUsers().values()) {
                if (!serverHandler.getUsername().equals(client.getUsername())) {
                    stringBuilder.append(client.getUsername()).append(",");
                }
            }

            List<String> onlineUsersArray = extractOnlineUsers();
            if (!onlineUsersArray.isEmpty()) {
                sendResp(OK, CODE_0000);
                sendOnlineUsersResp(onlineUsersArray);
            } else {
                sendResp(ERROR, CODE_10000);
            }
        } catch (JsonProcessingException e) {
            serverHandler.handleInvalidJsonFormat();
        }
    }
    private List<String> extractOnlineUsers() {
        List<String> onlineUsersArray = new ArrayList<>();
        for (ServerHandler client : serverHandler.getServer().getUsers().values()) {
            if (!serverHandler.getUsername().equals(client.getUsername())) {
                onlineUsersArray.add(client.getUsername());
            }
        }
        return onlineUsersArray;
    }
    @Override
    public void sendResp(String status, int code) throws JsonProcessingException {
        OnlineUserResp onlineUserResp = new OnlineUserResp(status, code);
        String onlineUserRespMessage = Utils.objectToMessage(onlineUserResp);
        serverHandler.printAndSendResponse(onlineUserRespMessage);
    }
    @Override
    public void sendOnlineUsersResp(List<String> onlineUsersList) throws JsonProcessingException {
        OnlineUsers onlineUsers = new OnlineUsers(onlineUsersList);
        String onlineUsersMessage = Utils.objectToMessage(onlineUsers);
        serverHandler.printAndSendResponse(onlineUsersMessage);
    }
}
