package client.scenes;

import client.avatar.AvatarSupplier;
import client.data.ClientData;
import client.game.Game;
import client.utils.ClientUtils;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import com.talanlabs.avatargenerator.Avatar;
import com.talanlabs.avatargenerator.eightbit.EightBitAvatar;
import commons.LeaderboardEntry;
import commons.Player;
import commons.WebsocketMessage;
import constants.GameType;
import constants.ResponseCodes;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameOverCtrl {

    private final ClientUtils client;
    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final ClientData clientData;
    private final Game game;
    private Thread thread;

    private ObservableList<Player> currentTop10;
    private Avatar builder;

    @FXML
    private TableView<Player> table;
    @FXML
    private TableColumn rank;
    @FXML
    private TableColumn<Player, String> nameColumn;
    @FXML
    private TableColumn<Player, String> avatarColumn;
    @FXML
    private TableColumn<Player, Integer> scoreColumn;

    @Inject
    public GameOverCtrl(ServerUtils server, MainCtrl mainCtrl, ClientData clientData, Game game, ClientUtils client) {
        this.mainCtrl = mainCtrl;
        this.server = server;
        this.clientData = clientData;
        this.game = game;
        this.client = client;
    }

    /**
     * If the game was a public multiplayer or single-player we delete the previous lobby and join a new one
     * There is no need to recycle the lobby.
     * If the game is a private multiplayer lobby then we must recycle the lobby so that the lobby code is the same
     */
    public void playAgain() {
        thread.interrupt();
        if(clientData.getLastLobby().getSingleplayer() || clientData.getLastLobby().getPublic()) {
            System.out.println("Attempting to restart public/single-player lobby");
            //kill ongoing timers
            client.killTimer();

            if(clientData.getLastLobby().getSingleplayer()){
                server.send("/app/leaveLobby", new WebsocketMessage(ResponseCodes.LEAVE_LOBBY,
                        clientData.getClientLobby().getToken(), clientData.getClientPlayer(), clientData.getIsHost(),
                        true));
            }else{
                server.send("/app/leaveLobby", new WebsocketMessage(ResponseCodes.LEAVE_LOBBY,
                        clientData.getClientLobby().getToken(), clientData.getClientPlayer(), clientData.getIsHost(),
                        false));
            }


            //no more server polling for this client
            client.unsubscribeFromMessages();

            client.resetMessages();

            clientData.clearUnansweredQuestionCounter();

            System.out.println("Left the lobby");

            clientData.setAsHost(false);
        }else{
            System.out.println("Attempting to restart private lobby");
            game.endGame();
            server.send("/app/leaveLobby", new WebsocketMessage(ResponseCodes.LEAVE_LOBBY,
                    clientData.getClientLobby().getToken(), clientData.getClientPlayer(), clientData.getIsHost(),
                    false));
            client.killTimer();
            client.resetMessages();
            clientData.setClientScore(0);
            clientData.setQuestionCounter(0);
            clientData.clearUnansweredQuestionCounter();
            mainCtrl.showWaiting();
        }
        game.restartLobby(clientData.getLastLobby());
        System.out.println("Restarted the game!");
    }

    public void leaveGame() {
        thread.interrupt();
        game.leaveLobby();
    }

    public void load() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //give time for all clients to poll the server to update their leaderboard
                loadLeaderboard();
                //then remove the player
                //without this sleepthe client that connects last will be
                // missing information on the final table
//                    Thread.sleep(2000);
//                    removePlayerFromLobby();
            }
        });
        thread.start();
    }

    private void loadLeaderboard()
    {
        if(clientData.getGameType() == GameType.MULTIPLAYER) {
            currentTop10 = FXCollections.observableList(server.getTopByLobbyToken(
                    clientData.getClientLobby().getToken()));
        }
        if(clientData.getGameType() == GameType.SINGLEPLAYER) {
            currentTop10 = FXCollections.observableList(turnIntoPlayer(server.getTop10Scores()));
        }
        builder = EightBitAvatar.newMaleAvatarBuilder().build();

        rank.setCellValueFactory((Callback<TableColumn.CellDataFeatures<Player, String>, ObservableValue<String>>) q
                -> new SimpleStringProperty((table.getItems().indexOf(q.getValue()) + 1) + ""));

        nameColumn.setCellValueFactory(q -> new SimpleStringProperty(q.getValue().name));

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
        scoreColumn.setCellValueFactory(q -> new SimpleIntegerProperty(q.getValue().score).asObject());
        table.setItems(currentTop10);
    }

    /**
     * Turns a list of leaderboardentries into a list of players, such that they can be properly displayed in this
     * leaderboard
     * @param leaderboardEntries
     * @return
     */
    private List<Player> turnIntoPlayer(List<LeaderboardEntry> leaderboardEntries){
        List<Player> players = new ArrayList<>();
        for( LeaderboardEntry l : leaderboardEntries){
            players.add(new Player(l.getName(), l.getScore(), l.getAvatarCode()));
        }
        return players;
    }
}
