package protocoltests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import static communications.Status.*;
import static communications.ErrorCode.*;
import communications.protocol.messages.Login;
import communications.protocol.messages.OnlineUserReq;
import communications.protocol.messages.OnlineUserResp;
import communications.protocol.messages.OnlineUsers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import communications.protocol.utils.Utils;

import java.io.*;
import java.net.Socket;
import java.util.*;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

public class OnlineUserTests {
    private static Properties props = new Properties();

    private Socket s, s2;
    private BufferedReader in, in2;
    private PrintWriter out, out2;
    private final static int max_delta_allowed_ms = 10000;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = OnlineUserTests.class.getResourceAsStream("testconfig.properties");
        props.load(in);
        in.close();
    }

    @BeforeEach
    void setup() throws IOException {
        s = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        out = new PrintWriter(s.getOutputStream(), true);

        s2 = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        in2 = new BufferedReader(new InputStreamReader(s2.getInputStream()));
        out2 = new PrintWriter(s2.getOutputStream(), true);

    }

    @AfterEach
    void cleanup() throws IOException {
        s.close();
        s2.close();
    }

    @Test
    void TC6_1_loggedInUserRequestOnlineUsersAndReturnsNoUsers() throws JsonProcessingException {
        receiveLineWithTimeout(in); //welcome message
        out.println(Utils.objectToMessage(new Login("user1")));
        out.flush();
        receiveLineWithTimeout(in);
        receiveLineWithTimeout(in);

        out.println(Utils.objectToMessage(new OnlineUserReq("user1")));
        out.flush();
        String onlineUsersResponse = receiveLineWithTimeout(in);
        OnlineUserResp onlineUserResp = Utils.messageToObject(onlineUsersResponse);
        assertEquals(new OnlineUserResp(ERROR, CODE_10000),onlineUserResp);
    }
    @Test
    void TC6_2_secondLoggedInUserRequestOnlineUsersAndReturnsOkAndListOfOnlineUsers() throws IOException {
        receiveLineWithTimeout(in); //welcome message
        receiveLineWithTimeout(in2); //welcome message
        out.println(Utils.objectToMessage(new Login("user1")));
        out.flush();
        receiveLineWithTimeout(in);

        out2.println(Utils.objectToMessage(new Login("user2")));
        out2.flush();
        receiveLineWithTimeout(in2);
        receiveLineWithTimeout(in2);
        receiveLineWithTimeout(in);

        out2.println(Utils.objectToMessage(new OnlineUserReq("user2")));
        out2.flush();
        String onlineUsersResponse = receiveLineWithTimeout(in2);
        OnlineUserResp onlineUserResp = Utils.messageToObject(onlineUsersResponse);
        assertEquals(OK,onlineUserResp.status());

        String onlineUsers = receiveLineWithTimeout(in2);
        OnlineUsers onlineUsersList = Utils.messageToObject(onlineUsers);
        List<String> expectedList = List.of("user1");
        List<String> actualList = onlineUsersList.onlineUsers();
        assertEquals(expectedList, actualList);
    }
    @Test
    void TC6_3_nonLoggedInUserRequestOnlineUserAndReturnsUserNotLoggedIn() throws JsonProcessingException {
        receiveLineWithTimeout(in); //welcome message
        out.println(Utils.objectToMessage(new OnlineUserReq("user1")));
        out.flush();
        String onlineUsersResponse = receiveLineWithTimeout(in);
        OnlineUserResp onlineUserResp = Utils.messageToObject(onlineUsersResponse);
        assertEquals(new OnlineUserResp(ERROR, CODE_6000),onlineUserResp);
    }

    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), reader::readLine);
    }
}
