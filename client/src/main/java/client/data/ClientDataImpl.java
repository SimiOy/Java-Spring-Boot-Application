package client.data;

import commons.Lobby;
import commons.Player;
import commons.Question;
import constants.GameType;
import constants.JokerType;

import java.util.HashSet;

public class ClientDataImpl implements ClientData {

    /**
     * This class holds client data about the player that should remain accesible from the entire client
     * It will also be more useful later on in case we have to do de/serialization, since this holds all player info
     * Might also come in handy for cookie parsing, as this info should retain
     */

    private Player clientPlayer;
    private Lobby clientLobby;
    private Lobby lastLobby;
    private Long clientPointer;
    private Question clientQuestion;
    private Integer clientScore;
    private Integer questionCounter;
    private GameType gameType;
    private Integer unansweredQuestionCounter = 0;
    private Boolean isHost = false; //remembers who the host of the lobby is
    private HashSet<JokerType> usedJokers = new HashSet<>();

    public HashSet<JokerType> getUsedJokers() {
        return usedJokers;
    }

    public void addJoker(JokerType joker){
        usedJokers.add(joker);
    }

    public Boolean getIsHost() {
        return isHost;
    }

    public void setAsHost(Boolean value) {
        isHost = value;
    }

    public Integer getQuestionCounter()
    {
        return questionCounter;
    }

    public void incrementQuestionCounter()
    {
        questionCounter++;
    }

    public void setQuestionCounter(Integer counter)
    {
        questionCounter = counter;
    }

    public Integer getClientScore() {
        return clientScore;
    }

    public void setClientScore(Integer score) {
        clientScore = score;
    }

    public void setPlayer(Player player)
    {
        clientPlayer = player;
    }

    public Player getClientPlayer(){return clientPlayer;}

    public void setPointer(Long pointer)
    {
        clientPointer = pointer;
    }

    public Long getClientPointer(){return clientPointer;}

    public void setQuestion(Question question)
    {
        clientQuestion = question;
    }

    public Question getClientQuestion(){return clientQuestion;}

    public void setLobby(Lobby lobby)
    {
        clientLobby = lobby;
        if(lobby != null){
            lastLobby = clientLobby;
        }
    }

    public Lobby getClientLobby(){return clientLobby;}

    public void resetJokers(){
        usedJokers = new HashSet<>();
    }

    public Integer getUnansweredQuestionCounter() {
        return unansweredQuestionCounter;
    }

    public void setUnansweredQuestionCounter(Integer unansweredQuestionCounter) {
        this.unansweredQuestionCounter = unansweredQuestionCounter;
    }

    public void incrementUnansweredQuestionCounter()
    {
        unansweredQuestionCounter++;
    }

    @Override
    public void clearUnansweredQuestionCounter() {
        unansweredQuestionCounter=0;
    }

    @Override
    public Lobby getLastLobby() {
        return lastLobby;
    }

    public GameType getGameType(){
        return gameType;
    }

    public void setGameType(GameType gameType){
        this.gameType = gameType;
    }
}
