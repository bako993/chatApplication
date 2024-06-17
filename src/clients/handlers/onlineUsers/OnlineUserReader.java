package clients.handlers.onlineUsers;

import clients.messageInterfaces.IReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import communications.ErrorCode;
import communications.protocol.messages.OnlineUserResp;
import communications.protocol.messages.OnlineUsers;
import communications.protocol.utils.Utils;
import static communications.Command.*;
import static communications.Message.*;
import static communications.Status.ERROR;

public class OnlineUserReader implements IReader {
    @Override
    public void readResponse(String response) throws JsonProcessingException {
        String[] responseParts = response.split(" ");
        String command = responseParts[0];
        switch (command) {
            case ONLINE_USERS -> handleOnlineUsers(response);
            case ONLINE_USERS_RESP -> handleOnlineUsersResp(response);
        }
    }
    private void handleOnlineUsers(String response) throws JsonProcessingException {
        OnlineUsers onlineUsers = Utils.messageToObject(response);
        for (String user : onlineUsers.onlineUsers()) {
            System.out.println(user);
        }
    }
    private void handleOnlineUsersResp(String response) throws JsonProcessingException {
        OnlineUserResp onlineUserResp = Utils.messageToObject(response);
        if (onlineUserResp.status().equals(ERROR)) {
            switch (onlineUserResp.code()) {
                case ErrorCode.CODE_6000 -> System.out.println(MSG_16);
                case ErrorCode.CODE_10000 -> System.out.println(MSG_02);
            }
        }
    }
}
