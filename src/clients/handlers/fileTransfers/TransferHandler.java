package clients.handlers.fileTransfers;

import communications.Command;
import static communications.Message.*;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;

public class TransferHandler {
    private DataInputStream in;
    private DataOutputStream os;
    private BufferedInputStream bis;
    private BufferedOutputStream bos;
    private Socket socket;
    private String username;
    private static int uniqueCounter = 0;
    public TransferHandler(String serverAddress, int serverPort, String username) {
        try {
            socket = new Socket(serverAddress, serverPort);

            in = new DataInputStream(socket.getInputStream());
            bis = new BufferedInputStream(in);

            os = new DataOutputStream(socket.getOutputStream());
            this.username = username;

            os.writeUTF(username);
            bos = new BufferedOutputStream(os);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    public void sendFile(String receiver, String filename) {
        System.out.println(receiver + " " + MSG_27 +  " " + filename + " " + MSG_29);
        Thread senderThread = new Thread(() -> {
            try {
                File file = new File("src/files/" + filename);
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);

                MessageDigest md = MessageDigest.getInstance("SHA-256");

                byte[] buffer = new byte[8000];
                int bytesRead;

                while ((bytesRead = bis.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }

                byte[] digest = md.digest();
                String myChecksum = bytesToHex(digest);

                os.writeUTF(Command.FILE_TRANSFER + " " + receiver + " " + filename + " " + myChecksum);

                bis = new BufferedInputStream(new FileInputStream(file));
                while ((bytesRead = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
                System.out.println(MSG_26);

                bos.flush();
                bos.close();
                os.flush();
                os.close();
                in.close();
                fis.close();
                bis.close();
                socket.close();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        });

        senderThread.start();
    }

    public void readFile() {
        System.out.println(MSG_31);
        Thread readerThread = new Thread(() -> {
            try {
                String fileInfo = in.readUTF();
                String[] fileInfoParts = fileInfo.split(" ",2);
                String fileName = fileInfoParts[0];
                String fileCheckSum = fileInfoParts[1];

                String uniqueNumber = generateUniqueNumber();
                String filePath = username + "_" + uniqueNumber + "_" + fileName;
                FileOutputStream fos = new FileOutputStream(filePath);

                byte[] buffer = new byte[8000];
                int bytesRead;

                MessageDigest md = MessageDigest.getInstance("SHA-256");

                while ((bytesRead = bis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    md.update(buffer, 0, bytesRead);
                }

                byte[] digest = md.digest();
                String checkSumAfterDownload = bytesToHex(digest);

                if (!checkSumAfterDownload.equals(fileCheckSum)) {
                    throw new Exception(MSG_32);
                }
                System.out.println(MSG_30);

                in.close();
                bis.close();
                os.flush();
                os.close();
                fos.flush();
                fos.close();
                socket.close();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        });

        readerThread.start();
    }
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }
    private synchronized String generateUniqueNumber() {
        uniqueCounter++;
        return String.valueOf(uniqueCounter);
    }
}
