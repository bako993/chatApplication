package protocoltests;

import com.fasterxml.jackson.core.JsonProcessingException;
import static communications.Status.*;
import static communications.ErrorCode.*;
import communications.protocol.messages.*;
import org.junit.jupiter.api.*;
import communications.protocol.utils.Utils;

import java.io.*;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GuessingGameTests {
    private static Properties props = new Properties();

    private Socket socketUser1, socketUser2, socketUser3;
    private BufferedReader inUser1, inUser2, inUser3;
    private PrintWriter outUser1, outUser2, outUser3;

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

        socketUser3 = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        inUser3 = new BufferedReader(new InputStreamReader(socketUser3.getInputStream()));
        outUser3 = new PrintWriter(socketUser3.getOutputStream(), true);
    }

    @AfterEach
    void cleanup() throws IOException {
        socketUser1.close();
        socketUser2.close();
        socketUser3.close();
    }

    @Test
    void TC11_1_userRequestGameAndReturnsOk() throws JsonProcessingException, InterruptedException {
        receiveLineWithTimeout(inUser1); //WELCOME
        receiveLineWithTimeout(inUser2); //WELCOME

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK
        receiveLineWithTimeout(inUser1); //JOINED

        // Initiate guessing game
        outUser1.println(Utils.objectToMessage(new GuessingGameInviteReq("user1")));
        outUser1.flush();
        String serverResponse = receiveLineWithTimeout(inUser1);
        GuessingGameInviteResp inviteResp = Utils.messageToObject(serverResponse);
        assertEquals(new GuessingGameInviteResp(OK, CODE_0000),inviteResp);
        Thread.sleep(10000);

    }
    @Test
    void TC11_2_userRequestGameAndOnlineUsersReceivesInvite() throws JsonProcessingException, InterruptedException {
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

        // Initiate guessing game
        outUser1.println(Utils.objectToMessage(new GuessingGameInviteReq("user1")));
        outUser1.flush();
        String serverResponse = receiveLineWithTimeout(inUser1);
        GuessingGameInviteResp inviteResp = Utils.messageToObject(serverResponse);
        assertEquals(new GuessingGameInviteResp(OK, CODE_0000),inviteResp);

        String gameInviteUser1 = receiveLineWithTimeout(inUser1);
        String gameInviteUser2 = receiveLineWithTimeout(inUser2);


        GuessingGameInvite guessingGameInvite1 = Utils.messageToObject(gameInviteUser1);
        assertEquals(new GuessingGameInvite("user1"),guessingGameInvite1);

        GuessingGameInvite guessingGameInvite2 = Utils.messageToObject(gameInviteUser2);
        assertEquals(new GuessingGameInvite("user1"),guessingGameInvite2);

        Thread.sleep(12000);
    }
    @Test
    void TC11_3_userRequestGameAndReturnsNotEnoughUsersAfterTenSeconds() throws JsonProcessingException, InterruptedException {
        receiveLineWithTimeout(inUser1);

        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1);
        receiveLineWithTimeout(inUser1);

        outUser1.println(Utils.objectToMessage(new GuessingGameInviteReq("user1")));
        outUser1.flush();
        String serverResponse = receiveLineWithTimeout(inUser1);
        GuessingGameInviteResp inviteSuccessResp = Utils.messageToObject(serverResponse);
        assertEquals(new GuessingGameInviteResp(OK, CODE_0000), inviteSuccessResp);

        String inviteResp = receiveLineWithTimeout(inUser1);
        GuessingGameInvite guessingGameInvite = Utils.messageToObject(inviteResp);
        assertEquals(new GuessingGameInvite("user1"), guessingGameInvite);

        Thread.sleep(11000);

        String inviteErrorResp = receiveLineWithTimeout(inUser1);

        if (inviteErrorResp.equals("PING {}")) {
            inviteErrorResp = receiveLineWithTimeout(inUser1);
        }
        if (inviteErrorResp.equals("DSCN {\"reason\":7000}")) {
            inviteErrorResp = receiveLineWithTimeout(inUser1);
        }

        GuessingGameInviteResp inviteErrorRespMessage = Utils.messageToObject(inviteErrorResp);
        assertEquals(new GuessingGameInviteResp(ERROR, CODE_11000), inviteErrorRespMessage);

        Thread.sleep(11000);
    }
    @Test
    void TC11_4_secondUserRequestGameWhileGameIsAlreadyRequestedByFirstUserAndReturnsNotIdle(TestReporter testReporter) throws JsonProcessingException, InterruptedException {
//        Thread.sleep(10000);
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

        outUser1.println(Utils.objectToMessage(new GuessingGameInviteReq("user1")));
        outUser1.flush();
        String serverResponse = receiveLineWithTimeout(inUser1);
        GuessingGameInviteResp inviteSuccessResp = Utils.messageToObject(serverResponse);
        assertEquals(new GuessingGameInviteResp(OK, CODE_0000), inviteSuccessResp);

        String gameInviteUser1 = receiveLineWithTimeout(inUser1);
        GuessingGameInvite guessingGameInvite1 = Utils.messageToObject(gameInviteUser1);
        assertEquals(new GuessingGameInvite("user1"),guessingGameInvite1);

        String gameInviteUser2 = receiveLineWithTimeout(inUser2);
        GuessingGameInvite guessingGameInvite2 = Utils.messageToObject(gameInviteUser2);
        assertEquals(new GuessingGameInvite("user1"),guessingGameInvite2);

        assertTimeoutPreemptively(Duration.ofMillis(11000), () -> {
            Instant start = Instant.now();

            outUser1.println(Utils.objectToMessage(new GuessingGameInviteReq("user1")));
            outUser1.flush();
            String serverResp = receiveLineWithTimeout(inUser1);
            GuessingGameInviteResp inviteErrorResp = Utils.messageToObject(serverResp);
            assertEquals(new GuessingGameInviteResp(ERROR, CODE_11002), inviteErrorResp);
            Instant finish = Instant.now();

            long timeElapsed = Duration.between(start, finish).toMillis();
            testReporter.publishEntry("timeElapsed", String.valueOf(timeElapsed));
            assertTrue(timeElapsed <= 11000);
        });

        Thread.sleep(11000);
    }
    @Test
    void TC11_5_userRequestsToJoinGameWithoutReceivingGameInvitationAndReturnsInvitationIsRequired() throws JsonProcessingException, InterruptedException {
//        Thread.sleep(10000);
        receiveLineWithTimeout(inUser1);

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1);
        receiveLineWithTimeout(inUser1);

        outUser1.println(Utils.objectToMessage(new GuessingGameJoinReq("user1")));
        outUser1.flush();
        String serverResp = receiveLineWithTimeout(inUser1);
        GuessingGameJoinResp guessingGameJoinResp = Utils.messageToObject(serverResp);
        assertEquals(new GuessingGameJoinResp(ERROR, CODE_11004),guessingGameJoinResp);
        Thread.sleep(11000);

    }
    @Test
    void TC11_6_userAttemptsToJoinGameAfterTenSecondsAndReturnsTimeOutError() throws JsonProcessingException, InterruptedException {
//        Thread.sleep(10000);
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

        outUser3.println(Utils.objectToMessage(new Login("user3")));
        outUser3.flush();
        receiveLineWithTimeout(inUser3); //OK
        receiveLineWithTimeout(inUser1); //JOINED
        receiveLineWithTimeout(inUser2); //JOINED
        receiveLineWithTimeout(inUser3); //JOINED
        receiveLineWithTimeout(inUser3); //JOINED

        // send guessing game invite
        outUser1.println(Utils.objectToMessage(new GuessingGameInviteReq("user1")));
        outUser1.flush();
        String serverResponse = receiveLineWithTimeout(inUser1);

        GuessingGameInviteResp inviteSuccessResp = Utils.messageToObject(serverResponse);
        assertEquals(new GuessingGameInviteResp(OK, CODE_0000), inviteSuccessResp);

        // receive invite
        String gameInviteUser1 = receiveLineWithTimeout(inUser1);
        GuessingGameInvite guessingGameInvite1 = Utils.messageToObject(gameInviteUser1);
        assertEquals(new GuessingGameInvite("user1"),guessingGameInvite1);

        // receive invite
        String gameInviteUser2 = receiveLineWithTimeout(inUser2);
        GuessingGameInvite guessingGameInvite2 = Utils.messageToObject(gameInviteUser2);
        assertEquals(new GuessingGameInvite("user1"),guessingGameInvite2);

        String gameInviteUser3 = receiveLineWithTimeout(inUser3);
        GuessingGameInvite guessingGameInvite3 = Utils.messageToObject(gameInviteUser3);
        assertEquals(new GuessingGameInvite("user1"),guessingGameInvite3);

        // User1 joins within ten seconds
        outUser1.println(Utils.objectToMessage(new GuessingGameJoinReq("user1")));
        outUser1.flush();
        String serverJoinResp1 = receiveLineWithTimeout(inUser1);
        GuessingGameJoinResp successJoinResp1 = Utils.messageToObject(serverJoinResp1);
        assertEquals(new GuessingGameJoinResp(OK, CODE_0000),successJoinResp1);

        // User2 joins within ten seconds
        outUser2.println(Utils.objectToMessage(new GuessingGameJoinReq("user2")));
        outUser2.flush();
        String serverJoinResp2 = receiveLineWithTimeout(inUser2);
        GuessingGameJoinResp successJoinResp2 = Utils.messageToObject(serverJoinResp2);
        assertEquals(new GuessingGameJoinResp(OK, CODE_0000),successJoinResp2);

        Thread.sleep(10000);

        // User3 joins after ten seconds
        outUser3.println(Utils.objectToMessage(new GuessingGameJoinReq("user3")));
        outUser3.flush();
        String serverJoinResp3 = receiveLineWithTimeout(inUser3);

        while (serverJoinResp3.equals("PING {}")) {
            serverJoinResp3 = receiveLineWithTimeout(inUser3);
        }

        GuessingGameJoinResp successJoinResp3 = Utils.messageToObject(serverJoinResp3);
        assertEquals(new GuessingGameJoinResp(ERROR, CODE_11003),successJoinResp3);

        // LoggingOff users so that the game countdown shutdown
        outUser1.println(Utils.objectToMessage(new Bye()));
        outUser1.flush();
        outUser2.println(Utils.objectToMessage(new Bye()));
        outUser2.flush();
        outUser3.println(Utils.objectToMessage(new Bye()));
        outUser3.flush();

        Thread.sleep(10000);
    }
    @Test
    void TC11_7_userJoinsGameAndAttemptsToSendGuessingNumberWithoutTenSecondBeingOverAndReturnsWaitForGameToBegin(TestReporter testReporter) throws JsonProcessingException, InterruptedException {
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

        outUser1.println(Utils.objectToMessage(new GuessingGameInviteReq("user1")));
        outUser1.flush();
        String serverResponse = receiveLineWithTimeout(inUser1);
        GuessingGameInviteResp inviteSuccessResp = Utils.messageToObject(serverResponse);
        assertEquals(new GuessingGameInviteResp(OK, CODE_0000), inviteSuccessResp);

        String gameInviteUser1 = receiveLineWithTimeout(inUser1);
        GuessingGameInvite guessingGameInvite1 = Utils.messageToObject(gameInviteUser1);
        assertEquals(new GuessingGameInvite("user1"),guessingGameInvite1);

        String gameInviteUser2 = receiveLineWithTimeout(inUser2);
        GuessingGameInvite guessingGameInvite2 = Utils.messageToObject(gameInviteUser2);
        assertEquals(new GuessingGameInvite("user1"),guessingGameInvite2);

        assertTimeoutPreemptively(Duration.ofMillis(11000), () -> {
            Instant start = Instant.now();

            outUser1.println(Utils.objectToMessage(new GuessingGameJoinReq("user1")));
            outUser1.flush();

            String serverJoinResp = receiveLineWithTimeout(inUser1);
            GuessingGameJoinResp joinResp = Utils.messageToObject(serverJoinResp);
            assertEquals(new GuessingGameJoinResp(OK, CODE_0000),joinResp);

            outUser1.println(Utils.objectToMessage(new GuessReq("user1",25)));
            outUser1.flush();

            String serverGuessResp = receiveLineWithTimeout(inUser1);
            GuessResp guessResp = Utils.messageToObject(serverGuessResp);
            assertEquals(new GuessResp(ERROR, CODE_11005),guessResp);

            Instant finish = Instant.now();

            long timeElapsed = Duration.between(start, finish).toMillis();
            testReporter.publishEntry("timeElapsed", String.valueOf(timeElapsed));
            assertTrue(timeElapsed <= 11000);
        });

        Thread.sleep(11000);
    }
    @Test
    void TC12_8_userAttemptsToGuessNumberWithoutReceivingGameInvitationAndReturnsInvitationIsRequired() throws InterruptedException, JsonProcessingException {
        receiveLineWithTimeout(inUser1);
        // Connect user1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1);
        receiveLineWithTimeout(inUser1);

        outUser1.println(Utils.objectToMessage(new GuessReq("user1",25)));
        outUser1.flush();
        String serverResp = receiveLineWithTimeout(inUser1);
        GuessResp guessResp = Utils.messageToObject(serverResp);
        assertEquals(new GuessResp(ERROR, CODE_11004),guessResp);
        Thread.sleep(11000);
    }
    @Test
    void TC12_9_userAttemptsToGuessNumberAfterReceivingGameInviteAndReturnsJoinFirstThenPlay() throws InterruptedException, JsonProcessingException {
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

        outUser1.println(Utils.objectToMessage(new GuessingGameInviteReq("user1")));
        outUser1.flush();
        String serverResponse = receiveLineWithTimeout(inUser1);
        GuessingGameInviteResp inviteSuccessResp = Utils.messageToObject(serverResponse);
        assertEquals(new GuessingGameInviteResp(OK, CODE_0000), inviteSuccessResp);

        String gameInviteUser1 = receiveLineWithTimeout(inUser1);
        GuessingGameInvite guessingGameInvite1 = Utils.messageToObject(gameInviteUser1);
        assertEquals(new GuessingGameInvite("user1"),guessingGameInvite1);

        String gameInviteUser2 = receiveLineWithTimeout(inUser2);
        GuessingGameInvite guessingGameInvite2 = Utils.messageToObject(gameInviteUser2);
        assertEquals(new GuessingGameInvite("user1"),guessingGameInvite2);

        outUser1.println(Utils.objectToMessage(new GuessReq("user1",25)));
        outUser1.flush();

        String serverResp = receiveLineWithTimeout(inUser1);
        GuessResp guessResp = Utils.messageToObject(serverResp);
        assertEquals(new GuessResp(ERROR, CODE_11006),guessResp);
        Thread.sleep(11000);
    }

    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), reader::readLine);
    }
}
