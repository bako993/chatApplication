package clients.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class SenderHandler extends Thread{
    private final PrintWriter writer;
    public SenderHandler(Socket socket) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            writer = new PrintWriter(outputStream, true);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    public void sendCommand(String command) {
        writer.println(command);
    }
}
