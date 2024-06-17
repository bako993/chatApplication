package servers.handlers.fileTransfers;

import servers.FileSetup;
import java.io.*;
import java.net.Socket;
import static communications.Message.*;

public class FileHandler  extends Thread{
    private final FileSetup server;
    private final Socket clientSocket;
    private String username;

    public String getUsername() {
        return username;
    }
    public Socket getClientSocket() {
        return clientSocket;
    }
    public FileHandler(FileSetup server, Socket clientSocket) {
        try {
            this.server = server;
            this.clientSocket = clientSocket;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    @Override
    public void run() {
        try {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            BufferedInputStream bis = new BufferedInputStream(in);

            String resp = in.readUTF();
            String[] resParts = resp.split(" ");
            username = resParts[0];
            server.addUser(this);


            String transfer = in.readUTF();
            resParts = transfer.split(" ",4);
            String recipient = resParts[1];
            String fileName = resParts[2];
            String checkSum = resParts[3];

            if (server.getUsers().get(recipient) != null) {
                Socket recipientSocket = server.getUsers().get(recipient).getClientSocket();

                if (recipientSocket != null) {
                    OutputStream os = recipientSocket.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(os);
                    BufferedOutputStream bos = new BufferedOutputStream(dos);

                    dos.writeUTF(fileName + " " + checkSum);

                    byte[] buffer = new byte[8000];
                    int bytesRead;
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                    }

                    server.removeUser(this);

                    bos.flush();
                    bos.close();
                    clientSocket.close();
                } else {
                    System.err.println(MSG_78);
                }
            } else {
                System.err.println(MSG_79);
            }

        } catch (IOException e) {
            System.err.println(MSG_80 + e.getMessage());
        }
    }
}
