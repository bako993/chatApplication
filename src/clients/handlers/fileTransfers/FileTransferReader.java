package clients.handlers.fileTransfers;

import clients.handlers.ClientHandler;
import clients.messageInterfaces.IReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import static communications.Command.*;
import static communications.Message.*;
import static communications.Status.*;
import communications.protocol.messages.*;
import communications.protocol.utils.Utils;
import static communications.ErrorCode.*;

public class FileTransferReader implements IReader {

private final ClientHandler client;
    public FileTransferReader(ClientHandler client) {
        this.client = client;
    }

    @Override
    public void readResponse(String response) throws JsonProcessingException {
        String[] responseParts = response.split(" ");
        String command = responseParts[0];

        switch (command) {
            case FILE_TRANSFER -> fileTransfer(response);
            case FILE_TRANSFER_RESP -> fileTransferResponse(response);
            case ACCEPT_FILE_TRANSFER -> acceptFileTransfer(response);
            case ACCEPT_FILE_TRANSFER_RESP -> acceptFileTransferResponse(response);
            case DECLINE_FILE_TRANSFER -> declineFileTransfer(response);
            case DECLINE_FILE_TRANSFER_RESP -> declineFileTransferResponse();
        }
    }
    private void fileTransfer(String response) throws JsonProcessingException {
        FileTransfer fileTransfer = Utils.messageToObject(response);
        String sender = fileTransfer.sender();
        String filename = fileTransfer.filename();

        client.addFile(sender + ":" + filename);
        System.out.println(sender + " " + MSG_25);
    }
    private void fileTransferResponse(String response) throws JsonProcessingException {
        FileTransferResp fileTransferResp = Utils.messageToObject(response);
        switch (fileTransferResp.status()) {
            case OK -> System.out.println(MSG_06);
            case ERROR -> {
                int code = fileTransferResp.code();
                fileTransferErrorResponse(code);
            }
        }
    }
    private void fileTransferErrorResponse(int errorCode) {
        switch (errorCode) {
            case CODE_9000 -> System.out.println(MSG_18);
            case CODE_12000 -> System.out.println(MSG_35);
            case CODE_6000 -> System.out.println(MSG_16);
            case CODE_12002 -> System.out.println(MSG_33);
        }
    }
    private void acceptFileTransfer(String response) throws JsonProcessingException {
        AcceptFileTransfer acceptTransfer = Utils.messageToObject(response);
        String receiver = acceptTransfer.receiver();
        String filename = acceptTransfer.filename();

        new TransferHandler("0.0.0.0", 3939, client.getUsername()).sendFile(receiver, filename);
    }
    private void acceptFileTransferResponse(String response) throws JsonProcessingException {
        AcceptFileTransferResp acceptFileTransferResp = Utils.messageToObject(response);
        switch (acceptFileTransferResp.status()) {
            case OK -> new TransferHandler("0.0.0.0", 3939, client.getUsername()).readFile();
            case ERROR -> {
                int errorCode = acceptFileTransferResp.code();
                acceptFileTransferErrorResponse(errorCode);
            }
        }
    }
    private void acceptFileTransferErrorResponse(int errorCode) {
        switch (errorCode) {
            case CODE_9000 -> System.out.println(MSG_36);
            case CODE_12001 -> System.out.println(MSG_37);
        }
    }
    private void declineFileTransfer(String response) throws JsonProcessingException {
        DeclineFileTransfer declineTransfer = Utils.messageToObject(response);
        String receiver = declineTransfer.receiver();
        String filename = declineTransfer.filename();

        System.out.println(filename + " " + MSG_39 + " " + receiver);
    }
    private void declineFileTransferResponse() {
        System.out.println(MSG_38);
    }
}
