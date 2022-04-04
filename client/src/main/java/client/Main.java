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
package client;

import client.scenes.*;
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
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.stage.Stage;

import static com.google.inject.Guice.createInjector;

public class Main extends Application {

    private static final Injector INJECTOR = createInjector(new MyModule());
    private static final MyFXML FXML = new MyFXML(INJECTOR);

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {

        // Make pairs of controllers and parent nodes of their respective scenes (which are created in MainCtrl)
        var home = FXML.load(HomeCtrl.class, "client", "scenes", "Home.fxml");
        var gameModeSelection = FXML.load(GameModeSelectionCtrl.class, "client", "scenes", "GameModeSelection.fxml");
        var leaderboard = FXML.load(LeaderboardCtrl.class, "client", "scenes", "Leaderboard.fxml");
        var waiting = FXML.load(WaitingCtrl.class, "client", "scenes", "Waiting.fxml");
        var gameMCQ = FXML.load(GameMCQCtrl.class, "client", "scenes", "GameMCQ.fxml");
        var estimationQuestion = FXML.load(EstimationQuestionCtrl.class, "client", "scenes", "EstimationQuestion.fxml");
        var alternativeQuestion = FXML.load(
                EnergyAlternativeQuestionCtrl.class, "client", "scenes", "EnergyAlternativeQuestion.fxml");
        var guessMultipleChoiceQuestion = FXML.load(
                GuessConsumptionCtrl.class, "client", "scenes", "GuessConsumptionQuestion.fxml");
        var tempLeaderboard = FXML.load(TempLeaderboardCtrl.class, "client", "scenes", "TempLeaderboard.fxml");
        var gameOver = FXML.load(GameOverCtrl.class, "client", "scenes", "GameOver.fxml");
        var multiPlayerMenu = FXML.load(MultiplayerMenuCtrl.class, "client", "scenes", "MultiplayerMenu.fxml");
        var usernamePopUp = FXML.load(UsernamePopUpCtrl.class, "client", "scenes", "UsernamePopUp.fxml");
        var kickedPopUp = FXML.load(KickPopUpCtrl.class, "client", "scenes", "KickedPopUp.fxml");
        var adminHome = FXML.load(AdminHomeCtrl.class, "client", "scenes", "HomeAdmin.fxml");
        var activityAdmin = FXML.load(ActivityAdminCtrl.class, "client", "scenes", "ActivityAdmin.fxml");
        var editActivity = FXML.load(EditActivitiesCtrl.class, "client", "scenes", "EditActivity.fxml");
        var questionAdmin = FXML.load(QuestionAdminCtrl.class, "client", "scenes", "QuestionAdmin.fxml");
        var editQuestion = FXML.load(EditQuestionsCtrl.class, "client", "scenes", "EditQuestion.fxml");
        var jokerPopUp = FXML.load(JokerPopUpCtrl.class, "client", "scenes", "jokerPopup.fxml");
        // Stylesheets are loaded using reflection
        home.getValue().getStylesheets()
                .add(getClass().getResource("scenes/stylesheets/Home.css").toExternalForm());
        home.getValue().getStylesheets()
                .add(getClass().getResource("scenes/stylesheets/Button.css").toExternalForm());
        gameModeSelection.getValue().getStylesheets()
                .add(getClass().getResource("scenes/stylesheets/GameModeSelection.css").toExternalForm());
        gameModeSelection.getValue().getStylesheets()
                .add(getClass().getResource("scenes/stylesheets/Button.css").toExternalForm());
        leaderboard.getValue().getStylesheets()
                .add(getClass().getResource("scenes/stylesheets/Leaderboard.css").toExternalForm());
        multiPlayerMenu.getValue().getStylesheets()
                .add(getClass().getResource("scenes/stylesheets/MultiplayerMenu.css").toExternalForm());
        tempLeaderboard.getValue().getStylesheets()
                .add(getClass().getResource("scenes/stylesheets/TempLeaderboard.css").toExternalForm());
        waiting.getValue().getStylesheets()
                .add(getClass().getResource("scenes/stylesheets/Waiting.css").toExternalForm());
        waiting.getValue().getStylesheets()
                .add(getClass().getResource("scenes/stylesheets/Button.css").toExternalForm());
        kickedPopUp.getValue().getStylesheets().
                add(getClass().getResource("scenes/stylesheets/Button.css").toExternalForm());
        usernamePopUp.getValue().getStylesheets().
                add(getClass().getResource("scenes/stylesheets/Button.css").toExternalForm());
        jokerPopUp.getValue().getStylesheets()
                .add(getClass().getResource("scenes/stylesheets/Button.css").toExternalForm());
        gameOver.getValue().getStylesheets()
                .add(getClass().getResource("scenes/stylesheets/GameOver.css").toExternalForm());


        var mainCtrl = INJECTOR.getInstance(MainCtrl.class);
        mainCtrl.initialize(primaryStage, home, leaderboard, gameModeSelection, multiPlayerMenu,
                            estimationQuestion, gameMCQ, alternativeQuestion, guessMultipleChoiceQuestion, gameOver,
                waiting, tempLeaderboard, usernamePopUp, kickedPopUp, adminHome,
                editActivity, activityAdmin, questionAdmin, editQuestion, jokerPopUp);
    }
}