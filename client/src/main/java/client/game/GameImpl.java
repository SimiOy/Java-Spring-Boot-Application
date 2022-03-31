package client.game;

import client.data.ClientData;
import client.emotes.Emotes;
import client.scenes.MainCtrl;
import client.utils.ClientUtils;
import client.utils.ServerUtils;
import commons.Lobby;
import commons.Player;
import commons.WebsocketMessage;
import constants.ConnectionStatusCodes;
import constants.ResponseCodes;
import javafx.application.Platform;
import org.apache.commons.lang3.RandomStringUtils;

import javax.inject.Inject;
import java.util.List;

public class GameImpl implements Game{

    private final ServerUtils server;
    private final ClientUtils client;
    private final ClientData clientData;
    private final MainCtrl mainCtrl;
    private  final Emotes emotes;

    private final String COMMON_CODE = "COMMON";
    private Integer questionsToEndGame = 20;
    private Integer questionsToDisplayLeaderboard = 10;

    @Inject
    public GameImpl(ServerUtils server, ClientUtils client, ClientData clientData, MainCtrl mainCtrl, Emotes emotes) {
        this.server = server;
        this.client = client;
        this.clientData = clientData;
        this.mainCtrl = mainCtrl;
        this.emotes = emotes;
    }

    /**
     * If the list of lobbies is empty, instantiate a new common lobby
     * If the list of lobbies is not empty
     */
    @Override
    public void instantiateCommonLobby()
    {
        List<Lobby> lobbies = server.getAllLobbies();

        if(lobbies.size() == 0)
        {
            Lobby mainLobby = new Lobby(COMMON_CODE);
            server.addLobby(mainLobby);
        }
        else
        {
            boolean commonLobbyExists = false;
            for(Lobby l : lobbies)
            {
                if(l.getToken().equals(COMMON_CODE))
                    commonLobbyExists = true;
            }

            if(!commonLobbyExists)
            {
                Lobby mainLobby = new Lobby(COMMON_CODE);
                server.addLobby(mainLobby);
            }
        }
    }

    public void instantiatePrivateLobby()
    {
        //instantiate a new lobby with a random token,using as host id the host player's id
        Lobby newLobby = new Lobby(RandomStringUtils.randomAlphabetic(5), (int) clientData.getClientPlayer().getId());
        server.addLobby(newLobby);
        clientData.setLobby(newLobby);
        clientData.setPointer(clientData.getClientPlayer().getId());
        clientData.setClientScore(0);
        clientData.setQuestionCounter(0);
        clientData.setAsHost(true);

        joinPrivateLobby(newLobby.getToken());

    }

    public boolean joinPrivateLobby(String token)
    {
        return joinLobby(token);
    }

    /**
     * Method that starts a single-player game
     */
    public void startSinglePlayer(){

        String lobbyCode = "SINGLE_PLAYER" +
                            clientData.getClientPlayer().getAvatarCode() +
                            RandomStringUtils.randomAlphabetic(5);

        Lobby mainLobby = new Lobby(lobbyCode);
        server.addLobby(mainLobby);
        clientData.setLobby(mainLobby);
        clientData.setPointer(clientData.getClientPlayer().getId());
        clientData.setClientScore(0);
        clientData.setQuestionCounter(0);
        //default value
        setQuestionsToEndGame(20);
        clientData.setAsHost(true);
        server.addMeToLobby(clientData.getClientLobby().getToken(),clientData.getClientPlayer());

        //add delay until game starts
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    //TODO: add timer progress bar / UI text with counter depleting until the start of the game

                    server.send("/app/nextQuestion",
                            new WebsocketMessage(ResponseCodes.NEXT_QUESTION,
                                    clientData.getClientLobby().getToken(), clientData.getClientPointer()));

                    Thread.sleep(3000);

                    Platform.runLater(() -> client.getQuestion());

                }catch (InterruptedException e){
                    e.printStackTrace();
                    System.out.println("Something went wrong while waiting to start the game");
                }
            }
        });
        thread.start();
    }

    /**
     * Method that joins the client to a public lobby (token = "COMMON")
     */
    public void joinPublicLobby()
    {
        joinLobby(COMMON_CODE);
    }

    private boolean joinLobby(String token)
    {
        Player clientPlayer = clientData.getClientPlayer();

        ConnectionStatusCodes permissionCode = server.getConnectPermission(token, clientPlayer.getName());

        switch(permissionCode){
            case USERNAME_ALREADY_USED:
                mainCtrl.showPopUp("public");
                break;
            case LOBBY_NOT_FOUND:
                //lobby not found
                return false;
            case CONNECTION_PERMISSION_GRANTED:
                //set client lobby static variable
                clientData.setLobby(server.addMeToLobby(token, clientPlayer));

                if(clientData.getClientLobby().playersInLobby.contains(clientPlayer))
                    mainCtrl.showWaiting();
        }
        return true;
    }

    /**
     * Initiates a multiplayer game
     */
    public void initiateMultiplayerGame()
    {
        System.out.println("game initiated");
        clientData.setClientScore(0);
        clientData.setQuestionCounter(0);
        //add delay until game starts
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(300);

                    Platform.runLater(() -> client.getQuestion());

                }catch (InterruptedException e){
                    e.printStackTrace();
                    System.out.println("Something went wrong while waiting to start the game");
                }
            }
        });
        thread.start();
    }

    /**
     * Only one player presses start game, that player becomes the host
     * The host precalculates the question in the server with one api call
     * The rest of the players use that question which the host asked to be precalculated on the server
     */
    public void startMultiplayerGame(){

        String token = clientData.getClientLobby().getToken();
        //start the lobby
        ConnectionStatusCodes code = server.startLobby(token);
        if(code.equals(ConnectionStatusCodes.YOU_ARE_HOST)) {
            clientData.setAsHost(true);
            clientData.setPointer(clientData.getClientLobby().getPlayerIds().get(0));
            clientData.setClientScore(0);
            clientData.setQuestionCounter(0);

            //start the game for the other players as well
            server.send("/app/lobbyStart",
                    new WebsocketMessage(ResponseCodes.START_GAME, clientData.getClientLobby().getToken()));
        }
    }

    public void leaveLobby() {
        //kill ongoing timers
        client.killTimer();

        server.send("/app/leaveLobby", new WebsocketMessage(ResponseCodes.LEAVE_LOBBY,
                clientData.getClientLobby().getToken(), clientData.getClientPlayer(), clientData.getIsHost()));

        //no more server polling for this client
        client.unsubscribeFromMessages();

        client.resetMessages();

        clientData.clearUnansweredQuestionCounter();

        System.out.println("Left the lobby");

        clientData.setAsHost(false);
        //set client lobby to exited
        clientData.setLobby(null);

        mainCtrl.showGameModeSelection();
    }

    public void endGame()
    {
        System.out.println("Game ended");
        server.send("/app/lobbyEnd", new WebsocketMessage(ResponseCodes.END_GAME,
                clientData.getClientLobby().getToken()));
        client.unsubscribeFromMessages();
        client.killTimer();
        client.resetMessages();
        //uses the current lobby to load images, scores and names for the players
        mainCtrl.showGameOver();
    }

    public Integer getQuestionsToEndGame(){
        return questionsToEndGame;
    }

    public void setQuestionsToEndGame(Integer value)
    {
        questionsToEndGame = value;
    }

    public Integer getQuestionsToDisplayLeaderboard()
    {
        return questionsToDisplayLeaderboard;
    }
}
