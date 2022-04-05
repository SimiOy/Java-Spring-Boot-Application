package client.scenes.menus;

import client.avatar.AvatarManager;
import client.data.ClientData;
import client.scenes.MainCtrl;
import client.utils.ClientUtils;
import client.utils.ServerUtils;
import commons.Player;
import exceptions.InvalidServerException;
import jakarta.ws.rs.WebApplicationException;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Modality;

import javax.inject.Inject;
import javafx.util.Duration;

public class HomeCtrl {

    private final ServerUtils server;
    private final ClientUtils clientUtils;
    private final MainCtrl mainCtrl;
    private final ClientData clientData;
    private final AvatarManager avatarGenerator;

    @FXML
    private TextField name;

    @FXML
    private TextField serverTextField;

    @FXML
    private TextField portTextField;

    @FXML
    private ImageView avatarImage;

    @FXML
    private Text incorrectServerText;

    @FXML
    private ImageView bgImage;

    @FXML
    private ImageView playButtonImage;

    @FXML
    private ImageView nextAvatar;

    @FXML
    private ImageView previousAvatar;

    @Inject
    public HomeCtrl(ServerUtils server, MainCtrl mainCtrl, ClientData clientData, AvatarManager avatarGenerator,
                    ClientUtils clientUtils) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.clientData = clientData;
        this.avatarGenerator = avatarGenerator;
        this.clientUtils = clientUtils;
    }

    /**
     * When home screen is shown, this method is called
     */
    public void load()
    {
        setRandomInitName();
        avatarGenerator.setNameAndAvatarImage(name, avatarImage);
        avatarGenerator.initAvatar();
        avatarGenerator.setAvatarImage();
        bgImage.setImage(new Image("images/home_screen.png"));
        bgImage.setPreserveRatio(false);
        playButtonImage.setImage(new Image("images/play.png"));
        nextAvatar.setImage(new Image("images/right-arrow.png"));
        previousAvatar.setImage(new Image("images/left-arrow.png"));
        FadeTransition ft = new FadeTransition();
        ft.setNode(bgImage);
        ft.setDuration(new Duration(1700));
        ft.setFromValue(1.0);
        ft.setToValue(0.4);
        ft.setCycleCount(6);
        ft.setOnFinished(a -> {
            ft.playFromStart();
        });
        ft.setAutoReverse(true);
        ft.play();
    }

    /**
     * Sets a random name to the prompt text of the player name-selection
     * Name should be generated from a random selection of names (ex MonkeyEye64, KingTower12...)
     */
    public void setRandomInitName()
    {
        if(clientData.getClientPlayer() == null) {
            name.setText("testPlayer");
        }
        else
        {
            name.setText(clientData.getClientPlayer().getName());
        }
    }

    public boolean setServer(){
        try {
            String host = serverTextField.getText();
            String portString = portTextField.getText();
            System.out.println("HOST: " + host + " PORT: " + portString);
            if(host == "" || host == null || portString == "" || portString == null){
                incorrectServerText.setText("You must enter both a host and a port!");
                return false;
            }else{
                int port = Integer.parseInt(portString);
                incorrectServerText.setText("");
                server.setHostAndPort(host, port);
            }
        }catch(NumberFormatException e){
            portTextField.clear();
            incorrectServerText.setText("Port must be a number!");
            return false;
        }catch(InvalidServerException e){
            serverTextField.clear();
            portTextField.clear();
            incorrectServerText.setText(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Method that is called when the play button is pressed, it:
     * a) Adds the player to the server and
     * b) Sets the avatar chosen from the user
     */
    public void play(){
        boolean serverReady = setServer();
        if(!serverReady){
            return;
        }
        try
        {
            Player p = getPlayer();
            Player serverPlayer = server.addPlayer(p);

            //store client player info received from the server
            clientData.setPlayer(serverPlayer);

            //update the avatar chosen to the specified path (String)
            avatarGenerator.setAvatarOnClient();
        }
        catch (WebApplicationException e)
        {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return;
        }

        mainCtrl.showGameModeSelection();
    }


    private Player getPlayer()
    {
        String userName = name.getText();
        if(userName.length() == 0)
            userName = "testUserX";
        var p = new Player(userName);

        return p;
    }

    public void incrementSeed(){
        avatarGenerator.incrementSeed();
    }

    public void decrementSeed(){
        avatarGenerator.decrementSeed();
    }

    public void jokersPopup(){
        mainCtrl.showJokerInfo();
    }
}
