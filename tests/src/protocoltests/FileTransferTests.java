package protocoltests;

import com.fasterxml.jackson.core.JsonProcessingException;
import static communications.Status.*;
import static communications.ErrorCode.*;
import communications.protocol.messages.*;
import communications.protocol.utils.Utils;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;
import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileTransferTests {
    private static Properties props = new Properties();

    private Socket socketUser1, socketUser2;
    private BufferedReader inUser1, inUser2;
    private PrintWriter outUser1, outUser2;

    private final static int max_delta_allowed_ms = 10000;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = FileTransferTests.class.getResourceAsStream("testconfig.properties");
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
    void TC10_1_UserSendsFileToNonExistingUserAndReturnsUserNotFound() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1);

        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1);
        receiveLineWithTimeout(inUser1);

        outUser1.println(Utils.objectToMessage(new FileTransferReq("user2", "test.txt")));
        outUser1.flush();
        String responseUser1 = receiveLineWithTimeout(inUser1);
        FileTransferResp transferResp = Utils.messageToObject(responseUser1);
        assertEquals(new FileTransferResp(ERROR, CODE_9000),transferResp);
    }
    @Test
    void TC10_2_UserSendsFileTransferRequestAndReturnsOk() throws JsonProcessingException {
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
        receiveLineWithTimeout(inUser2);
        receiveLineWithTimeout(inUser1);

        outUser1.println(Utils.objectToMessage(new FileTransferReq("user2", "test.txt")));
        outUser1.flush();
        String responseUser1 = receiveLineWithTimeout(inUser1);
        FileTransferResp transferResp = Utils.messageToObject(responseUser1);
        assertEquals(OK,transferResp.status());
    }
    @Test
    void TC10_3_User1SendsFileTransferRequestSuccessfullyAndUser2ReceivesRequest() throws JsonProcessingException {
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
        receiveLineWithTimeout(inUser2);
        receiveLineWithTimeout(inUser1);

        outUser1.println(Utils.objectToMessage(new FileTransferReq("user2", "test.txt")));
        outUser1.flush();
        String responseUser1 = receiveLineWithTimeout(inUser1);
        FileTransferResp transferResp = Utils.messageToObject(responseUser1);
        assertEquals(OK,transferResp.status());

        String responseUser2 = receiveLineWithTimeout(inUser2);
        FileTransfer transferRes2 = Utils.messageToObject(responseUser2);
        assertEquals(new FileTransfer("user1","test.txt"),transferRes2);
    }
    @Test
    void TC10_4_User1SendsFileTransferRequestToUser1AndReturnsCannotSendFileToYourself() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //WELCOME
        receiveLineWithTimeout(inUser2); //WELCOME
        // Connect user1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK
        receiveLineWithTimeout(inUser1);

        outUser1.println(Utils.objectToMessage(new FileTransferReq("user1", "test.txt")));
        outUser1.flush();
        String responseUser1 = receiveLineWithTimeout(inUser1);
        FileTransferResp transferResp = Utils.messageToObject(responseUser1);
        assertEquals(new FileTransferResp(ERROR, CODE_12000),transferResp);
    }
    @Test
    void TC10_5_User2AcceptsFileTransferRequestsAndUser1ReceivesAccepted() throws JsonProcessingException {
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
        receiveLineWithTimeout(inUser2);
        receiveLineWithTimeout(inUser1);

        outUser1.println(Utils.objectToMessage(new FileTransferReq("user2", "text2.txt")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1);
        receiveLineWithTimeout(inUser2);

        outUser2.println(Utils.objectToMessage(new AcceptFileTransferReq("user1","text2.txt")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2);

        String acceptResp = receiveLineWithTimeout(inUser1);
        AcceptFileTransfer acceptFileTransfer = Utils.messageToObject(acceptResp);
        assertEquals(new AcceptFileTransfer(ACCEPTED,"user2","text2.txt"),acceptFileTransfer);
    }
    @Test
    void TC10_6_User2AcceptsFileTransferRequestsAndReturnsUser1NotFound() throws IOException {
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
        receiveLineWithTimeout(inUser2);
        receiveLineWithTimeout(inUser1);

        outUser1.println(Utils.objectToMessage(new FileTransferReq("user2", "text2.txt")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1);
        receiveLineWithTimeout(inUser2);

        // Disconnect user1
        outUser1.println(Utils.objectToMessage(new Bye()));
        outUser1.flush();
        receiveLineWithTimeout(inUser1);  // ok bye
        receiveLineWithTimeout(inUser2); // left

        // user2 accepts file from user1
        outUser2.println(Utils.objectToMessage(new AcceptFileTransferReq("user1","text2.txt")));
        outUser2.flush();

        String acceptResp = receiveLineWithTimeout(inUser2);
        AcceptFileTransferResp acceptFileTransferResp = Utils.messageToObject(acceptResp);
        assertEquals(new AcceptFileTransferResp(ERROR, CODE_9000),acceptFileTransferResp);
    }
    @Test
    void TC10_7_User2DeclineFileTransferAndReturnsOk() throws JsonProcessingException {
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
        receiveLineWithTimeout(inUser2);
        receiveLineWithTimeout(inUser1);

        outUser1.println(Utils.objectToMessage(new FileTransferReq("user2", "text2.txt")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1);
        receiveLineWithTimeout(inUser2);

        outUser2.println(Utils.objectToMessage(new DeclineFileTransferReq("user1","text2.txt")));
        outUser2.flush();
        String declineResp = receiveLineWithTimeout(inUser2);
        DeclineFileTransferResp declineFileTransferResp = Utils.messageToObject(declineResp);
        assertEquals((OK),declineFileTransferResp.status());

    }
    @Test
    void TC10_8_User2DeclineFileTransferAndUser1ReceivesDeclined() throws JsonProcessingException {
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
        receiveLineWithTimeout(inUser2);
        receiveLineWithTimeout(inUser1);

        outUser1.println(Utils.objectToMessage(new FileTransferReq("user2", "text2.txt")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1);
        receiveLineWithTimeout(inUser2);

        outUser2.println(Utils.objectToMessage(new DeclineFileTransferReq("user1","text2.txt")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2);

        String declineResp = receiveLineWithTimeout(inUser1);
        DeclineFileTransfer declineFileTransfer = Utils.messageToObject(declineResp);
        assertEquals((DECLINED),declineFileTransfer.status());
    }
    @Test
    void TC10_9_User1SendsFileTransferRequestToUser2WithoutAddingReceiverNameAndReturnsEmptyBody() throws JsonProcessingException {
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
        receiveLineWithTimeout(inUser2);
        receiveLineWithTimeout(inUser1);

        outUser1.println(Utils.objectToMessage(new FileTransferReq("", "test.txt")));
        outUser1.flush();
        String responseUser1 = receiveLineWithTimeout(inUser1);
        FileTransferResp transferResp = Utils.messageToObject(responseUser1);
        assertEquals(new FileTransferResp(ERROR, CODE_12002),transferResp);
    }

    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), reader::readLine);
    }
}
