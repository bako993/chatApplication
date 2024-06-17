package servers.handlers.logins;

import com.fasterxml.jackson.core.JsonProcessingException;
import static communications.ErrorCode.*;
import static communications.Status.*;
import communications.protocol.messages.Login;
import communications.protocol.messages.LoginResp;
import communications.protocol.utils.Utils;
import servers.handlers.ResponseHandler;
import servers.handlers.pingPongs.PingPongHandler;
import servers.handlers.ServerHandler;
import servers.handlers.usersStatus.UserStatusHandler;

import java.net.Socket;

public class LoginHandler extends ResponseHandler {
    private final ServerHandler serverHandler;
    private final PingPongHandler pingPongHandler;
    public LoginHandler(ServerHandler serverHandler, PingPongHandler pingPongHandler) {
        this.serverHandler = serverHandler;
        this.pingPongHandler = pingPongHandler;
    }

    public void handleLogin(String response) {
        String[] responseParts = response.split(" ",2);
        System.out.println(response);

        try {
            if (responseParts[1].isBlank() || responseParts[1].isEmpty() || !isValidUsername(response)) {
                handleInvalidUsername();
                return;
            }

            String loggedInUserName = getLoggedInUserName(serverHandler.getClientSocket());
            if (loggedInUserName != null) {
                handleUserLoginTwice();
                return;
            }

            if (checkIfUsernameExists(response)) {
                handleExistingUsername();
                return;
            }

            handleSuccessfulLogin(response);
            pingPongHandler.startPingPongThread();

        } catch (JsonProcessingException e) {
            serverHandler.handleInvalidJsonFormat();
        }
    }
    private void handleSuccessfulLogin(String response) throws JsonProcessingException {
        Login login = Utils.messageToObject(response);

        serverHandler.setUsername(login.username());
        serverHandler.getServer().addUser(serverHandler);

        sendResp(OK, CODE_0000);

        UserStatusHandler userStatus = new UserStatusHandler(serverHandler);
        userStatus.sendJoinStatusToOnlineUsers();
    }
    private void handleExistingUsername() throws JsonProcessingException {
        sendResp(ERROR, CODE_5000);
    }
    private void handleInvalidUsername() throws JsonProcessingException {
        sendResp(ERROR, CODE_5001);
    }
    private void handleUserLoginTwice() throws JsonProcessingException {
        sendResp(ERROR, CODE_5002);
    }
    private String getLoggedInUserName(Socket clientSocket) throws JsonProcessingException {
        for (ServerHandler otherClient : serverHandler.getServer().getUsers().values()) {
            if (otherClient.checkIfUserLoggedIn() && otherClient.getClientSocket().equals(clientSocket)) {
                return otherClient.getUsername();
            }
        }
        return null;
    }
    private boolean isValidUsername(String response) throws JsonProcessingException {
        Login login = Utils.messageToObject(response);
        return login.username().matches("\\w{3,14}");
    }
    private boolean checkIfUsernameExists(String response) throws JsonProcessingException {
        Login login = Utils.messageToObject(response);
        return serverHandler.getServer().getUsers().containsKey(login.username());
    }
    @Override
    public void sendResp(String status, int code) throws JsonProcessingException {
        LoginResp loginResp = new LoginResp(status, code);
        String loginRespMessage = Utils.objectToMessage(loginResp);
        serverHandler.printAndSendResponse(loginRespMessage);
    }
}
