package server.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import commons.Player;
import commons.WebsocketMessage;
import constants.ConnectionStatusCodes;
import constants.ResponseCodes;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import server.database.LobbyRepository;
import commons.Lobby;
import server.gameLogic.QuestionProvider;

import static constants.ConnectionStatusCodes.*;

@RestController
@RequestMapping("/api/lobby")
public class LobbyController {

    @Autowired
    private LobbyRepository repository;

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private QuestionProvider questionProvider;

    public LobbyController(LobbyRepository repository){
        this.repository = repository;
    }

    public LobbyController(){

    }

    /**
     * Given a lobby object, save that to the lobby repository
     * @param newLobby
     * @return the lobby that was saved
     */
    @PostMapping("/addLobby")
    @ResponseBody
    public Lobby addLobby(@RequestBody Lobby newLobby){
        String token = newLobby.getToken();
        if(repository.findByToken(token).isEmpty()) {
            repository.save(newLobby);
            System.out.println("Lobby created: " + newLobby.getToken());
        }
        return newLobby;
    }

    /**
     * Find all the lobbies in the repository
     * @return the full list of the lobbies that are in the repository
     */
    @GetMapping("/getAllLobbies")
    @ResponseBody
    public List<Lobby> getAllLobbies(){
        List<Lobby> lis = repository.findAll();
        return lis;
    }

    /**
     * Given the id of the lobby find that lobby
     * @param lobbyId
     * @return If a lobby was found with the given id return that lobby
     */
    @GetMapping("/getLobby")
    @ResponseBody
    public Optional<Lobby> getLobby(@RequestParam Long lobbyId){
        Optional<Lobby> found = repository.findById(lobbyId);
        return found;
    }

    /**
     * Given the token of the lobby find that lobby
     * @param token
     * @return If a lobby was found with the given token return that lobby
     */
    @GetMapping("/getLobbyByToken")
    @ResponseBody
    public Optional<Lobby> getLobbyByToken(@RequestParam String token){
        Optional<Lobby> found = repository.findByToken(token);
        return found;
    }

    /**
     * Given the token of the lobby, start the game if the player that sends the request is the host of the lobby
     * @param token
     * @return if the player was able to start the game
     */
    @GetMapping("/startLobby")
    @ResponseBody
    public ConnectionStatusCodes startLobby(@RequestParam String token){
        Optional<Lobby> found = repository.findByToken(token);
        if(found.isPresent())
        {
            Lobby activeLobby = found.get();
            if(activeLobby.getIsStarted())
                return ConnectionStatusCodes.YOU_ARE_NOT_HOST;
            activeLobby.setIsStarted(true);
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
        Lobby lobby = repository.findByToken(message.getLobbyToken()).get();
        if(lobby.getToken().equals("COMMON")) {
            lobby.setToken(RandomStringUtils.randomAlphabetic(5));
            repository.save(lobby);
            repository.save(new Lobby(message.getLobbyToken()));
        }
            return new WebsocketMessage(ResponseCodes.START_GAME, message.getLobbyToken(), lobby.getToken());
    }

    /**
     * Websocket mapping that processes the game start message from the player
     * and redirects it to all clients subscribed to /topic/lobbyEnd
     * @param message received from the client containing the Response code and lobby token.
     */
    @MessageMapping("/lobbyEnd")
    @SendTo("/topic/updateLobby")
    public WebsocketMessage endGame(WebsocketMessage message){
        Optional<Lobby> found = getLobbyByToken(message.getLobbyToken());
        if(found.isPresent())
        {
            Lobby lobbyToTerminate = found.get();
            lobbyToTerminate.setIsStarted(false);
            questionProvider.clearAllQuestionsFromLobby(lobbyToTerminate.getToken());
            questionProvider.clearAllActivitiesFromLobby(lobbyToTerminate.getToken());
            repository.save(lobbyToTerminate);
        }
        return new WebsocketMessage(ResponseCodes.END_GAME, message.getLobbyToken());
    }

    /**
     * Websocket mapping that processes the update request message from the player
     * and redirects it to all clients subscribed to /topic/updateLobby
     * @param message received from the client containing the Response code, lobby token and player
     * @return
     */
    @MessageMapping("/leaveLobby")
    @SendTo("/topic/updateLobby")
    public WebsocketMessage leaveLobby(WebsocketMessage message){
        Optional<Lobby> found = getLobbyByToken(message.getLobbyToken());
        if(found.isPresent()){
            Player playerToRemove = message.getPlayer();
            Lobby currentLobby = found.get();
            currentLobby.removePlayerFromLobby(playerToRemove);

            //check if the removed player was the host
            //if yes, assign a new host
            //if the lobby is now empty, terminate it

            if(message.getIsPlayerHost())
            {
                if(currentLobby.getPlayersInLobby().size() == 0)
                {
                    endGame(message);
                }
                else
                {
                    repository.save(currentLobby);
                    //first remaining player in the lobby is assigned as the new host
                    return new WebsocketMessage(ResponseCodes.UPDATE_HOST,
                            message.getLobbyToken(), currentLobby.getPlayersInLobby().get(0));
                }
            }

            repository.save(currentLobby);
        }

        return new WebsocketMessage(ResponseCodes.LEAVE_LOBBY, message.getLobbyToken());
    }

    @MessageMapping("/kickFromLobby")
    @SendTo("/topic/updateLobby")
    public WebsocketMessage kickFromLobby(WebsocketMessage message){
        Optional<Lobby> found = getLobbyByToken(message.getLobbyToken());
        if(found.isPresent()){
            Player playerToRemove = message.getPlayer();
            Lobby currentLobby = found.get();
            currentLobby.removePlayerFromLobby(playerToRemove);

            //removed the player from the repo as well
            repository.save(currentLobby);

            System.out.println("Player " + playerToRemove.getName()
                    + " has been kicked from lobby: " + message.getLobbyToken());
        }

        return new WebsocketMessage(ResponseCodes.KICK_PLAYER, message.getLobbyToken(), message.getPlayer());
    }

    /**
     * Websocket mapping that updates for each client the
     * appropriate no of questions, imposed by the admin (for private lobbies)
     * and redirects it to all clients subscribed to /topic/updateLobby
     * @param message received from the client containing the Response code and lobby token.
     * @return
     */
    @MessageMapping("/setNoOfQuestions")
    @SendTo("/topic/updateLobby")
    public WebsocketMessage setNoOfQuestions(WebsocketMessage message){
        return new WebsocketMessage(ResponseCodes.UPDATE_QUESTION_NUMBER,
                                    message.getLobbyToken(),message.getDifficultySetting());
    }


    /**
     * Websocket mapping that updates a player's score on the repo
     * and redirects it to all clients subscribed to /topic/updateLobby
     * @param message received from the client containing the Response code and lobby token.
     * @return
     */
    @MessageMapping("/updateScore")
    @SendTo("/topic/updateLobby")
    public WebsocketMessage updateScore(WebsocketMessage message){
        Optional<Lobby> found = getLobbyByToken(message.getLobbyToken());
        if(found.isPresent()){
            for(Player p : found.get().getPlayersInLobby()){
                if(p.getName().equals(message.getPlayer().getName()))
                    p.setScore(message.getPlayer().getScore());
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

    /**
     * Deletes all the lobby objects in the repository
     * @return a string that implies all the lobbies were deleted
     */
    @GetMapping("/clear")
    protected String clear(){
        repository.deleteAll();
        return "Cleared";
    }

    /**
     * Given the id of the lobby object, delete that lobby from the repository
     * @param id
     * @return a string that whether the deletion was successful or not
     */
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
