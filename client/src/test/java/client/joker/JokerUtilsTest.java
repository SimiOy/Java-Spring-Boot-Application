package client.joker;

import client.data.ClientData;
import client.utils.ClientUtils;
import client.utils.ServerUtils;
import commons.Lobby;
import commons.Player;
import commons.WebsocketMessage;
import constants.JokerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class JokerUtilsTest {

    private ClientData clientData;
    private ClientUtils client;
    private ServerUtils server;

    private JokerUtils jokerUtils;

    @BeforeEach
    void setUp(){
        clientData = mock(ClientData.class);
        client = mock(ClientUtils.class);
        server = mock(ServerUtils.class);
        defineMethodsForClientData();

        jokerUtils = new JokerUtils(client, server, clientData);
    }
    /**
     * This method is part of the 'given' section in the
     * given - then - verify testing pattern
     * for the ClientData class
     */
    private void defineMethodsForClientData() {
        when(clientData.getClientLobby()).thenReturn(new Lobby("LOBBY", false));
        when(clientData.getClientPlayer()).thenReturn(new Player("PLAYER"));
    }


    @Test
    void registerForJokerUpdates() {
        jokerUtils.registerForJokerUpdates();
        verify(server).registerForMessages(anyString(), any());
    }

    @Test
    void sendJoker() {
        jokerUtils.sendJoker();
        verify(server).send("/app/updateJoker", new WebsocketMessage(
                (JokerType) null, "LOBBY", "PLAYER"
        ));
    }

    @Test
    void halfTimeIsSender() {
        jokerUtils.halfTime("PLAYER");
        verify(client, times(0)).halfTime();
    }

    @Test
    void halfTimeIsNotSender() {
        jokerUtils.halfTime("OTHER_PLAYER");
        verify(client).halfTime();
    }

    @Test
    void setLobbyJoker() {
        jokerUtils.setLobbyJoker(JokerType.HALF_TIME_FOR_ALL_LOBBY);
        jokerUtils.sendJoker();
        verify(server).send("/app/updateJoker", new WebsocketMessage(
                JokerType.HALF_TIME_FOR_ALL_LOBBY, "LOBBY", "PLAYER"
        ));
    }
}