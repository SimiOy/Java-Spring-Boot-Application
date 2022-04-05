package client.game;

import client.data.ClientData;
import client.emotes.Emotes;
import client.joker.JokerUtils;
import client.scenes.MainCtrl;
import client.utils.ClientUtils;
import client.utils.ServerUtils;
import commons.LeaderboardEntry;
import commons.Lobby;
import commons.Player;
import commons.WebsocketMessage;
import constants.ConnectionStatusCodes;
import constants.GameType;
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
    private final Emotes emotes;
    private final JokerUtils jokerUtils;

    private Thread singleplayerThread;
    private Thread multiplayerThread;

    private final String COMMON_CODE = "COMMON";
    private Integer questionsToEndGame = 20;
    private Integer questionsToDisplayLeaderboard = 10;

    @Inject
    public GameImpl(ServerUtils server, ClientUtils client, ClientData clientData, MainCtrl mainCtrl, Emotes emotes,
                    JokerUtils jokerUtils) {
        this.server = server;
        this.client = client;
        this.clientData = clientData;
        this.mainCtrl = mainCtrl;
        this.emotes = emotes;
        this.jokerUtils = jokerUtils;
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
            Lobby mainLobby = new Lobby(COMMON_CODE, false);
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
                Lobby mainLobby = new Lobby(COMMON_CODE, false);
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

        Lobby mainLobby = new Lobby(lobbyCode, true);
        server.addLobby(mainLobby);
        clientData.setPointer(clientData.getClientPlayer().getId());
        clientData.setClientScore(0);
        clientData.setQuestionCounter(0);
        //default value
        setQuestionsToEndGame(20);
        clientData.setAsHost(true);
        clientData.setGameType(GameType.SINGLEPLAYER);
        client.swapEmoteJokerUsability(true);
        clientData.setLobby(mainLobby);
        server.addMeToLobby(clientData.getClientLobby().getToken(),clientData.getClientPlayer());
        jokerUtils.registerForJokerUpdates();

        //add delay until game starts
        singleplayerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    //TODO: add timer progress bar / UI text with counter depleting until the start of the game

                    server.send("/app/nextQuestion",
                            new WebsocketMessage(ResponseCodes.NEXT_QUESTION,
                                    mainLobby.getToken(), clientData.getClientPointer()));

                    client.startSyncCountdown();
                    Thread.sleep(3000);

                    Platform.runLater(() -> client.getQuestion());

                }catch (InterruptedException e){
                    e.printStackTrace();
                    System.out.println("Something went wrong while waiting to start the game");
                }
            }
        });
        singleplayerThread.start();
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
        clientData.setGameType(GameType.MULTIPLAYER);
        client.swapEmoteJokerUsability(false);
        //add delay until game starts
        multiplayerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    client.startSyncCountdown();
                    Thread.sleep(3000);

                    Platform.runLater(() -> client.getQuestion());

                }catch (InterruptedException e){
                    e.printStackTrace();
                    System.out.println("Something went wrong while waiting to start the game");
                }
            }
        });
        multiplayerThread.start();
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
        emotes.sendDisconnect();
        //kill ongoing timers
        client.killTimer();
        server.send("/app/leaveLobby", new WebsocketMessage(ResponseCodes.LEAVE_LOBBY,
                clientData.getClientLobby().getToken(), clientData.getClientPlayer(), clientData.getIsHost(), true));

        //no more server polling for this client
        client.unsubscribeFromMessages();

        client.resetMessages();

        clientData.clearUnansweredQuestionCounter();

        System.out.println("Left the lobby");

        clientData.setAsHost(false);
        //set client lobby to exited
        clientData.setLobby(null);
        clientData.setGameType(null);

        mainCtrl.showGameModeSelection();
    }

    public void endGame()
    {
        System.out.println("Game ended");
        //if we've come to the end of a singleplayergame the player's avatarCode, score and name are stored in the repo
        if(clientData.getGameType() == GameType.SINGLEPLAYER){
            Player temp = clientData.getClientPlayer();
            server.persistScore(new LeaderboardEntry(temp.getScore(), temp.getName(), temp.getAvatarCode()));
        }
        server.send("/app/lobbyEnd", new WebsocketMessage(ResponseCodes.END_GAME,
                clientData.getClientLobby().getToken()));
        client.unsubscribeFromMessages();
        client.killTimer();
        client.resetMessages();
        killGameThreads();
        //uses the current lobby to load images, scores and names for the players
        mainCtrl.showGameOver();
        //only done after loading the leaderboard, because it's still needed there to determine which one to display
        clientData.setGameType(null);
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

    public void setQuestionsToDisplayLeaderboard(Integer questionsToDisplayLeaderboard) {
        this.questionsToDisplayLeaderboard = questionsToDisplayLeaderboard;
    }

    /**
     * There's three cases:
     * 1) Lobby was singleplayer, in which case start again
     * 2) Lobby was a common lobby, in which case queue up the player in a common lobby
     * with the same players of the old one
     * 3) Lobby was a private lobby, in which case queue up the player in a private lobby
     * with the same players of the old one
     * TODO: figure out how to reset multiplayer lobbies
     * @param lobby
     */
    public void restartLobby(Lobby lobby) {
        client.registerQuestionCommunication();
        client.registerLobbyCommunication();
        client.registerMessageCommunication();
        if(lobby.getSingleplayer()){
            startSinglePlayer();
        }else{
            if(lobby.getPublic()){
                instantiateCommonLobby();
                joinPublicLobby();
            }else{
                joinPrivateLobby(lobby.getToken());
            }
        }
    }

    @Override
    public void killGameThreads() {
        if(singleplayerThread != null){
            singleplayerThread.interrupt();
        }
        if(multiplayerThread != null){
            multiplayerThread.interrupt();
        }
    }
}
