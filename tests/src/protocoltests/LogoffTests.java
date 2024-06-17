package protocoltests;

import com.fasterxml.jackson.core.JsonProcessingException;
import static communications.Status.OK;
import communications.protocol.messages.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import communications.protocol.utils.Utils;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class LogoffTests {
    private static Properties props = new Properties();

    private Socket s, socketUser1, socketUser2;
    private BufferedReader in, inUser1, inUser2;
    private PrintWriter out, outUser1, outUser2;

    private final static int max_delta_allowed_ms = 5000;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = LogoffTests.class.getResourceAsStream("testconfig.properties");
        props.load(in);
        in.close();
    }

    @BeforeEach
    void setup() throws IOException {
        s = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        out = new PrintWriter(s.getOutputStream(), true);

        socketUser1 = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        inUser1 = new BufferedReader(new InputStreamReader(socketUser1.getInputStream()));
        outUser1 = new PrintWriter(socketUser1.getOutputStream(), true);

        socketUser2 = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        inUser2 = new BufferedReader(new InputStreamReader(socketUser2.getInputStream()));
        outUser2 = new PrintWriter(socketUser2.getOutputStream(), true);
    }

    @AfterEach
    void cleanup() throws IOException {
        s.close();
        socketUser1.close();
        socketUser2.close();
    }


    @Test
    void TC7_1_userLogsInAndOffReturnsOk() throws JsonProcessingException {
        receiveLineWithTimeout(in); //welcome message
        out.println(Utils.objectToMessage(new Login("mym")));
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        LoginResp loginResp = Utils.messageToObject(serverResponse);
        assertEquals(OK, loginResp.status());

        receiveLineWithTimeout(in);
        // Log off
        out.println(Utils.objectToMessage(new Bye()));
        out.flush();
        String logoffResponse = receiveLineWithTimeout(in);
        ByeResp logoffResp = Utils.messageToObject(logoffResponse);
        assertEquals(OK, logoffResp.status());
    }

    @Test
    void TC7_2_notifyUserWhenAnotherLogsOff() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // WELCOME
        receiveLineWithTimeout(inUser2);

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); // OK

        // Connect user2
        outUser2.println(Utils.objectToMessage(new Login("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); // OK
        receiveLineWithTimeout(inUser1); // Joined
        receiveLineWithTimeout(inUser2);
        receiveLineWithTimeout(inUser1);

        // Log off user1
        outUser1.println(Utils.objectToMessage(new Bye()));
        outUser1.flush();
        String logoffResponse = receiveLineWithTimeout(inUser1);
        ByeResp logoffResp = Utils.messageToObject(logoffResponse);
        assertEquals(OK, logoffResp.status());

        String user2Notification = receiveLineWithTimeout(inUser2);
        Left leftMessage = Utils.messageToObject(user2Notification);
        assertEquals("user1",leftMessage.username());

    }


    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), reader::readLine);
    }
}
