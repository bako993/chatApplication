package communications.protocol.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import static communications.Command.*;
import communications.protocol.messages.*;

import java.util.HashMap;
import java.util.Map;

public class Utils {

    private final static ObjectMapper mapper = new ObjectMapper();
    private final static Map<Class<?>, String> objToNameMapping = new HashMap<>();
    static {
        objToNameMapping.put(Login.class, LOGIN);
        objToNameMapping.put(LoginResp.class, LOGIN_RESP);
        objToNameMapping.put(BroadcastReq.class, BROADCAST_REQ);
        objToNameMapping.put(BroadcastResp.class, BROADCAST_RESP);
        objToNameMapping.put(Broadcast.class, BROADCAST);
        objToNameMapping.put(Joined.class, JOINED);
        objToNameMapping.put(ParseError.class, PARSE_ERROR);
        objToNameMapping.put(Pong.class, PONG);
        objToNameMapping.put(PongError.class, PONG_ERROR);
        objToNameMapping.put(Dscn.class, DSCN);
        objToNameMapping.put(Welcome.class, WELCOME);
        objToNameMapping.put(Ping.class, PING);
        objToNameMapping.put(OnlineUserReq.class, ONLINE_USERS_REQ);
        objToNameMapping.put(OnlineUserResp.class, ONLINE_USERS_RESP);
        objToNameMapping.put(OnlineUsers.class, ONLINE_USERS);
        objToNameMapping.put(Bye.class, BYE);
        objToNameMapping.put(ByeResp.class, BYE_RESP);
        objToNameMapping.put(Left.class, LEFT);
        objToNameMapping.put(Private.class, PRIVATE);
        objToNameMapping.put(PrivateReq.class, PRIVATE_REQ);
        objToNameMapping.put(PrivateResp.class, PRIVATE_RESP);
        objToNameMapping.put(FileTransferReq.class, FILE_TRANSFER_REQ);
        objToNameMapping.put(FileTransferResp.class, FILE_TRANSFER_RESP);
        objToNameMapping.put(FileTransfer.class, FILE_TRANSFER);
        objToNameMapping.put(AcceptFileTransferReq.class, ACCEPT_FILE_TRANSFER_REQ);
        objToNameMapping.put(AcceptFileTransferResp.class, ACCEPT_FILE_TRANSFER_RESP);
        objToNameMapping.put(AcceptFileTransfer.class, ACCEPT_FILE_TRANSFER);
        objToNameMapping.put(DeclineFileTransferReq.class, DECLINE_FILE_TRANSFER_REQ);
        objToNameMapping.put(DeclineFileTransferResp.class, DECLINE_FILE_TRANSFER_RESP);
        objToNameMapping.put(DeclineFileTransfer.class, DECLINE_FILE_TRANSFER);
        objToNameMapping.put(GuessingGameInviteReq.class, GUESSING_GAME_INVITE_REQ);
        objToNameMapping.put(GuessingGameInviteResp.class, GUESSING_GAME_INVITE_RESP);
        objToNameMapping.put(GuessingGameInvite.class, GUESSING_GAME_INVITE);
        objToNameMapping.put(GuessingGameJoinReq.class, GUESSING_GAME_JOIN_REQ);
        objToNameMapping.put(GuessingGameJoinResp.class, GUESSING_GAME_JOIN_RESP);
        objToNameMapping.put(GuessReq.class, GUESS_REQ);
        objToNameMapping.put(GuessResp.class, GUESS_RESP);
        objToNameMapping.put(GuessingGameResult.class, GUESSING_GAME_RESULT);
        objToNameMapping.put(UnknownCommand.class, UNKNOWN_COMMAND);
        objToNameMapping.put(Msg.class, MSG);
    }

    public static String objectToMessage(Object object) throws JsonProcessingException {
        Class<?> clazz = object.getClass();
        String header = objToNameMapping.get(clazz);

        if (header == null) {
            throw new RuntimeException("Cannot convert this class to a message");
        }
        String body = mapper.writeValueAsString(object);
        return header + " " + body;
    }

    public static <T> T messageToObject(String message) throws JsonProcessingException {
        String[] parts = message.split(" ", 2);
        if (parts.length > 2 || parts.length == 0) {
            throw new RuntimeException("Invalid message");
        }
        String header = parts[0];
        String body = "{}";
        if (parts.length == 2) {
            body = parts[1];
        }
        Class<?> clazz = getClass(header);
        Object obj = mapper.readValue(body, clazz);
        return (T) clazz.cast(obj);
    }
    private static Class<?> getClass(String header) {
        return objToNameMapping.entrySet().stream()
                .filter(e -> e.getValue().equals(header))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find class belonging to header " + header));
    }
}
