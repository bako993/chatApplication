package clients.handlers.logins;

import clients.handlers.ClientHandler;
import clients.messageInterfaces.ISender;
import com.fasterxml.jackson.core.JsonProcessingException;
import static communications.Message.*;
import communications.protocol.messages.Login;
import communications.protocol.utils.Utils;

public class LoginSender implements ISender {
    private final ClientHandler clientHandler;
    public LoginSender(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }
    @Override
    public void sendRequest() throws JsonProcessingException {
        handleUserLogin();
    }
    private void handleUserLogin() throws JsonProcessingException {
        if (clientHandler.isLoggedIn()) {
            Login loginReq = new Login(clientHandler.getUsername());
            String loginMessage = Utils.objectToMessage(loginReq);
            clientHandler.send(loginMessage);
        }
        while (!clientHandler.isLoggedIn()) {
            String username = clientHandler.getUserInput(MSG_63);
            attemptLogin(username);
            waitForLoginStatus();
        }
    }
    private void attemptLogin(String username) throws JsonProcessingException {
        Login loginRequest = new Login(username);
        String loginMessage = Utils.objectToMessage(loginRequest);
        clientHandler.send(loginMessage);
    }
    private void waitForLoginStatus() {
        synchronized (clientHandler) {
        try {
            clientHandler.wait(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e.getCause());
        }
        }
    }
}
