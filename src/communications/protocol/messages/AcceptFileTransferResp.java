package communications.protocol.messages;

import com.fasterxml.jackson.annotation.JsonInclude;

public record AcceptFileTransferResp(String status, int code) {}
