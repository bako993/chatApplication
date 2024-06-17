package clients.handlers.privateChats;

import clients.messageInterfaces.IReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.Private;
import communications.protocol.messages.PrivateResp;
import communications.protocol.utils.Utils;

import static communications.Command.*;
import static communications.ErrorCode.*;
import static communications.Message.*;
import static communications.Status.*;

public class PrivateChatReader implements IReader {
    @Override
    public void readResponse(String response) throws JsonProcessingException {
        String[] responseParts = response.split(" ",2);
        String command = responseParts[0];
        switch (command) {
            case PRIVATE_RESP -> handlePrivateChatResp(response);
            case PRIVATE -> handlePrivateChat(response);
        }
    }
    private void handlePrivateChatResp(String response) throws JsonProcessingException {
        PrivateResp privateResp = Utils.messageToObject(response);
        switch (privateResp.status()) {
            case OK -> System.out.println(MSG_06);
            case ERROR -> {
                switch (privateResp.code()) {
                    case CODE_6000 -> System.out.println(MSG_16);
                    case CODE_9001 -> System.out.println(MSG_19);
                    case CODE_6001 -> System.out.println(MSG_15);
                    case CODE_9000 -> System.out.println(MSG_18);
                }
            }
        }
    }
    private void handlePrivateChat(String response) throws JsonProcessingException {
        Private privateMessage = Utils.messageToObject(response);
        String sender = privateMessage.sender();
        String message = privateMessage.message();
        System.out.println(sender + ": " + message);
    }
}
