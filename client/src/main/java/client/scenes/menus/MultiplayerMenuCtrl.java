package client.scenes.menus;

import client.game.Game;
import client.scenes.MainCtrl;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import javax.inject.Inject;


public class MultiplayerMenuCtrl {

    private final MainCtrl mainCtrl;
    private final Game game;

    @FXML
    private TextField lobbyCode;

    @FXML
    private Text invalidLobbyPromt;

    @FXML
    private ImageView backButtonImageView;

    @Inject
    public MultiplayerMenuCtrl(MainCtrl mainCtrl, Game game) {
        this.mainCtrl = mainCtrl;
        this.game = game;
    }

    public void load()
    {
        backButtonImageView.setImage(new Image("/images/back-button.png"));
    }

    public void back(){
        mainCtrl.showGameModeSelection();
    }

    public void joinPublicLobby(){
        game.instantiateCommonLobby();
        game.joinPublicLobby();
    }


    public void createPrivateLobby() {
        game.instantiatePrivateLobby();
    }

    public void joinPrivateLobby() {
        String token = getLobbyCode();
        if(!token.equals("")) {
            boolean availableGame = game.joinPrivateLobby(token);
            if(!availableGame)
            {
                //the token was not found, the scene didn't change
                textPopUpRoutine("Invalid lobby code", 2000);
            }
        }
    }

    private String getLobbyCode()
    {
        String lobbyCodeText = lobbyCode.getText();
        if(lobbyCodeText.equals(""))
        {
            textPopUpRoutine("No lobby code provided", 2000);

            return lobbyCodeText;
        }
        else return lobbyCodeText;
    }

    /**
     * method that pops up an invalid text promt, from a given String
     * for the specified number of milisconds
     * @param miliSeconds
     */
    private void textPopUpRoutine(String text, Integer miliSeconds)
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    invalidLobbyPromt.setText(text);
                    Thread.sleep(miliSeconds);
                    invalidLobbyPromt.setText("");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
