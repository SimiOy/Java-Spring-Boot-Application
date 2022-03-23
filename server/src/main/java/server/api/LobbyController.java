package server.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import commons.Player;
import commons.WebsocketMessage;
import constants.ConnectionStatusCodes;
import constants.ResponseCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import server.database.LobbyRepository;
import commons.Lobby;

import static constants.ConnectionStatusCodes.*;

@RestController
@RequestMapping("/api/lobby")
public class LobbyController {

    @Autowired
    private LobbyRepository repository;

    @Autowired
    private SimpMessagingTemplate template;

    public LobbyController(LobbyRepository repository){
        this.repository = repository;
    }

    public LobbyController(){

    }

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

    @GetMapping("/startLobby")
    @ResponseBody
    public ConnectionStatusCodes startLobby(@RequestParam String token){
        Optional<Lobby> found = repository.findByToken(token);
        if(found.isPresent())
        {
            Lobby activeLobby = found.get();
            if(activeLobby.getStarted())
                return ConnectionStatusCodes.YOU_ARE_NOT_HOST;
            activeLobby.setStarted(true);
            repository.save(activeLobby);

            System.out.println("game started");
            return YOU_ARE_HOST;
        } else {
            return LOBBY_NOT_FOUND;
        }
    }

    /**
     * An api endpoint that adds the player to a lobby in the database.
     * In client the endpoint is used in combination with getLobbyByToken to update the local lobby
     * @param token of the lobby that the player is added to
     * @param player to be added to lobby
     * @return player that was added to lobby
     */
    @PostMapping("/addMeToLobby")
    @ResponseBody
    public Optional<Player> addMeToLobby(@RequestParam String token, @RequestBody Player player){
        Optional<Lobby> found = repository.findByToken(token);
        if(found.isPresent()){
            found.get().addPlayerToLobby(player);
            repository.save(found.get());
        }

        return Optional.of(player);
    }

    /**
     * Websocket mapping that processes the game start message from the player
     * and redirects it to all clients subscribed to /topic/lobbyStart
     * @param message received from the client containing the Response code and lobby token.
     */
    @MessageMapping("/lobbyStart")
    @SendTo("/topic/lobbyStart")
    public WebsocketMessage startGame(WebsocketMessage message){
            return new WebsocketMessage(ResponseCodes.START_GAME, message.getLobbyToken());
    }

    @MessageMapping("/leaveLobby")
    @SendTo("/topic/updateLobby")
    public WebsocketMessage leaveLobby(WebsocketMessage message){
        Optional<Lobby> found = getLobbyByToken(message.getLobbyToken());
        if(found.isPresent()){
            found.get().removePlayerByName(message.getPlayer().getName());
            repository.save(found.get());
        }

        return new WebsocketMessage(ResponseCodes.LEAVE_LOBBY, message.getLobbyToken());
    }

    @MessageMapping("/updateScore")
    @SendTo("/topic/updateLobby")
    public WebsocketMessage updateScore(WebsocketMessage message){
        Optional<Lobby> found = getLobbyByToken(message.getLobbyToken());
        if(found.isPresent()){
            for(Player p : found.get().playersInLobby){
                if(p.name.equals(message.getPlayer().name))
                    p.score = message.getPlayer().score;
            }

            repository.save(found.get());
        }

        return new WebsocketMessage(ResponseCodes.SCORE_UPDATED, message.getLobbyToken());
    }

    /**
     * Websocket mapping that processes the update request message from the player
     * and redirects it to all clients subscribed to /topic/updateLobby
     * @param message received from the client containing the Response code and lobby token.
     * @return
     */
    @MessageMapping("/requestUpdate")
    @SendTo("/topic/updateLobby")
    public WebsocketMessage updateLobby(WebsocketMessage message){
        return new WebsocketMessage(ResponseCodes.LOBBY_UPDATED, message.getLobbyToken());
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

    /**
     * @param token lobby token for which you want to find the top 10 scores
     * @return top 10 players (or less if there are less players) from lobby with given token
     * Gets all players from lobby with given token, sorts them by their score and returns the sorted list,
     * which can then be displayed in the table
     */

    @GetMapping("/getTopByLobbyToken")
    public List<Player> getTopByLobbyToken(@RequestParam String token) {
        Optional<Lobby> lobby = repository.findByToken(token);
        if(lobby.isEmpty()) {
            return null;
        }
        List<Player> players = lobby.get().getPlayersInLobby();
        List<Player> playersSorted = new ArrayList<>();
        Player temp;
        while(players.isEmpty()==false){
            temp = players.get(0);
            for (int i = 1; i < players.size(); i++) {
                if (players.get(i).getScore() > temp.getScore()) {
                    temp = players.get(i);
                }
            }
            playersSorted.add(temp);
            players.remove(temp);
        }
        return playersSorted;
    }
}
