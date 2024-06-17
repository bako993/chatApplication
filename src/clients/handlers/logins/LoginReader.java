package clients.handlers.logins;

import clients.handlers.ClientHandler;
import clients.handlers.ReaderHandler;
import clients.messageInterfaces.IReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import static communications.Status.*;
import communications.protocol.messages.LoginResp;
import communications.protocol.utils.Utils;
import static communications.ErrorCode.*;
import static communications.Message.*;

public class LoginReader implements IReader {
    private final ClientHandler clientHandler;

    public LoginReader(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    @Override
    public void readResponse(String response) throws JsonProcessingException {
        LoginResp loginResp = Utils.messageToObject(response);

        switch (loginResp.status()) {
            case OK -> successLoginResp();
            case ERROR -> {
                switch (loginResp.code()) {
                    case CODE_5000 -> System.out.println(MSG_12);
                    case CODE_5001 -> System.out.println(MSG_07);
                    case CODE_5002 -> System.out.println(MSG_08);
                }
            }
        }
    }
    private void successLoginResp() {
        clientHandler.setLoggedIn(true);
        System.out.println(MSG_01);
    }
}
