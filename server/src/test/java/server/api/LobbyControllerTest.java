package server.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import commons.Lobby;
import commons.Player;
import commons.WebsocketMessage;
import constants.ConnectionStatusCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import server.api.Mocks.TestLobbyRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static constants.ConnectionStatusCodes.*;
import static constants.ResponseCodes.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
class LobbyControllerTest {

    private LobbyController lobbyController = new LobbyController(new TestLobbyRepository());

    private Lobby publicLobby;
    private Lobby privateLobby;

    private TestLobbyRepository repo;

    private LobbyController sut;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup(){
        publicLobby = new Lobby("public", false);
        privateLobby = new Lobby("private", 1);
        lobbyController.clear();
        repo = new TestLobbyRepository();
        sut = new LobbyController(repo);
    }

    @Test
    void addLobby(){
        assertTrue(lobbyController.getAllLobbies().size() == 0);
        Lobby newLobby = new Lobby("private", 2);
        lobbyController.addLobby(newLobby);
        assertTrue(lobbyController.getAllLobbies().size() == 1);
        lobbyController.addLobby(publicLobby);
        assertTrue(lobbyController.getAllLobbies().size() == 2);
    }

    @Test
    void getAllLobbies(){
        Lobby newLobby = new Lobby("private2", 2);
        lobbyController.addLobby(newLobby);
        lobbyController.addLobby(publicLobby);
        lobbyController.addLobby(privateLobby);
        assertTrue(lobbyController.getAllLobbies().contains(newLobby));
        assertTrue(lobbyController.getAllLobbies().contains(privateLobby));
        assertTrue(lobbyController.getAllLobbies().contains(publicLobby));
    }

    @Test
    void getLobbyByToken() {
        Lobby newLobby = new Lobby("private", 2);
        lobbyController.addLobby(newLobby);
        assertTrue(lobbyController.getLobbyByToken("private").get().equals(newLobby));
    }

    @Test
    void getConnectPermission() {
        assertTrue(lobbyController.getConnectPermission("private", "alex")
                .equals(ConnectionStatusCodes.LOBBY_NOT_FOUND));
        Lobby newLobby = new Lobby("private", 2);
        lobbyController.addLobby(newLobby);
        assertTrue(lobbyController.getConnectPermission("private", "alex")
                .equals(ConnectionStatusCodes.CONNECTION_PERMISSION_GRANTED));
        newLobby.addPlayerToLobby(new Player("alex"));
        assertTrue(lobbyController.getConnectPermission("private", "alex")
                .equals(ConnectionStatusCodes.USERNAME_ALREADY_USED));
        assertTrue(lobbyController.getConnectPermission("private", "testPlayer")
                .equals(ConnectionStatusCodes.CONNECTION_PERMISSION_GRANTED));
    }

    @Test
    void clear() {
        lobbyController.clear();
        assertTrue(lobbyController.getAllLobbies().isEmpty());
        Lobby newLobby = new Lobby("private", 2);
        lobbyController.addLobby(newLobby);
        lobbyController.clear();
        assertTrue(lobbyController.getAllLobbies().isEmpty());
    }

    @Test
    void addLobbyResponse() throws Exception{
        Lobby newLobby = new Lobby("private", 10);
        mockMvc.perform(post("/api/lobby/addLobby")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newLobby))
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("token").value("private"))
                .andExpect(jsonPath("hostId").value(10));
    }

    @Test
    void getAllLobbiesResponse() throws Exception{
        mockMvc.perform(get("/api/lobby/getAllLobbies"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));
    }

    @Test
    void getLobbyByTokenResponse() throws Exception{
        mockMvc.perform(get("/api/lobby/getLobbyByToken")
                .param("token", "private"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));
    }


    @Test
    public void startLobby(){
        Lobby newLobby = new Lobby("private", 2);
        repo.save(newLobby);
        assertEquals(YOU_ARE_HOST, sut.startLobby("private"));
    }

    @Test
    void startLobbyResponse() throws Exception{
        mockMvc.perform(get("/api/lobby/startLobby")
                .param("token", "private"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));
    }

    @Test
    public void addMeToLobby(){
        Lobby newLobby = new Lobby("private", 2);
        repo.save(newLobby);
        Player player1 = new Player();
        assertEquals(Optional.of(player1), sut.addMeToLobby("private", player1));
    }

    /*
    @Test
    public void startGame(){
        Lobby newLobby = new Lobby("private", 2);
        repo.save(newLobby);
        WebsocketMessage wsm = new WebsocketMessage(START_GAME, newLobby.getToken(), newLobby.getToken());
        assertNotEquals(sut.startGame(wsm).getNewToken(), wsm.getNewToken());
    }

     */

    /*
    @Test
    public void endGame(){
        Lobby newLobby = new Lobby("private", 2);
        newLobby.setIsStarted(true);
        repo.save(newLobby);
        WebsocketMessage wsm = new WebsocketMessage(END_GAME, newLobby.getToken());
        assertNotEquals(sut.getLobbyByToken(sut.endGame(wsm).getLobbyToken()), newLobby);
    }*/

    @Test
    public void leaveLobby(){
        Lobby newLobby = new Lobby("private", 2);
        Player player = new Player();
        newLobby.addPlayerToLobby(player);
        WebsocketMessage wsm = new WebsocketMessage(UPDATE_HOST, newLobby.getToken(), player, false);
        repo.save(newLobby);
        assertNotEquals(sut.getLobbyByToken(sut.leaveLobby(wsm).getLobbyToken()), newLobby);
    }

    @Test
    public void updateScore(){
        Lobby newLobby = new Lobby("private", 2);
        Player player = new Player("arda");
        newLobby.addPlayerToLobby(player);
        repo.save(newLobby);
        player.setScore(150);
        WebsocketMessage wsm = new WebsocketMessage(SCORE_UPDATED, newLobby.getToken(), player);
        assertEquals(sut.getLobbyByToken(sut.updateScore(wsm).getLobbyToken())
                .get().getPlayersInLobby().get(0).getScore(), 150);
    }


    @Test
    public void updateLobby(){
        Lobby newLobby = new Lobby("private", 2);
        repo.save(newLobby);
        WebsocketMessage wsm = new WebsocketMessage(LOBBY_UPDATED, "public");
        assertNotEquals(newLobby, sut.getLobbyByToken(sut.updateLobby(wsm).getLobbyToken()));
    }

    @Test
    public void getTopByLobbyToken() {
        Lobby newLobby = new Lobby("private", 2);
        Player player = new Player("arda");
        player.setScore(1);
        Player player1 = new Player("alex");
        player1.setScore(15);
        Player player2 = new Player("simi");
        player2.setScore(150);
        newLobby.addPlayerToLobby(player);
        newLobby.addPlayerToLobby(player1);
        newLobby.addPlayerToLobby(player2);
        repo.save(newLobby);
        List<Player> playerList = new ArrayList<Player>();
        playerList.add(0, player2);
        playerList.add(1, player1);
        playerList.add(2, player);
        assertEquals(playerList, sut.getTopByLobbyToken(newLobby.getToken()));
    }

}