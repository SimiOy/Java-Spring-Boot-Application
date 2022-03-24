/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.scenes;

import client.game.Game;
import client.scenes.leaderboards.LeaderboardCtrl;
import client.scenes.leaderboards.TempLeaderboardCtrl;
import client.scenes.menus.GameModeSelectionCtrl;
import client.scenes.menus.HomeCtrl;
import client.scenes.menus.MultiplayerMenuCtrl;
import client.scenes.menus.WaitingCtrl;
import client.scenes.questions.EnergyAlternativeQuestionCtrl;
import client.scenes.questions.EstimationQuestionCtrl;
import client.scenes.questions.GameMCQCtrl;
import client.utils.ClientUtils;
import com.google.inject.Inject;
import jakarta.ws.rs.core.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

public class MainCtrl extends Application {

    private Stage primaryStage;

    private WaitingCtrl waitingCtrl;
    private Scene waiting;

    private GameMCQCtrl gameMCQCtrl;
    private Scene gameMCQ;

    private GameModeSelectionCtrl gameModeSelectionCtrl;
    private Scene gameModeSelection;

    private EstimationQuestionCtrl estimationQuestionCtrl;
    private Scene estimation;

    private GameOverCtrl gameOverCtrl;
    private Scene gameOver;

    private HomeCtrl homeCtrl;
    private Scene home;

    private LeaderboardCtrl leaderboardCtrl;
    private Scene leaderboard;

    private MultiplayerMenuCtrl multiplayerMenuCtrl;
    private Scene multiplayerMenu;

    private TempLeaderboardCtrl tempLeaderboardCtrl;
    private Scene tempLeaderboard;

    private UsernamePopUpCtrl usernamePopUpCtrl;
    private Scene usernamePopUp;

    private EnergyAlternativeQuestionCtrl energyAlternativeQuestionCtrl;
    private Scene energyAlternative;

    private MessageTabCtrl messageTabCtrl;
    private Scene messageTab;

    private Stage incorrectUsernamePopUp;

    @Inject
    private ClientUtils client;

    @Inject
    private Game game;

    public void initialize(Stage primaryStage, Pair<HomeCtrl, Parent> home, Pair<LeaderboardCtrl, Parent> leaderboard,
                           Pair<GameModeSelectionCtrl, Parent> gameModeSelection, Pair<MultiplayerMenuCtrl,
                           Parent> multiplayerMenu, Pair<EstimationQuestionCtrl, Parent> estimationQuestion,
                           Pair<GameMCQCtrl, Parent> gameMCQ,
                           Pair<EnergyAlternativeQuestionCtrl, Parent> energyAlternative,
                           Pair<GameOverCtrl, Parent> gameOver, Pair<WaitingCtrl, Parent> waiting,
                           Pair<TempLeaderboardCtrl, Parent> tempLeaderboard,
                           Pair<MessageTabCtrl, Parent> messageTab,
                           Pair<UsernamePopUpCtrl, Parent> usernamePopUp) {
        this.primaryStage = primaryStage;


        this.waitingCtrl = waiting.getKey();
        this.waiting = new Scene(waiting.getValue());

        this.gameMCQCtrl = gameMCQ.getKey();
        this.gameMCQ = new Scene(gameMCQ.getValue());

        this.gameModeSelectionCtrl = gameModeSelection.getKey();
        this.gameModeSelection = new Scene(gameModeSelection.getValue());

        this.estimationQuestionCtrl = estimationQuestion.getKey();
        this.estimation = new Scene(estimationQuestion.getValue());

        this.gameOverCtrl = gameOver.getKey();
        this.gameOver = new Scene(gameOver.getValue());

        this.homeCtrl = home.getKey();
        this.home = new Scene(home.getValue());

        this.leaderboardCtrl = leaderboard.getKey();
        this.leaderboard = new Scene(leaderboard.getValue());

        this.multiplayerMenuCtrl = multiplayerMenu.getKey();
        this.multiplayerMenu = new Scene(multiplayerMenu.getValue());

        this.tempLeaderboardCtrl = tempLeaderboard.getKey();
        this.tempLeaderboard = new Scene(tempLeaderboard.getValue());

        this.usernamePopUpCtrl = usernamePopUp.getKey();
        this.usernamePopUp = new Scene(usernamePopUp.getValue());

        this.energyAlternativeQuestionCtrl = energyAlternative.getKey();
        this.energyAlternative = new Scene(energyAlternative.getValue());

        this.messageTabCtrl = messageTab.getKey();
        this.messageTab = new Scene(messageTab.getValue());

        primaryStage.setOnCloseRequest(e -> {
            if(client.isInLobby()){
                game.leaveLobby();
            }
        });

        showHome();
        primaryStage.show();
    }


    public void showWaiting(){
        client.setCurrentSceneCtrl(waitingCtrl);
        primaryStage.setTitle("WaitingScreen");
        primaryStage.setScene(waiting);
        waitingCtrl.load();
    }

    public void showGameMCQ(){
        client.setCurrentSceneCtrl(gameMCQCtrl);
        primaryStage.setTitle("GameScreen");
        primaryStage.setScene(gameMCQ);
        gameMCQCtrl.load();
    }

    public void showEnergyAlternative(){
        primaryStage.setTitle("GameScreen");
        primaryStage.setScene(energyAlternative);
        energyAlternativeQuestionCtrl.load();
    }

    public void showGameEstimation(){
        client.setCurrentSceneCtrl(estimationQuestionCtrl);
        primaryStage.setTitle("GameScreen");
        primaryStage.setScene(estimation);
        estimationQuestionCtrl.load();
    }

    public void showHome(){
        client.setCurrentSceneCtrl(homeCtrl);
        primaryStage.setTitle("Home");
        primaryStage.setScene(home);
        homeCtrl.load();
    }

    public void showLeaderboard(){
        client.setCurrentSceneCtrl(leaderboardCtrl);
        primaryStage.setTitle("Leaderboard");
        primaryStage.setScene(leaderboard);
        leaderboardCtrl.load();
    }
    public void showGameModeSelection(){
        client.setCurrentSceneCtrl(gameModeSelectionCtrl);
        primaryStage.setTitle("GameModeSelection");
        primaryStage.setScene(gameModeSelection);
    }

    public void showGameOver(){
        client.setCurrentSceneCtrl(gameOverCtrl);
        primaryStage.setTitle("GameOver");
        primaryStage.setScene(gameOver);
        gameOverCtrl.load();
    }

    public void showMultiplayerMenu(){
        client.setCurrentSceneCtrl(multiplayerMenuCtrl);
        primaryStage.setTitle("MultiplayerMenu");
        primaryStage.setScene(multiplayerMenu);
    }

    public void showTempLeaderboard(){
        client.setCurrentSceneCtrl(tempLeaderboardCtrl);
        primaryStage.setTitle("TempLeaderboard");
        primaryStage.setScene(tempLeaderboard);
        tempLeaderboardCtrl.load();
    }

    public void showPopUp(String lobbyType){
        incorrectUsernamePopUp = new Stage();
        incorrectUsernamePopUp.setScene(usernamePopUp);
        incorrectUsernamePopUp.setTitle("Incorrect Username");
        incorrectUsernamePopUp.initModality(Modality.APPLICATION_MODAL);
        incorrectUsernamePopUp.showAndWait();

        //tries to join the lobby again
        switch(lobbyType){
            case "public":
                multiplayerMenuCtrl.joinPublicLobby();
                break;
        }

    }

    public void closePopUp(){
        incorrectUsernamePopUp.close();
    }
}