package server.api;

import java.util.List;
import java.util.Optional;

import commons.Player;
import constants.ConnectionStatusCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import server.database.LobbyRepository;
import commons.Lobby;

import static constants.ConnectionStatusCodes.*;

@RestController
@RequestMapping("/api/lobby")
public class LobbyController {
    
    @Autowired
    private LobbyRepository repository;

    @PostMapping("/addLobby")
    @ResponseBody
    public Lobby addLobby(@RequestBody Lobby newLobby){
        repository.save(newLobby);
        return newLobby;
    }

    @GetMapping("/getAllLobbies")
    @ResponseBody
    public List<Lobby> getAllLobbies(){
        List<Lobby> lis = repository.findAll();
        return lis;
    }

    @GetMapping("/getLobby")
    @ResponseBody
    public Optional<Lobby> getLobby(@RequestParam Long lobbyId){
        Optional<Lobby> found = repository.findById(lobbyId);
        return found;
    }

    @GetMapping("/getLobbyByToken")
    @ResponseBody
    public Optional<Lobby> getLobbyByToken(@RequestParam String token){
        Optional<Lobby> found = repository.findByToken(token);
        return found;
    }

    /**
     *
     * @param token the token of the lobby a player is trying to access
     * @param playerUsername the username of the player
     * @return 0 if the username is already used, 1 if the lobby cannot be found,
     *          2 if the player has permission to connect
     */
    @GetMapping("/getConnectPermission")
    @ResponseBody
    public ConnectionStatusCodes getConnectPermission(@RequestParam String token, @RequestParam String playerUsername){
        Optional<Lobby> found = repository.findByToken(token);
        if(found.isPresent()){
            if(!found.get().playersInLobby.isEmpty()){
                for(Player p : found.get().playersInLobby){
                    if(p.name.equals(playerUsername))
                        return ConnectionStatusCodes.USERNAME_ALREADY_USED;
                }
            }

            return CONNECTION_PERMISSION_GRANTED;
        } else {
            return LOBBY_NOT_FOUND;
        }
    }

    @GetMapping("/clear")
    protected String clear(){
        repository.deleteAll();
        return "Cleared";
    }

    @GetMapping("/delete")
    public String deleteLobby(@RequestParam long id) {
        Optional<Lobby> lobby = repository.findById(id);
        if (lobby.isEmpty()) {
            return "Lobby not found";
        }

        repository.deleteById(id);
        return "Success";
    }
}
