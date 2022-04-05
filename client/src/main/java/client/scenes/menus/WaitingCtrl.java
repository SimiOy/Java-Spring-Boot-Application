package client.scenes.menus;

import client.avatar.AvatarSupplier;
import client.data.ClientData;
import client.game.Game;
import client.joker.JokerUtils;
import client.scenes.MainCtrl;
import client.utils.ClientUtils;
import client.utils.ClientUtilsImpl;
import client.utils.ServerUtils;
import com.talanlabs.avatargenerator.Avatar;
import com.talanlabs.avatargenerator.eightbit.EightBitAvatar;
import commons.Lobby;
import commons.Player;
import commons.WebsocketMessage;
import constants.ResponseCodes;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.springframework.messaging.simp.stomp.StompSession;

import javax.inject.Inject;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WaitingCtrl implements Initializable{

    private final ServerUtils server;
    private final ClientUtils client;
    private final ClientData clientData;
    private final JokerUtils jokerUtils;
    private final Game game;

    private final MainCtrl mainCtrl;

    private ObservableList<Player> playerData;

    @FXML
    private TableView<Player> tableView;
    @FXML
    private TableColumn<Player, String> avatarColumn;
    @FXML
    private TableColumn<Player, String> usernameColumn;


    @FXML
    private Text tip;
    @FXML
    private Text countDownLabel;
    @FXML
    private Text lobbyCode;
    @FXML
    private Button startGameButton;

    // ----- admin panel controls
    @FXML
    private Pane parentPane;
    @FXML
    private TextField noOfQuestionsSetting;
    @FXML
    private TextField difficultySetting;
    @FXML
    private Text playerToBeKicked;

    // ----- end of admin panel controls

    private List<Player> activePlayers;

    private Timer timer;
    private Avatar builder;
    private  Player selectedPlayer;

    private StompSession.Subscription lobbyStartSubscription;

    @Inject
    public WaitingCtrl(ServerUtils server, MainCtrl mainCtrl, ClientUtils client, ClientData clientData,
                       JokerUtils jokerUtils, Game game) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.client = client;
        this.clientData = clientData;
        this.jokerUtils = jokerUtils;
        this.game = game;
    }

    /**
     * Method that sets up how the scene should look like when switched to
     */
    public void load(){
        clientData.setQuestionCounter(0);

        resetUI();

        builder = EightBitAvatar.newMaleAvatarBuilder().build();
        showActivePlayers();

        if(client.getClass().equals(ClientUtilsImpl.class)) {
            client.setCurrentSceneCtrl(this);
        }

        registerLobbyStartSubscription();

        server.send("/app/requestUpdate",
                new WebsocketMessage(ResponseCodes.LOBBY_UPDATED, clientData.getClientLobby().getToken()));
    }

    public void registerLobbyStartSubscription()
    {
        if(lobbyStartSubscription == null) {
            lobbyStartSubscription = server.registerForMessages("/topic/lobbyStart", a -> {
                if (a.getCode() == ResponseCodes.START_GAME
                                && a.getLobbyToken().equals(clientData.getClientLobby().token)) {
                    killTimer();
                    System.out.println("ishost:" + clientData.getIsHost());
                    client.assignCountdownLabel(countDownLabel);
                    clientData.setLobby(server.getLobbyByToken(a.getNewToken()));
                    if (clientData.getIsHost())
                        server.send("/app/nextQuestion",
                                new WebsocketMessage(ResponseCodes.NEXT_QUESTION,
                                        clientData.getClientLobby().token, clientData.getClientPointer()));
                    jokerUtils.registerForJokerUpdates();
                }
            });
        }
    }

    /**
     * Method used to update the UI according to specific
     * lobbies/client info (i.e lobby codes, tips etc)
     */
    private void resetUI()
    {
        String token = clientData.getClientLobby().getToken();
        if(token.equals("COMMON")) {
            tip.setText("Theres only one correct answer per question, get the most right to win.");
            //default value for normal game
            game.setQuestionsToEndGame(20);
        }
        else
        {
            tip.setText("Share this lobby code to play with your friends! \n" + token);
        }
        lobbyCode.setText("WELCOME TO LOBBY: " + token);
        countDownLabel.setText("Currently waiting...");

        //enable admin UI
        if(clientData.getClientLobby().getHostId() != null
                && clientData.getClientPlayer().getId() == clientData.getClientLobby().getHostId())
        {
            //only the host is allowed to have access to these set of buttons (privileges)
            enableAdminPanel(true);
        }
        else
        {
            //maybe he was an ex-host
            enableAdminPanel(false);
        }
    }

    /**
     * Method that updates the UI according to the user's privileges
     * If he is the host, he gains access to numerous control panels,
     * which he can use to tweak his/her preferences
     */
    private void enableAdminPanel(boolean status)
    {
        if(status)
        {
            //only admins
            parentPane.setVisible(true);
            extractGameSettings();
        }
        else
        {
            parentPane.setVisible(false);
            if(!Objects.equals(clientData.getClientLobby().getToken(), "COMMON"))
            {
                //if in a private lobby, disable start game
                startGameButton.setVisible(false);
            }
            else
            {
                startGameButton.setVisible(true);
            }
        }
    }

    /**
     * Method that shows active players in a given lobby
     * The lobby field from ClientData should have been filled/updated prior to calling this method
     */
    public void showActivePlayers()
    {
        activePlayers = clientData.getClientLobby().getPlayersInLobby();
        refresh();
    }

    // Big credits to "https://codereview.stackexchange.com/questions/220969/javafx-lazy-loading-of-images-in-tableview"
    //for the idea of using executor services to schedule the load and/or generation of images
    //it's the only somewhat efficient solution I found, since it makes use of
    // listeners to cancel and monitor on-going tasks
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        usernameColumn.setCellValueFactory(q ->
                new SimpleStringProperty(q.getValue().name));

        ExecutorService exec = Executors.newCachedThreadPool();

        avatarColumn.setCellFactory(param -> {

            //Set up the ImageView
            final ImageView imageview = new ImageView();
            imageview.setFitWidth(35);
            imageview.setPreserveRatio(true);

            //Loading task
            ObjectProperty<Task<Image>> loadingTask = new SimpleObjectProperty<>();

            //Set up the Table
            TableCell<Player, String> cell = new TableCell<Player, String>() {
                @Override
                public void updateItem(String itemCode, boolean empty) {
                    //Stop already running image fetch tast
                    if (loadingTask.get() != null &&
                            loadingTask.get().getState() != Worker.State.SUCCEEDED &&
                            loadingTask.get().getState() != Worker.State.FAILED) {

                        loadingTask.get().cancel();
                    }
                    loadingTask.set(null);
                    //Load image if not null
                    if (empty || itemCode == null) {
                        imageview.setVisible(false);
                    } else {
                        imageview.setVisible(true);
                        Task<Image> task = new Task<Image>() {
                            @Override
                            public Image call() throws Exception {

                                //generate the image
                                Path newPath = AvatarSupplier.generateAvatar(builder, itemCode, null);
                                Image image = new Image(newPath.toString());

                                return image;
                            }
                        };
                        loadingTask.set(task);
                        task.setOnSucceeded(event -> {
                            imageview.setImage(task.getValue());
                        });
                        exec.submit(task);
                    }
                }
            };
            // Attach the imageview to the cell
            cell.setGraphic(imageview);
            return cell;
        });
        avatarColumn.setCellValueFactory(cellData ->  new SimpleStringProperty(cellData.getValue().getAvatarCode()));

        tableView.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, e-> {
            updateSelectedPlayer();
        });

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                refresh();
            }
        }, 0, 250);
    }

    /**
     * Method to be called on scene change to stop the running of unnecessary timers
     */
    public void killTimer()
    {
        timer.cancel();
    }


    /**
     * Refresh waiting screen page
     */
    public void refresh()
    {
        if(activePlayers != null && clientData.getClientPlayer() != null)
        {
            Lobby current = clientData.getClientLobby();
            clientData.setLobby(current);
            if(current == null)
            {
                //the lobby/player has been killed
                killTimer();
                return;
            }
            activePlayers = current.getPlayersInLobby();
            playerData = FXCollections.observableList(activePlayers);
            tableView.setItems(playerData);
        }
    }

    public void leaveLobby(){
        game.leaveLobby();
    }

    public void startGame(){
        game.startMultiplayerGame();
    }

    /**
     * Method that adds listeners to value changes, and connects it
     * to websockets for all other client
     * In the admin panel (# players, # questions, difficultyLevel)
     * Called only if admin (only in private lobbies)
     */
    private void extractGameSettings()
    {
        noOfQuestionsSetting.textProperty().addListener(((observable, oldValue, newValue) -> {
            int newQuestionNumber = Integer.parseInt(newValue);
            server.send("/app/setNoOfQuestions",
                    new WebsocketMessage(clientData.getClientLobby().getToken(), newQuestionNumber));
        }));

        difficultySetting.textProperty().addListener(((observable, oldValue, newValue) -> {
            int newDifficulty = Integer.parseInt(newValue);
            server.send("/app/setDifficulty",
                    new WebsocketMessage(clientData.getClientLobby().getToken(), newDifficulty));
        }));
    }

    /**
     * Method called on event handler,
     * whenever a row from the table view is selected
     * This method stores the information about the selected player
     */
    private void updateSelectedPlayer()
    {
        selectedPlayer = tableView.getSelectionModel().getSelectedItem();
        playerToBeKicked.setText(selectedPlayer.getName());
    }

    /**
     * Method that kicks a player from the lobby
     * the player should be removed from the lobby,
     * and his scene should change back to GameModeSelection Screen
     */
    public void kickPlayer()
    {
        if(clientData.getClientPlayer().equals(selectedPlayer))
        {
            //you cant kick yourself out easter-egg
            tip.setText("Kicking yourself out might require " +
                    "some thorough mental-check appointments (self love and all that)");
        }
        else {
            server.send("/app/kickFromLobby", new WebsocketMessage(ResponseCodes.KICK_PLAYER,
                    clientData.getClientLobby().getToken(), selectedPlayer));
        }
    }
}
