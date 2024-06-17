package protocoltests;

import com.fasterxml.jackson.core.JsonProcessingException;
import communications.protocol.messages.*;
import org.junit.jupiter.api.*;
import communications.protocol.utils.Utils;

import static communications.Message.MSG_00;
import static communications.Status.*;
import static communications.ErrorCode.*;
import java.io.*;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class SingleUserTests {

    private static Properties props = new Properties();
    private static int ping_time_ms;
    private static int ping_time_ms_delta_allowed;
    private final static int max_delta_allowed_ms = 10000;

    private Socket s;
    private BufferedReader in;
    private PrintWriter out;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = SingleUserTests.class.getResourceAsStream("testconfig.properties");
        props.load(in);
        in.close();

        ping_time_ms = Integer.parseInt(props.getProperty("ping_time_ms", "10000"));
        ping_time_ms_delta_allowed = Integer.parseInt(props.getProperty("ping_time_ms_delta_allowed", "100"));
    }

    @BeforeEach
    void setup() throws IOException {
        s = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        out = new PrintWriter(s.getOutputStream(), true);
    }

    @AfterEach
    void cleanup() throws IOException {
        s.close();
    }

    @Test
    void TC5_1_initialConnectionToServerReturnsWelcomeMessage() throws JsonProcessingException {
        String firstLine = receiveLineWithTimeout(in);
        Welcome welcome = Utils.messageToObject(firstLine);
        assertEquals(new Welcome(MSG_00), welcome);
    }

    @Test
    void TC5_2_validIdentMessageReturnsOkMessage() throws JsonProcessingException {
        receiveLineWithTimeout(in); //welcome message
        out.println(Utils.objectToMessage(new Login("myname")));
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        LoginResp loginResp = Utils.messageToObject(serverResponse);
        assertEquals(OK, loginResp.status());
    }

    @Test
    void TC5_3_invalidJsonMessageReturnsParseError() throws JsonProcessingException {
        receiveLineWithTimeout(in); //welcome message
        out.println("LOGIN {\"}");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        ParseError parseError = Utils.messageToObject(serverResponse);
        assertNotNull(parseError);
    }

    @Test
    void TC5_4_emptyJsonMessageReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(in);
        out.println("LOGIN ");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        LoginResp loginResp = Utils.messageToObject(serverResponse);
        assertEquals(new LoginResp(ERROR, CODE_5001), loginResp);
    }

    @Test
    void TC5_5_pongWithoutPingReturnsErrorMessage() throws JsonProcessingException {
        receiveLineWithTimeout(in); //welcome message
        out.println(Utils.objectToMessage(new Pong()));
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        PongError pongError = Utils.messageToObject(serverResponse);
        assertEquals(new PongError(CODE_8000), pongError);
    }

    @Test
    void TC5_6_logInTwiceReturnsErrorMessage() throws JsonProcessingException {
        receiveLineWithTimeout(in); //welcome message
        out.println(Utils.objectToMessage(new Login("first")));
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        LoginResp loginResp = Utils.messageToObject(serverResponse);
        assertEquals(OK, loginResp.status());
        receiveLineWithTimeout(in);

        out.println(Utils.objectToMessage(new Login("second")));
        out.flush();
        serverResponse = receiveLineWithTimeout(in);
        loginResp = Utils.messageToObject(serverResponse);
        assertEquals(new LoginResp(ERROR, CODE_5002), loginResp);
    }

    @Test
    void TC5_7_pingIsReceivedAtExpectedTime(TestReporter testReporter) throws JsonProcessingException {
        receiveLineWithTimeout(in); //welcome message
        out.println(Utils.objectToMessage(new Login("myname")));
        out.flush();
        receiveLineWithTimeout(in); //server response
        receiveLineWithTimeout(in);

        //Make sure the test does not hang when no response is received by using assertTimeoutPreemptively
        assertTimeoutPreemptively(ofMillis(ping_time_ms + ping_time_ms_delta_allowed + 2000), () -> {
            Instant start = Instant.now();
            String pingString = in.readLine();
            Instant finish = Instant.now();
            // Make sure the correct response is received
            Ping ping = Utils.messageToObject(pingString);
            assertNotNull(ping);
            // Also make sure the response is not received too early
            long timeElapsed = Duration.between(start, finish).toMillis();
            testReporter.publishEntry("timeElapsed", String.valueOf(timeElapsed));
            assertTrue(timeElapsed > ping_time_ms - ping_time_ms_delta_allowed);
        });
    }

    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), reader::readLine);
    }

}