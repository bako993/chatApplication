package protocoltests;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.*;
import communications.protocol.messages.BroadcastResp;
import communications.protocol.messages.LoginResp;
import communications.protocol.utils.Utils;
import static communications.Status.OK;
import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class PacketBreakup {

    private static Properties props = new Properties();

    private Socket s;
    private BufferedReader in;
    private PrintWriter out;

    private final static int max_delta_allowed_ms = 5000;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = PacketBreakup.class.getResourceAsStream("testconfig.properties");
        props.load(in);
        in.close();
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
    void TC4_1_identFollowedByBroadcastWithMultipleFlushReturnsOk() throws JsonProcessingException {
        receiveLineWithTimeout(in); //welcome message
        out.print("LOGIN {\"username\":\"m");
        out.flush();
        out.print("yname\"}\r\nBROAD");
        out.flush();
        out.print("CAST_REQ {\"message\":\"a\"}\r\n");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        LoginResp loginResp = Utils.messageToObject(serverResponse);
        assertEquals(OK, loginResp.status());
        receiveLineWithTimeout(in);
        serverResponse = receiveLineWithTimeout(in);
        BroadcastResp broadcastResp = Utils.messageToObject(serverResponse);
        assertEquals(OK, broadcastResp.status());
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), reader::readLine);
    }

}