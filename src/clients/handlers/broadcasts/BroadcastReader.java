package clients.handlers.broadcasts;

import clients.messageInterfaces.IReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import static communications.Command.*;
import static communications.ErrorCode.*;
import static communications.Status.*;
import static communications.Message.*;
import communications.protocol.messages.Broadcast;
import communications.protocol.messages.BroadcastResp;
import communications.protocol.utils.Utils;

public class BroadcastReader implements IReader {
    public BroadcastReader() {}
    @Override
    public void readResponse(String response) throws JsonProcessingException {
        String[] parts = response.split(" ",2);
        String command = parts[0];

        switch (command) {
            case BROADCAST_RESP -> {
                BroadcastResp broadcastResp = Utils.messageToObject(response);
                switch (broadcastResp.status()) {
                    case OK -> System.out.println(MSG_06);
                    case ERROR -> {
                        switch (broadcastResp.code()) {
                            case CODE_6000 -> System.out.println(MSG_16);
                            case CODE_6001 -> System.out.println(MSG_15);
                        }
                    }
                }
            }
            case BROADCAST -> handleBroadcastMessage(response);
        }
    }
    private void handleBroadcastMessage(String response) throws JsonProcessingException {
        Broadcast broadcast = Utils.messageToObject(response);
        System.out.println(broadcast.username() + ": " + broadcast.message());
    }
}
