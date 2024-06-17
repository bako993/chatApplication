package clients.messageInterfaces;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface IReader {
    void readResponse(String response) throws JsonProcessingException;
}
