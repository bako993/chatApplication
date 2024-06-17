package clients.handlers.fileTransfers.receivedFiles;

import clients.messageInterfaces.ISender;
import com.fasterxml.jackson.core.JsonProcessingException;

public class FileDecisionSender implements ISender {
    private final FileReceiver fileReceiver;
    public FileDecisionSender(FileReceiver fileReceiver) {
        this.fileReceiver = fileReceiver;
    }
    @Override
    public void sendRequest() throws JsonProcessingException {
        fileReceiver.handleAcceptOrDecline();
    }
}
