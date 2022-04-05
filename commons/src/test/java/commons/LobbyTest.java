package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class LobbyTest {

    private static Lobby lobby;
    private static Lobby lobby1;
    private static Lobby lobby2;
    private static Player player;

    @BeforeEach
    private void createLobby(){
        lobby = new Lobby("arda", false);
        lobby1 = new Lobby("someToken", 1);
        lobby2 = new Lobby();
        player = new Player("arda");
    }

    /*
    Tests the main constructor
     */
    @Test
    void testConstructor(){
        assertNotNull(lobby);
        assertNotNull(lobby1);
    }

    /*
    Tests the empty constructor
     */
    @Test
    void testConstructorNull(){
        assertNotNull(lobby2);
    }

    @Test
    void getToken(){
        assertEquals("arda", lobby.getToken());
    }

    @Test
    void getHostId(){
        assertEquals(null, lobby.getHostId());
    }

    @Test
    void setToken(){
        Lobby lobby1 = lobby;
        lobby1.setToken("simi");
        assertEquals("simi", lobby1.getToken());
    }

    @Test
    void setAndGetHostId(){
        Lobby lobby1 = lobby;
        lobby1.setHostId(5);
        assertEquals(5, lobby1.getHostId());
    }

    @Test
    public void addPlayerToLobby()
    {
        assertEquals(0, lobby.getPlayersInLobby().size());
        lobby.addPlayerToLobby(player);
        assertEquals(1, lobby.getPlayersInLobby().size());
    }

    @Test
    public void removePlayerFromLobby()
    {
        assertEquals(0, lobby.getPlayersInLobby().size());
        lobby.addPlayerToLobby(player);
        assertEquals(1, lobby.getPlayersInLobby().size());
        lobby.removePlayerFromLobby(player);
        assertEquals(0, lobby.getPlayersInLobby().size());
    }

    @Test
    public void getPlayerIds() {
        ArrayList<Long> players = new ArrayList<Long>();
        players.add(player.getId());
        lobby.addPlayerToLobby(player);
        assertEquals(lobby.getPlayerIds(), players);
    }

    @Test
    public void setPlayerIds() {
        ArrayList<Long> players = new ArrayList<Long>();
        players.add(player.getId());
        lobby.setPlayerIds(players);
        assertEquals(lobby.getPlayerIds(), players);
    }

    @Test
    public void getIsStarted() {
        assertEquals(false, lobby.getIsStarted());
    }

    @Test
    public void setIsStarted() {
        lobby.setIsStarted(true);
        assertEquals(true, lobby.getIsStarted());
    }

    @Test
    public void equals() {
        assertNotEquals(lobby, lobby1);
        Lobby lobby3 = lobby;
        assertEquals(lobby3, lobby);
    }
}
