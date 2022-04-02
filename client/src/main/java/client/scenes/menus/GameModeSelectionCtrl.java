package client.scenes.menus;

import client.game.Game;
import client.scenes.MainCtrl;
import client.utils.ClientUtils;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

import javax.inject.Inject;

public class GameModeSelectionCtrl {

    private final MainCtrl mainCtrl;
    private final Game game;
    private final ClientUtils client;

    @FXML
    private Text labelToCountdown;

    @Inject
    public GameModeSelectionCtrl(MainCtrl mainCtrl, Game game, ClientUtils client) {
        this.mainCtrl = mainCtrl;
        this.game = game;
        this.client = client;
    }

    public void load()
    {
        labelToCountdown.setText("");
        client.registerQuestionCommunication();
        client.registerLobbyCommunication();
        client.registerMessageCommunication();
    }

    public void back(){
        mainCtrl.showHome();
    }

    public void singleplayer(){
        client.assignCountdownLabel(labelToCountdown);
        game.startSinglePlayer();
    }

    public void multiplayer(){
        mainCtrl.showMultiplayerMenu();
    }

    public void leaderboard(){
        mainCtrl.showLeaderboard();
    }

    public void admin(){
        mainCtrl.showAdminHome();
    }
}
