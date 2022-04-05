package server.database;

import commons.Lobby;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@DataJpaTest
class LobbyRepositoryTest {

    @Autowired
    private LobbyRepository lobbyRepository;

    @BeforeEach
    void initUseCase() {
        List<Lobby> lobbies = Arrays.asList(
                new Lobby("PUBLIC", false), new Lobby("PRIVATE", 0)
        );
        lobbyRepository.saveAll(lobbies);
    }

    @AfterEach
    public void destroyAll(){
        lobbyRepository.deleteAll();
    }

    @Test
    void saveAll_success() {
        List<Lobby> lobbies = Arrays.asList(
                new Lobby("PRIVATE", 2),
                new Lobby("PUBLIC", 3),
                new Lobby("PRIVATE", 4)
        );
        Iterable<Lobby> allCustomer = lobbyRepository.saveAll(lobbies);

        AtomicInteger validIdFound = new AtomicInteger();
        allCustomer.forEach(lobby -> {
            if(lobby.getHostId()>0){
                validIdFound.getAndIncrement();
            }
        });

        assertThat(validIdFound.intValue()).isEqualTo(3);
    }

    @Test
    void findAll_success() {
        List<Lobby> allLobby = lobbyRepository.findAll();
        assertThat(allLobby.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void findNoting(){
        lobbyRepository.deleteAll();
        assertThat(lobbyRepository.findAll().size() == 0);
    }

    @Test
    void findById(){
        long id = lobbyRepository.findAll().get(0).id;
        Lobby lobby = lobbyRepository.findById(id).get();
        assertTrue(lobby != null);
        assertTrue(lobby.getToken().equals("PUBLIC"));
        assertTrue(lobby.getPlayersInLobby().size() == 0);
        id = lobbyRepository.findAll().get(1).id;
        lobby = lobbyRepository.findById(id).get();
        assertTrue(lobby != null);
        assertTrue(lobby.getToken().equals("PRIVATE"));
        assertTrue(lobby.getPlayersInLobby().size() == 0);
    }

    @Test
    void findByToken() {
        Lobby lobby = lobbyRepository.findByToken("PUBLIC").get();
        assertTrue(lobby.getToken().equals("PUBLIC"));
        lobby = lobbyRepository.findByToken("PRIVATE").get();
        assertTrue(lobby.getToken().equals("PRIVATE"));
        lobbyRepository.save(new Lobby("TestToken", 214567));
        lobby = lobbyRepository.findByToken("TestToken").get();
        assertTrue(lobby.getHostId() == 214567);
    }

    @Test
    void deletePrivate(){
        lobbyRepository.delete(lobbyRepository.findByToken("PRIVATE").get());
        assertThat(lobbyRepository.findAll().size() == 1);
    }

}