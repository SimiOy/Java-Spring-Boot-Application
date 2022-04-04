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
import client.scenes.admin.*;
import client.scenes.leaderboards.LeaderboardCtrl;
import client.scenes.leaderboards.TempLeaderboardCtrl;
import client.scenes.menus.GameModeSelectionCtrl;
import client.scenes.menus.HomeCtrl;
import client.scenes.menus.MultiplayerMenuCtrl;
import client.scenes.menus.WaitingCtrl;
import client.scenes.questions.EnergyAlternativeQuestionCtrl;
import client.scenes.questions.EstimationQuestionCtrl;
import client.scenes.questions.GameMCQCtrl;
import client.scenes.questions.GuessConsumptionCtrl;
import client.utils.ClientUtils;
import com.google.inject.Inject;
import commons.Question;
import jakarta.ws.rs.core.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import commons.Activity;

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

    private GuessConsumptionCtrl guessConsumptionCtrl;
    private Scene guessXScene;

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

    private KickPopUpCtrl kickPopUpCtrl;
    private Scene kickedPopUp;

    private EnergyAlternativeQuestionCtrl energyAlternativeQuestionCtrl;
    private Scene energyAlternative;

    private EditActivitiesCtrl editActivitiesCtrl;
    private Scene editActivity;

    private AdminHomeCtrl adminHomeCtrl;
    private Scene adminHome;

    private ActivityAdminCtrl activityAdminCtrl;
    private Scene activityAdmin;

    private QuestionAdminCtrl questionAdminCtrl;
    private Scene questionAdmin;

    private EditQuestionsCtrl editQuestionsCtrl;
    private Scene editQuestion;

    private Stage incorrectUsernamePopUp;

    private Stage kickPopUp;

    @Inject
    private ClientUtils client;

    @Inject
    private Game game;

    public void initialize(Stage primaryStage, Pair<HomeCtrl, Parent> home, Pair<LeaderboardCtrl, Parent> leaderboard,
                           Pair<GameModeSelectionCtrl, Parent> gameModeSelection, Pair<MultiplayerMenuCtrl,
                           Parent> multiplayerMenu, Pair<EstimationQuestionCtrl, Parent> estimationQuestion,
                           Pair<GameMCQCtrl, Parent> gameMCQ,
                           Pair<EnergyAlternativeQuestionCtrl, Parent> energyAlternative,
                           Pair<GuessConsumptionCtrl, Parent> guessXQuestion,
                           Pair<GameOverCtrl, Parent> gameOver, Pair<WaitingCtrl, Parent> waiting,
                           Pair<TempLeaderboardCtrl, Parent> tempLeaderboard,
                           Pair<UsernamePopUpCtrl, Parent> usernamePopUp,
                           Pair<KickPopUpCtrl, Parent> kickedPopUp,
                           Pair<AdminHomeCtrl, Parent> adminHome,
                           Pair<EditActivitiesCtrl, Parent> editActivity,
                           Pair<ActivityAdminCtrl, Parent> activityAdmin,
                           Pair<QuestionAdminCtrl, Parent> questionAdmin,
                           Pair<EditQuestionsCtrl, Parent> editQuestion) {
        this.primaryStage = primaryStage;


        this.waitingCtrl = waiting.getKey();
        this.waiting = new Scene(waiting.getValue());

        this.gameMCQCtrl = gameMCQ.getKey();
        this.gameMCQ = new Scene(gameMCQ.getValue());

        this.gameModeSelectionCtrl = gameModeSelection.getKey();
        this.gameModeSelection = new Scene(gameModeSelection.getValue());

        this.estimationQuestionCtrl = estimationQuestion.getKey();
        this.estimation = new Scene(estimationQuestion.getValue());

        this.guessConsumptionCtrl = guessXQuestion.getKey();
        this.guessXScene = new Scene(guessXQuestion.getValue());

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

        this.kickPopUpCtrl = kickedPopUp.getKey();
        this.kickedPopUp = new Scene(kickedPopUp.getValue());

        this.energyAlternativeQuestionCtrl = energyAlternative.getKey();
        this.energyAlternative = new Scene(energyAlternative.getValue());

        this.editActivitiesCtrl = editActivity.getKey();
        this.editActivity = new Scene(editActivity.getValue());

        this.adminHomeCtrl = adminHome.getKey();
        this.adminHome = new Scene(adminHome.getValue());

        this.activityAdminCtrl = activityAdmin.getKey();
        this.activityAdmin = new Scene(activityAdmin.getValue());

        this.questionAdminCtrl = questionAdmin.getKey();
        this.questionAdmin = new Scene(questionAdmin.getValue());

        this.editQuestionsCtrl = editQuestion.getKey();
        this.editQuestion = new Scene(editQuestion.getValue());

        primaryStage.setOnCloseRequest(e -> {
            if(client.isInLobby()){
                game.leaveLobby();
            }
        });

        showHome();
        primaryStage.setOnCloseRequest(evt -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Close");
            alert.setHeaderText("Do you want to close the game?");
            alert.showAndWait().filter(r -> r != ButtonType.OK).ifPresent(r->evt.consume());
        });
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
        gameMCQCtrl.load();
        primaryStage.setTitle("GameScreen");
        primaryStage.setScene(gameMCQ);
    }

    public void showEnergyAlternative(){
        client.setCurrentSceneCtrl(energyAlternativeQuestionCtrl);
        energyAlternativeQuestionCtrl.load();
        primaryStage.setTitle("GameScreen");
        primaryStage.setScene(energyAlternative);
    }

    public void showGameEstimation(){
        client.setCurrentSceneCtrl(estimationQuestionCtrl);
        estimationQuestionCtrl.load();
        estimationQuestionCtrl.addEventListeners(estimation);
        primaryStage.setTitle("GameScreen");
        primaryStage.setScene(estimation);
    }

    public void showGuessX(){
        client.setCurrentSceneCtrl(guessConsumptionCtrl);
        guessConsumptionCtrl.load();
        primaryStage.setTitle("GameScreen");
        primaryStage.setScene(guessXScene);
    }

    public void showActivityEdit(Activity act){
        client.setCurrentSceneCtrl(editActivitiesCtrl);
        primaryStage.setTitle("ActivityEditScreen");
        primaryStage.setScene(editActivity);
        editActivitiesCtrl.load(act);
    }

    public void showAdminHome(){
        client.setCurrentSceneCtrl(adminHomeCtrl);
        primaryStage.setTitle("AdminHomeScreen");
        primaryStage.setScene(adminHome);
        //adminHomeCtrl.load();
    }

    public void showAdminActivities(){
        client.setCurrentSceneCtrl(activityAdminCtrl);
        primaryStage.setTitle("AdminActivitiesScreen");
        primaryStage.setScene(activityAdmin);
        activityAdminCtrl.load();
    }

    public void showAdminQuestions(){
        client.setCurrentSceneCtrl(questionAdminCtrl);
        primaryStage.setTitle("AdminQuestionsScreen");
        primaryStage.setScene(questionAdmin);
        questionAdminCtrl.load();
    }

    public void showQuestionsEdit(Question q){
        client.setCurrentSceneCtrl(editQuestionsCtrl);
        primaryStage.setTitle("QuestionEditScreen");
        primaryStage.setScene(editQuestion);
        editQuestionsCtrl.load(q);
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
        gameModeSelectionCtrl.load();
    }

    public void showGameOver(){
        client.setCurrentSceneCtrl(gameOverCtrl);
        primaryStage.setTitle("GameOver");
        primaryStage.setScene(gameOver);
        gameOverCtrl.load();
    }

    public void showMultiplayerMenu(){
        client.setCurrentSceneCtrl(multiplayerMenuCtrl);
        multiplayerMenuCtrl.load();
        primaryStage.setTitle("MultiplayerMenu");
        primaryStage.setScene(multiplayerMenu);
    }

    public void showTempLeaderboard(){
        client.setCurrentSceneCtrl(tempLeaderboardCtrl);
        primaryStage.setTitle("TempLeaderboard");
        primaryStage.setScene(tempLeaderboard);
        tempLeaderboardCtrl.load();
    }

    public void showKickPopUp(){
        kickPopUp = new Stage();
        kickPopUp.setScene(kickedPopUp);
        kickPopUp.setTitle("Kicked");
        kickPopUp.initModality(Modality.APPLICATION_MODAL);
        kickPopUp.showAndWait();
    }

    public void closeKickPopUp(){
        kickPopUp.close();
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