package protocoltests;

import com.fasterxml.jackson.core.JsonProcessingException;

import static communications.ErrorCode.*;
import static communications.Status.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import communications.protocol.messages.*;
import communications.protocol.utils.Utils;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class BroadcastTests {
    private static Properties props = new Properties();

    private Socket socketUser1, socketUser2;
    private BufferedReader inUser1, inUser2;
    private PrintWriter outUser1, outUser2;

    private final static int max_delta_allowed_ms = 100000;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = BroadcastTests.class.getResourceAsStream("testconfig.properties");
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
    void TC9_1_loggedInUserSendsEmptyBodyBroadcastMessageAndReturnsEmptyBodyError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //WELCOME
        receiveLineWithTimeout(inUser2); //WELCOME

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK
        receiveLineWithTimeout(inUser1); //JOINED
        // Connect user2
        outUser2.println(Utils.objectToMessage(new Login("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); //OK
        receiveLineWithTimeout(inUser1); //JOINED
        receiveLineWithTimeout(inUser2); //JOINED

        outUser1.println(Utils.objectToMessage(new BroadcastReq(" ")));
        outUser1.flush();

        String fromUser1 = receiveLineWithTimeout(inUser1);
        BroadcastResp broadcastResp = Utils.messageToObject(fromUser1);
        assertEquals(new BroadcastResp(ERROR, CODE_6001),broadcastResp);
    }
    @Test
    void TC9_2_nonLoggedInUserSendsBroadcastMessageAndReturnsUserNotLoggedInError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //WELCOME

        outUser1.println(Utils.objectToMessage(new BroadcastReq("messageFromNonLoggedInUser")));
        outUser1.flush();

        String nonLoggedInUser = receiveLineWithTimeout(inUser1);
        BroadcastResp broadcastResp = Utils.messageToObject(nonLoggedInUser);
        assertEquals(new BroadcastResp(ERROR, CODE_6000), broadcastResp);

    }
    @Test
    void TC9_3_userSendsBroadcastMessageAndOtherOnlineUsersReceivesMessage() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //WELCOME
        receiveLineWithTimeout(inUser2); //WELCOME

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK
        receiveLineWithTimeout(inUser1); //JOINED
        // Connect user2
        outUser2.println(Utils.objectToMessage(new Login("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); //OK
        receiveLineWithTimeout(inUser1); //JOINED
        receiveLineWithTimeout(inUser2); //JOINED

        String message = "Hello!";
        outUser1.println(Utils.objectToMessage(new BroadcastReq(message)));
        outUser1.flush();

        String fromUser1 = receiveLineWithTimeout(inUser1);
        BroadcastResp broadcastResp = Utils.messageToObject(fromUser1);
        assertEquals(new BroadcastResp(OK, CODE_0000), broadcastResp);

        String broadcastMessage = receiveLineWithTimeout(inUser2);
        Broadcast broadcast = Utils.messageToObject(broadcastMessage);
        assertEquals(new Broadcast("user1",message),broadcast);
    }


    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), reader::readLine);
    }
}
