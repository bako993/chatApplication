package communications.protocol.messages;

public record DeclineFileTransfer(String status, String receiver, String filename) {}
