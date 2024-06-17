package clients.menu;

public enum MenuOption {
    USER_LOGIN("1","Login."),
    VIEW_ONLINE_USERS("2", "View online users."),
    BROADCAST_MESSAGE("3", "Broadcast message."),
    PRIVATE_MESSAGE("4", "Private message"),
    SEND_FILE("5", "Send file."),
    VIEW_RECEIVED_FILES("6", "View Received files"),
    EXIT("7", "Exit SaxionChat."),
    REQUEST_GUESSING_GAME("8","Request Guessing Game"),
    JOIN_GUESSING_GAME("9","Join Guessing Game."),
    PLAY_GUESSING_GAME("10","Play Guessing Game"),
    HELP("help", "Display menu.");

    private final String code;
    private final String description;

    MenuOption(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
