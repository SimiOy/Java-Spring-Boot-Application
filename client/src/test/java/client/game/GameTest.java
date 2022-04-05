package client.game;

import client.data.ClientData;
import client.emotes.Emotes;
import client.joker.JokerUtils;
import client.scenes.MainCtrl;
import client.utils.ClientUtils;
import client.utils.ServerUtils;
import commons.Lobby;
import commons.Player;
import constants.ConnectionStatusCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GameTest {

    private ServerUtils server;
    private ClientUtils client;
    private JokerUtils jokerUtils;
    private ClientData clientData;
    private MainCtrl mainCtrl;
    private Game game;
    private Emotes emotes;

    @BeforeEach
    void setUp() {
        server = mock(ServerUtils.class);
        client = mock(ClientUtils.class);
        clientData = mock(ClientData.class);
        mainCtrl = mock(MainCtrl.class);
        emotes = mock(Emotes.class);
        defineMethodsForServer();
        defineMethodsForClientData();

        game = new GameImpl(server, client, clientData, mainCtrl, emotes, jokerUtils);
    }

    /**
     * This method is part of the 'given' section in the
     * given - then - verify testing pattern
     * for the ServerUtils class
     */
    void defineMethodsForServer(){
        List<Lobby> lobbies = new ArrayList<>();
        lobbies.add(new Lobby("lobby-1", false));
        lobbies.add(new Lobby("lobby-2", false));
        when(server.getAllLobbies()).thenReturn(lobbies);
    }

    /**
     * This method is part of the 'given' section in the
     * given - then - verify testing pattern
     * for the ClientData class
     */
    void defineMethodsForClientData(){
        Player player = new Player("playerName");
        Lobby lobby = new Lobby("SINGLE_PLAYER", true);
        lobby.addPlayerToLobby(player);
        when(clientData.getClientPlayer()).thenReturn(player);
        when(clientData.getClientLobby()).thenReturn(lobby);
    }

    @Test
    void instantiateCommonLobbyAllLobbiesRetrieved() {
        game.instantiateCommonLobby();
        verify(server).getAllLobbies();
        verify(server).addLobby(new Lobby("COMMON", false));
    }


    /*
    @Test
    void startSingleplayer() {
        game.startSinglePlayer();
        Lobby singlePlayerLobby = new Lobby("SINGLE_PLAYER");
        verify(server).addLobby(singlePlayerLobby);
        verify(clientData).setLobby(singlePlayerLobby);
        verify(clientData).setAsHost(true);
        verify(server).addMeToLobby(clientData.getClientLobby().getToken(), clientData.getClientPlayer());
    }
     */

    @Test
    void joinPublicLobby() {
        when(server.getConnectPermission("COMMON", "playerName")).
                thenReturn(ConnectionStatusCodes.USERNAME_ALREADY_USED);
        game.joinPublicLobby();
        verify(clientData).getClientPlayer();
        verify(server).getConnectPermission("COMMON", clientData.getClientPlayer().getName());
    }

    @Test
    void joinPublicLobbyUsernameAlreadyUsed() {
        when(server.getConnectPermission("COMMON", "playerName")).
                thenReturn(ConnectionStatusCodes.USERNAME_ALREADY_USED);
        game.joinPublicLobby();
        verify(mainCtrl).showPopUp("public");
    }

    @Test
    void joinPublicLobbyLobbyNotFound() {
        when(server.getConnectPermission("COMMON", "playerName")).
                thenReturn(ConnectionStatusCodes.LOBBY_NOT_FOUND);
        game.joinPublicLobby();
        verify(clientData, times(0)).setLobby(
                server.addMeToLobby("COMMON", new Player("playerName")));
        verify(mainCtrl, times(0)).showPopUp("public");
    }

    @Test
    void joinPublicLobbyPermissionGranted() {
        when(server.getConnectPermission("COMMON", "playerName")).
                thenReturn(ConnectionStatusCodes.CONNECTION_PERMISSION_GRANTED);
        game.joinPublicLobby();
        verify(clientData).setLobby(server.addMeToLobby("COMMON", clientData.getClientPlayer()));
    }

    @Test
    void leaveLobby() {
        game.leaveLobby();
        verify(client).killTimer();
        verify(client).unsubscribeFromMessages();
        verify(clientData).setAsHost(false);
        verify(mainCtrl).showGameModeSelection();
    }

    @Test
    void initiateMultiplayerGame() {
        game.initiateMultiplayerGame();
        verify(clientData).setClientScore(0);
        verify(clientData).setQuestionCounter(0);
    }

    @Test
    void startMultiplayerGame() {
        when(server.startLobby("SINGLE_PLAYER")).thenReturn(ConnectionStatusCodes.YOU_ARE_HOST);
        game.startMultiplayerGame();
        verify(clientData).setAsHost(true);
        verify(clientData).setQuestionCounter(0);
    }

    @Test
    void endGame() {
        game.endGame();
        verify(client).unsubscribeFromMessages();
        verify(client).killTimer();
        verify(mainCtrl).showGameOver();
    }

    @Test
    void getQuestionsToEndGame() {
        Integer questionsToEnd = game.getQuestionsToEndGame();
        assertEquals(questionsToEnd, 20);
    }

    @Test
    void getQuestionsToDisplayLeaderboard() {
        Integer questionsToLeaderboard = game.getQuestionsToDisplayLeaderboard();
        assertEquals(questionsToLeaderboard, 10);
    }
}