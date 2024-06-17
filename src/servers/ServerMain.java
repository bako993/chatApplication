package servers;

public class ServerMain {
    public static void main(String[] args) {
        int serverPort = 3838;
        int fileTransferPort = 3939;

        FileSetup fileTransfer = new FileSetup(fileTransferPort);
        Thread fileThread = new Thread(fileTransfer);
        fileThread.start();

        ServerSetup server = new ServerSetup(serverPort);
        server.startServer();
    }
}
