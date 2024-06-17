package communications.protocol.messages;

public record AcceptFileTransfer(String status, String receiver, String filename) {}
