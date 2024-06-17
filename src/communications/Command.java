package communications;

public class Command {
    public static final String WELCOME = "WELCOME";
    public static final String LOGIN = "LOGIN";
    public static final String LOGIN_RESP = "LOGIN_RESP";
    public static final String BYE = "BYE";
    public static final String BYE_RESP = "BYE_RESP";
    public static final String DSCN = "DSCN";
    public static final String LEFT = "LEFT";
    public static final String JOINED = "JOINED";
    public static final String PARSE_ERROR = "PARSE_ERROR";
    public static final String ONLINE_USERS = "ONLINE_USERS";
    public static final String ONLINE_USERS_REQ = "ONLINE_USERS_REQ";
    public static final String ONLINE_USERS_RESP = "ONLINE_USERS_RESP";
    public static final String MSG = "MSG";
    public static final String PING = "PING";
    public static final String PONG = "PONG";
    public static final String PONG_ERROR = "PONG_ERROR";
    public static final String BROADCAST_REQ = "BROADCAST_REQ";
    public static final String BROADCAST_RESP = "BROADCAST_RESP";
    public static final String BROADCAST = "BROADCAST";
    public static final String PRIVATE = "PRIVATE";
    public static final String PRIVATE_REQ = "PRIVATE_REQ";
    public static final String PRIVATE_RESP = "PRIVATE_RESP";
    public static final String FILE_TRANSFER = "FILE_TRANSFER";
    public static final String FILE_TRANSFER_REQ = "FILE_TRANSFER_REQ";
    public static final String FILE_TRANSFER_RESP = "FILE_TRANSFER_RESP";
    public static final String ACCEPT_FILE_TRANSFER_REQ = "ACCEPT_FILE_TRANSFER_REQ";
    public static final String ACCEPT_FILE_TRANSFER_RESP = "ACCEPT_FILE_TRANSFER_RESP";
    public static final String ACCEPT_FILE_TRANSFER = "ACCEPT_FILE_TRANSFER";
    public static final String DECLINE_FILE_TRANSFER_REQ = "DECLINE_FILE_TRANSFER_REQ";
    public static final String DECLINE_FILE_TRANSFER_RESP = "DECLINE_FILE_TRANSFER_RESP";
    public static final String DECLINE_FILE_TRANSFER = "DECLINE_FILE_TRANSFER";
    public static final String GUESSING_GAME_INVITE_REQ = "GUESSING_GAME_INVITE_REQ";
    public static final String GUESSING_GAME_INVITE_RESP = "GUESSING_GAME_INVITE_RESP";
    public static final String GUESSING_GAME_INVITE = "GUESSING_GAME_INVITE";
    public static final String GUESSING_GAME_JOIN_REQ = "GUESSING_GAME_JOIN_REQ";
    public static final String GUESSING_GAME_JOIN_RESP = "GUESSING_GAME_JOIN_RESP";
    public static final String GUESSING_GAME_RESULT = "GUESSING_GAME_RESULT";
    public static final String GUESS_REQ = "GUESS_REQ";
    public static final String GUESS_RESP = "GUESS_RESP";
    public static final String UNKNOWN_COMMAND = "UNKNOWN_COMMAND";
}
