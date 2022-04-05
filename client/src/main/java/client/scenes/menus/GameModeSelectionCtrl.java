package client.scenes.menus;

import client.game.Game;
import client.scenes.MainCtrl;
import client.utils.ClientUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import javax.inject.Inject;

public class GameModeSelectionCtrl {

    private final MainCtrl mainCtrl;
    private final Game game;
    private final ClientUtils client;

    @FXML
    private Text labelToCountdown;

    @FXML
    private ImageView back;

    @FXML
    private ImageView singleController;

    @FXML
    private ImageView multiController1;

    @FXML
    private ImageView multiController2;

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
        back.setImage(new Image("images/back.png"));
        singleController.setImage(new Image("images/singleController.png"));
        multiController1.setImage(new Image("images/multiController1.png"));
        multiController2.setImage(new Image("images/multiController2.png"));
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
        mainCtrl.showAdminActivities();
    }
}
