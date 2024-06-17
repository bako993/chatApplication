package servers.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface IResponse {
    void sendResp(ServerHandler sender, String status, int code) throws JsonProcessingException;
    void sendBroadcastResp(String senderUsername, String message) throws JsonProcessingException;
    void sendResp(String status, int code) throws JsonProcessingException;
    void sendResp() throws JsonProcessingException;
    void sendAcceptOrDeclineResp(String sender, String filename) throws JsonProcessingException;
    void sendFileTransferResp(String sender, String filename, String receiver) throws JsonProcessingException;
    void sendResp(String username, String status, int code) throws JsonProcessingException;
    void sendOnlineUsersResp(List<String> onlineUsersList)  throws JsonProcessingException;
    <T> void sendResp(T message) throws JsonProcessingException;
    void sendPrivateResp(ServerHandler receiver, String message) throws JsonProcessingException;

}
