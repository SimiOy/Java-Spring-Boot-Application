package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static constants.JokerType.DOUBLE_POINTS;
import static constants.ResponseCodes.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WebsocketMessageTest {

    WebsocketMessage wsm1, wsm2, wsm3, wsm4, wsm5, wsm6, wsm7, wsm8, wsm9;
    private static Player player;
    private static Question question;

    @BeforeEach
    void setUp(){
        //these are test websocketmessages for the communication of messages
        wsm1 = new WebsocketMessage("testMessage", "testToken");
        wsm2 = new WebsocketMessage("testMessage2", "testToken2");
        wsm3 = new WebsocketMessage("testMessage", "testToken");
        wsm4 = new WebsocketMessage(DOUBLE_POINTS, "someToken", "someName");
        player = new Player("arda");
        wsm5 = new WebsocketMessage(START_GAME, "someToken", player, true, false);
        wsm6 = new WebsocketMessage(LOBBY_UPDATED, "someToken", "someNewToken");
        wsm7 = new WebsocketMessage(NEXT_QUESTION, "someToken", 1L);
        question = new Question();
        wsm8 = new WebsocketMessage(LEAVE_LOBBY, "someToken", question);
        wsm9 = new WebsocketMessage();
    }

    @Test
    void getMessageTest(){
        assertEquals(wsm1.getMessage(), "testMessage");
        assertNotEquals(wsm1.getMessage(), "fakeTestMessage");
    }
    @Test
    void getLobbyTokenTest(){
        assertEquals(wsm1.getLobbyToken(), "testToken");
        assertNotEquals(wsm1.getLobbyToken(), "fakeTestToken");
    }

    @Test
    void testEquals() {
        assertEquals(wsm1, wsm3);
        assertNotEquals(wsm1, wsm2);
        assertNotEquals(null, wsm1);
    }

    @Test
    public void getJokerType() {
        assertEquals(DOUBLE_POINTS, wsm4.getJokerType());
    }

    @Test
    public void getPlayer() {
        assertEquals(player, wsm5.getPlayer());
    }

    @Test
    public void setPlayer() {
        WebsocketMessage wsm = new WebsocketMessage(END_GAME, "someToken", null, true, false);
        wsm.setPlayer(player);
        assertEquals(player, wsm.getPlayer());

    }

    @Test
    public void getIsPlayerHost() {
        assertEquals(true, wsm5.getIsPlayerHost());
    }

    @Test
    public void getCode() {
         assertEquals(START_GAME, wsm5.getCode());
    }

    @Test
    public void getSenderName() {
        assertEquals("someName", wsm4.getSenderName());
    }

    @Test
    public void getPointer() {
        assertEquals(1L, wsm7.getPointer());
    }

    @Test
    public void getQuestion() {
        assertEquals(question, wsm8.getQuestion());
    }

    @Test
    public void getNewToken() {
        assertEquals("someNewToken", wsm6.getNewToken());
    }

    @Test
    void testConstructor(){
        assertNotNull(wsm1);
        assertNotNull(wsm8);
    }

}