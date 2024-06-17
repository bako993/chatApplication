package clients;

import clients.handlers.ClientHandler;
import clients.handlers.ReaderHandler;
import clients.handlers.SenderHandler;
import java.io.IOException;
import java.net.Socket;
import static communications.Message.MSG_11;

public class ClientSetup {
    private final String serverAddress;
    private final int serverPort;
    private Socket socket;
    private SenderHandler senderHandler;
    private ReaderHandler readerHandler;
    private final ClientHandler clientHandler;

    public ClientSetup(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        clientHandler = new ClientHandler(this);

        connect();
    }

    public SenderHandler getSenderHandler() {
        return senderHandler;
    }

    public ReaderHandler getReaderHandler() {
        return readerHandler;
    }

    private void startSender() {
        senderHandler = new SenderHandler(socket);
        senderHandler.start();
    }
    private void startReader() {
        readerHandler = new ReaderHandler(socket,clientHandler);
        readerHandler.start();
    }
    public ClientHandler getClientHandler() {
        return clientHandler;
    }
    private void connect() {
        try {
            socket = new Socket(serverAddress, serverPort);

            startReader();
            startSender();
        } catch (IOException e) {
            System.err.println(MSG_11);
        }
    }
    public Socket getSocket() {
        return socket;
    }
}
