package client.data;

import commons.Lobby;
import commons.Player;
import commons.Question;
import constants.GameType;
import constants.JokerType;

import java.util.HashSet;

public interface ClientData {

    Boolean getIsHost();

    void setAsHost(Boolean value);

    Integer getQuestionCounter();

    void incrementQuestionCounter();

    void setQuestionCounter(Integer counter);

    Integer getClientScore();

    void setClientScore(Integer score);

    void setPlayer(Player player);

    Player getClientPlayer();

    void setPointer(Long pointer);

    Long getClientPointer();

    void setQuestion(Question question);

    Question getClientQuestion();

    void setLobby(Lobby lobby);

    Lobby getClientLobby();

    HashSet<JokerType> getUsedJokers();

    void addJoker(JokerType joker);

    void resetJokers();

    Integer getUnansweredQuestionCounter();

    void setUnansweredQuestionCounter(Integer unansweredQuestionCounter);

    void incrementUnansweredQuestionCounter();

    void clearUnansweredQuestionCounter();

    GameType getGameType();

    void setGameType(GameType gameType);

    Lobby getLastLobby();
}
