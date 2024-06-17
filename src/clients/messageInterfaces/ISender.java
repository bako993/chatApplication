package clients.messageInterfaces;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface ISender {
    void sendRequest() throws JsonProcessingException;
}
