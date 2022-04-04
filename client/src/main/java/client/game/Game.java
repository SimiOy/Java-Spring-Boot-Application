package client.game;

public interface Game {
    void instantiateCommonLobby();

    void instantiatePrivateLobby();

    boolean joinPrivateLobby(String token);

    void startSinglePlayer();

    void joinPublicLobby();

    void leaveLobby();

    void initiateMultiplayerGame();

    void startMultiplayerGame();

    void endGame();

    Integer getQuestionsToEndGame();

    void setQuestionsToEndGame(Integer value);

    Integer getQuestionsToDisplayLeaderboard();

    void setQuestionsToDisplayLeaderboard(Integer value);
}
