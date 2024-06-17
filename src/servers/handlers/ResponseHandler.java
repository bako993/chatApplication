package servers.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import static communications.Message.*;

public abstract class ResponseHandler implements IResponse{

    @Override
    public void sendResp() throws JsonProcessingException {
        throw new UnsupportedOperationException(MSG_81);
    }
    @Override
    public void sendResp(String status, int code) throws JsonProcessingException {
        throw new UnsupportedOperationException(MSG_81);
    }
    @Override
    public <T> void sendResp(T message) throws JsonProcessingException {
        throw new UnsupportedOperationException(MSG_81);
    }
    @Override
    public void sendResp(String username, String status, int code) throws JsonProcessingException {
        throw new UnsupportedOperationException(MSG_81);
    }
    @Override
    public void sendResp(ServerHandler sender, String status, int code) throws JsonProcessingException {
        throw new UnsupportedOperationException(MSG_81);
    }
    @Override
    public void sendBroadcastResp(String senderUsername, String message) throws JsonProcessingException {
        throw new UnsupportedOperationException(MSG_82);
    }
    @Override
    public void sendPrivateResp(ServerHandler receiver, String message) throws JsonProcessingException {
        throw new UnsupportedOperationException(MSG_86);
    }
    @Override
    public void sendAcceptOrDeclineResp(String sender, String filename) throws JsonProcessingException {
        throw new UnsupportedOperationException(MSG_83);
    }
    @Override
    public void sendFileTransferResp(String sender, String filename, String receiver) throws JsonProcessingException {
        throw new UnsupportedOperationException(MSG_84);
    }
    @Override
    public void sendOnlineUsersResp(List<String> onlineUsersList) throws JsonProcessingException {
        throw new UnsupportedOperationException(MSG_85);
    }
}
