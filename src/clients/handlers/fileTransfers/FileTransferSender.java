package clients.handlers.fileTransfers;

import clients.handlers.ClientHandler;
import clients.messageInterfaces.ISender;
import com.fasterxml.jackson.core.JsonProcessingException;
import static communications.Message.*;
import communications.protocol.messages.FileTransferReq;
import communications.protocol.utils.Utils;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;

public class FileTransferSender implements ISender {
    private final ClientHandler client;
    public FileTransferSender(ClientHandler client) {
        this.client = client;
    }
    @Override
    public void sendRequest() {
        try {
            client.send(requestFileTransfer());
        } catch (FileNotFoundException | JsonProcessingException e) {
            System.err.println(e.getMessage());
        }
    }
    private String requestFileTransfer() throws FileNotFoundException, JsonProcessingException {
        String receiver = client.getUserInput(MSG_10).trim();
        String filename = client.getUserInput(MSG_41).trim();

        File file = new File("src/files/" + filename);

        if (!file.exists()) {
            throw new FileNotFoundException(MSG_34);
        }

        long fileSizeInBytes = file.length();
        String fileSize = formatFileSize(fileSizeInBytes);
        System.out.println(MSG_58 + fileSize);

        FileTransferReq fileTransferReq = new FileTransferReq(receiver,filename);
        return Utils.objectToMessage(fileTransferReq);
    }
    private String formatFileSize(long fileSizeInBytes) {
        DecimalFormat df = new DecimalFormat("#.##");

        double fileSizeInKB = (double) fileSizeInBytes / 1024;
        if (fileSizeInKB < 1024) {
            return df.format(fileSizeInKB) + " KB";
        }
        double fileSizeInMB = fileSizeInKB / 1024;
        return df.format(fileSizeInMB) + " MB";
    }

}
