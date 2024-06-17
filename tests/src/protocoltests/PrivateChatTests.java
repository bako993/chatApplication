package protocoltests;

import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.Login;
import communications.protocol.messages.Private;
import communications.protocol.messages.PrivateReq;
import communications.protocol.messages.PrivateResp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import communications.protocol.utils.Utils;
import static communications.Status.*;
import static communications.ErrorCode.*;
import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

public class PrivateChatTests {
    private static Properties props = new Properties();

    private Socket socketUser1, socketUser2;
    private BufferedReader inUser1, inUser2;
    private PrintWriter outUser1, outUser2;

    private final static int max_delta_allowed_ms = 100000;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = PrivateChatTests.class.getResourceAsStream("testconfig.properties");
        props.load(in);
        in.close();
    }

    @BeforeEach
    void setup() throws IOException {
        socketUser1 = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        inUser1 = new BufferedReader(new InputStreamReader(socketUser1.getInputStream()));
        outUser1 = new PrintWriter(socketUser1.getOutputStream(), true);

        socketUser2 = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        inUser2 = new BufferedReader(new InputStreamReader(socketUser2.getInputStream()));
        outUser2 = new PrintWriter(socketUser2.getOutputStream(), true);
    }

    @AfterEach
    void cleanup() throws IOException {
        socketUser1.close();
        socketUser2.close();

    }
    @Test
    void TC8_1_userSendsPrivateMessageSuccessfullyAndReturnsOk() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //WELCOME
        receiveLineWithTimeout(inUser2); //WELCOME
        // Connect user1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK
        receiveLineWithTimeout(inUser1);
        // Connect user2
        outUser2.println(Utils.objectToMessage(new Login("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); //OK
        receiveLineWithTimeout(inUser2); // joined
        receiveLineWithTimeout(inUser1); // joined

        // Send a private message from user1 to user2
        String messageBody = "Hello, user2!";
        outUser1.println(Utils.objectToMessage(new PrivateReq("user2", messageBody)));
        outUser1.flush();
        String response = receiveLineWithTimeout(inUser1);
        PrivateResp privateResp = Utils.messageToObject(response);
        assertEquals(OK, privateResp.status());
    }
    @Test
    void TC8_2_userSendsPrivateMessageToNonExistingUserAndReturnsNoUserFound() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //WELCOME
        // Connect user1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK
        receiveLineWithTimeout(inUser1);

        // Send a private message from user1 to user2
        String messageBody = "Hello, user2!";
        outUser1.println(Utils.objectToMessage(new PrivateReq("user2", messageBody)));
        outUser1.flush();
        String response = receiveLineWithTimeout(inUser1);
        PrivateResp privateResp = Utils.messageToObject(response);
        assertEquals(new PrivateResp(ERROR, CODE_9000), privateResp);
    }
    @Test
    void TC8_3_userSendsEmptyPrivateMessageAndReturnsEmptyBodyMessage() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //WELCOME
        receiveLineWithTimeout(inUser2); //WELCOME
        // Connect user1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK
        receiveLineWithTimeout(inUser1);
        // Connect user2
        outUser2.println(Utils.objectToMessage(new Login("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); //OK
        receiveLineWithTimeout(inUser1); // joined
        receiveLineWithTimeout(inUser2);

        // Send a private message from user1 to user2
        String messageBody = "";
        outUser1.println(Utils.objectToMessage(new PrivateReq("user2", messageBody)));
        outUser1.flush();
        String response = receiveLineWithTimeout(inUser1);
        PrivateResp privateResp = Utils.messageToObject(response);
        assertEquals(new PrivateResp(ERROR, CODE_6001), privateResp);
    }
    @Test
    void TC8_4_userSendsPrivateMessageToSelfAndReturnsCannotMessageYourself() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //WELCOME
        // Connect user1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK
        receiveLineWithTimeout(inUser1);

        // Send a private message from user1 to user2
        String messageBody = "Hello, user1!";
        outUser1.println(Utils.objectToMessage(new PrivateReq("user1", messageBody)));
        outUser1.flush();
        String response = receiveLineWithTimeout(inUser1);
        PrivateResp privateResp = Utils.messageToObject(response);
        assertEquals(new PrivateResp(ERROR, CODE_9001), privateResp);
    }
    @Test
    void TC8_5_privateMessageIsReceivedByOtherConnectedUser() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //WELCOME
        receiveLineWithTimeout(inUser2); //WELCOME
        // Connect user1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK
        receiveLineWithTimeout(inUser1);
        // Connect user2
        outUser2.println(Utils.objectToMessage(new Login("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); //OK
        receiveLineWithTimeout(inUser1); // joined
        receiveLineWithTimeout(inUser2);

        // Send a private message from user1 to user2
        String messageBody = "Hello, user2!";
        outUser1.println(Utils.objectToMessage(new PrivateReq("user2", messageBody)));
        outUser1.flush();
        String response = receiveLineWithTimeout(inUser1);
        PrivateResp privateResp = Utils.messageToObject(response);
        assertEquals(OK, privateResp.status());

        // Receive the private message from user1 on user2's side
        String receivedMessageUser2 = receiveLineWithTimeout(inUser2);
        Private privateMessageUser2 = Utils.messageToObject(receivedMessageUser2);

        assertEquals("user1", privateMessageUser2.sender());
        assertEquals(messageBody, privateMessageUser2.message());
    }
    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), reader::readLine);
    }
}
