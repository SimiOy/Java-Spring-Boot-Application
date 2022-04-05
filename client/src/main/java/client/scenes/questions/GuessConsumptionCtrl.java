package client.scenes.questions;

import client.data.ClientData;
import client.emotes.Emotes;
import client.game.Game;
import client.joker.JokerPowerUps;
import client.joker.JokerUtils;
import client.scenes.MainCtrl;
import client.utils.ClientUtils;
import client.utils.ServerUtils;
import commons.Activity;
import commons.Question;
import commons.WebsocketMessage;
import constants.GameType;
import constants.JokerType;
import constants.ResponseCodes;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import javax.inject.Inject;
import java.util.List;
import java.util.Random;

import static constants.QuestionTypes.GUESS_X;

public class GuessConsumptionCtrl implements JokerPowerUps {

    private final ServerUtils server;
    private final ClientUtils client;
    private final MainCtrl mainCtrl;
    private final ClientData clientData;
    private final Emotes emotes;
    private final Game game;
    protected boolean doublePoints = false;
    private JokerUtils jokerUtils;

    @FXML
    private ProgressBar pb;

    @FXML
    private Text scoreTxt;

    @FXML
    private Text nQuestionsTxt;

    @FXML
    private Label questionTxt;

    final ToggleGroup radioGroup = new ToggleGroup();

    @FXML
    private RadioButton answer1;
    @FXML
    private RadioButton answer2;
    @FXML
    private RadioButton answer3;
    @FXML
    private Pane doublePointsJoker;
    @FXML
    private Pane eliminateAnswerJoker;
    @FXML
    private Pane halfTimeJoker;
    @FXML
    private Text halfTimeText;

    private int correctAnswer;
    private int revealedAnswer;

    @FXML
    private MenuButton emotesMenu;
    @FXML
    private Pane commTab;
    @FXML
    private Label messageTxt1;
    @FXML
    private Label messageTxt2;
    @FXML
    private Label messageTxt3;

    @FXML
    private ImageView imageView;

    @FXML
    private Label activityText;

    @FXML
    private Pane answerOneContainer;
    @FXML
    private Pane answerTwoContainer;
    @FXML
    private Pane answerThreeContainer;
    @FXML
    private Label answerOneText;
    @FXML
    private Label answerTwoText;
    @FXML
    private Label answerThreeText;
    @FXML
    private ImageView hourglassImageView;
    @FXML
    private ImageView insightImageView;
    @FXML
    private ImageView doubleImageView;

    @Inject
    public GuessConsumptionCtrl(ServerUtils server, ClientUtils client, MainCtrl mainCtrl, ClientData clientData,
                       JokerUtils jokerUtils, Emotes emotes, Game game) {
        this.jokerUtils = jokerUtils;
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.client = client;
        this.clientData = clientData;
        this.emotes = emotes;
        this.game = game;
    }

    public void leaveGame(){
        game.leaveLobby();
    }

    public void load() {
        if(client.isInLobby()) {
            setUpEmoteMenu();
            nQuestionsTxt.setText(clientData.getQuestionCounter() + "/" + game.getQuestionsToEndGame());
            Question question = clientData.getClientQuestion();
            resetUI(question);
        }
        hourglassImageView.setImage(new Image("/images/hourglass.png"));
        insightImageView.setImage(new Image("/images/insight.png"));
        doubleImageView.setImage(new Image("/images/double.png"));
    }

    public void resetUI(Question question)
    {
        scoreTxt.setText("Score: " + clientData.getClientScore());

        doublePoints = false;
        jokerUtils.resetJokerUI(halfTimeJoker, doublePointsJoker, eliminateAnswerJoker);

        revealedAnswer = -1;

        answer1.setToggleGroup(radioGroup);
        answer2.setToggleGroup(radioGroup);
        answer3.setToggleGroup(radioGroup);

        answer1.setDisable(false);
        answer2.setDisable(false);
        answer3.setDisable(false);

        answerOneContainer.getStyleClass().add("image-button");
        answerTwoContainer.getStyleClass().add("image-button");
        answerThreeContainer.getStyleClass().add("image-button");

        answer1.setStyle(" -fx-background-color: transparent; ");
        answer2.setStyle(" -fx-background-color: transparent; ");
        answer3.setStyle(" -fx-background-color: transparent; ");
        answerOneContainer.setStyle(" -fx-background-color: white; ");
        answerTwoContainer.setStyle(" -fx-background-color: white; ");
        answerThreeContainer.setStyle(" -fx-background-color: white; ");


        if(answer1.isSelected()) answer1.setSelected(false);
        if(answer2.isSelected()) answer2.setSelected(false);
        if(answer3.isSelected()) answer3.setSelected(false);

        client.startTimer(pb,this, GUESS_X);

        questionTxt.setText(question.getText());

        Random random = new Random();
        correctAnswer = random.nextInt(3);

        List<Activity> list = server.getActivitiesFromIDs(question.getFoundActivities());

        switch (correctAnswer)
        {
            case 0:
                //correct answer is first one
                randomizeFields(answerOneText, answerTwoText, answerThreeText,question);
                break;
            case 1:
                //correct answer is second one
                randomizeFields(answerTwoText,answerOneText,answerThreeText,question);
                break;
            case 2:
                //correct answer is third one
                randomizeFields(answerThreeText,answerOneText,answerTwoText,question);
                break;
            default:
                break;
        }
        Activity polledActivity = server.getActivityByID(question.getFoundActivities().get(0)).get();
        questionTxt.setText(question.getText());
        activityText.setText(polledActivity.getTitle());
        Image image = server.getImageFromActivity(polledActivity);
        imageView.setImage(image);
    }

    public void randomizeFields(Label a, Label b, Label c, Question question)
    {
        List<Activity> list = server.getActivitiesFromIDs(question.getFoundActivities());

        a.setText(String.valueOf(list.get(0).getEnergyConsumption()) + " Wh");
        b.setText(String.valueOf(list.get(1).getEnergyConsumption()) + " Wh");
        c.setText(String.valueOf(list.get(2).getEnergyConsumption()) + " Wh");
    }

    public void disableAnswers(){
        answer1.setDisable(true);
        answer2.setDisable(true);
        answer3.setDisable(true);
    }

    public void nextQuestion(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Platform.runLater(() -> updateCorrectAnswer());
                    //sleep for two seconds to update ui and let the user see the correct answer

                    Thread.sleep(2000);

                    if(clientData.getQuestionCounter() == game.getQuestionsToDisplayLeaderboard() &&
                    clientData.getGameType() == GameType.MULTIPLAYER){
                        Platform.runLater(() -> mainCtrl.showTempLeaderboard());
                        Thread.sleep(5000);
                    }

                    //execute next question immediatly after sleep on current thread finishes execution
                    Platform.runLater(() -> client.getQuestion());
                    //client.getQuestion();
                }catch (InterruptedException e)
                {
                    e.printStackTrace();
                    System.out.println("Something went wrong when showing correct answer!");
                }
            }
        });
        thread.start();
    }

    public void updateCorrectAnswer()
    {

        int pointsToAdd = doublePoints ? 1000 : 500;
        doublePoints = false;

        if(clientData.getIsHost())
        {
            //if host prepare next question
            //client.prepareQuestion();

            server.send("/app/nextQuestion",
                    new WebsocketMessage(ResponseCodes.NEXT_QUESTION,
                            clientData.getClientLobby().token, clientData.getClientPointer()));
        }

        switch (correctAnswer)
        {
            case 0:
                if(answer1.equals(radioGroup.getSelectedToggle())){
                    clientData.setClientScore(clientData.getClientScore() +
                            (int) (pointsToAdd* client.getCoefficient()));
                }
                answerOneContainer.setStyle("-fx-background-color: green");
                answerTwoContainer.setStyle("-fx-background-color: red");
                answerThreeContainer.setStyle("-fx-background-color: red");
                break;
            case 1:
                if(answer2.equals(radioGroup.getSelectedToggle())){
                    clientData.setClientScore(clientData.getClientScore() +
                            (int) (pointsToAdd* client.getCoefficient()));
                }
                answerTwoContainer.setStyle("-fx-background-color: green");
                answerOneContainer.setStyle("-fx-background-color: red");
                answerThreeContainer.setStyle("-fx-background-color: red");
                break;
            case 2:
                if(answer3.equals(radioGroup.getSelectedToggle())){
                    clientData.setClientScore(clientData.getClientScore() +
                            (int) (pointsToAdd* client.getCoefficient()));
                }
                answerThreeContainer.setStyle("-fx-background-color: green");
                answerOneContainer.setStyle("-fx-background-color: red");
                answerTwoContainer.setStyle("-fx-background-color: red");
                break;
            default:
                break;
        }
        if(answer3.isSelected() == false && answer2.isSelected() == false && answer1.isSelected() == false){
            clientData.incrementUnansweredQuestionCounter();
            if(clientData.getUnansweredQuestionCounter() >= 5){
                leaveGame();
            }
        }
        scoreTxt.setText("Score: " + clientData.getClientScore());

        clientData.getClientPlayer().score = clientData.getClientScore();
        server.send("/app/updateScore", new WebsocketMessage(ResponseCodes.SCORE_UPDATED,
                clientData.getClientLobby().getToken(), clientData.getClientPlayer()));
    }

    /**
     * Get a random answer and if:
     * a) the answer is wrong, mark it as disabled
     * b) if the answer is the correct one, disable the answer after that (which will b a wrong one)
     */
    public void eliminateRandomWrongAnswer(){
        if(!clientData.getUsedJokers().contains(JokerType.ELIMINATE_ANSWERS)) {
            clientData.addJoker(JokerType.ELIMINATE_ANSWERS);
            eliminateAnswerJoker.setStyle("-fx-background-color: gray");
            eliminateAnswerJoker.setDisable(true);
            int indexToRemove = new Random().nextInt(3);
            if (indexToRemove == correctAnswer) {
                indexToRemove++;
            }
            revealedAnswer = indexToRemove % 3;
            switch (indexToRemove % 3) {
                case 0:
                    answerOneContainer.setStyle(" -fx-background-color: red; ");
                    System.out.println("Disabled first answer");
                    break;
                case 1:
                    answerTwoContainer.setStyle(" -fx-background-color: red; ");
                    System.out.println("Disabled second answer");
                    break;
                case 2:
                    answerThreeContainer.setStyle(" -fx-background-color: red; ");
                    System.out.println("Disabled third answer");
                    break;
            }
        }
    }

    public MenuButton getEmotesMenu() {
        return emotesMenu;
    }

    public RadioButton getAnswer1() {
        return answer1;
    }


    public RadioButton getAnswer2() {
        return answer2;
    }


    public RadioButton getAnswer3() {
        return answer3;
    }

    /**
     * Returns the label corresponding to the position in the method name.
     * @return label corresponding to the position
     */
    public Label getMessageTxt1() {
        return messageTxt1;
    }

    public Label getMessageTxt2() {
        return messageTxt2;
    }

    public Label getMessageTxt3() {
        return messageTxt3;
    }

    /**
     * Sets the label text to the given string and when said string is not empty,
     * a background colour is also added to make the message stand out more.
     * This background colour is removed however when the string is empty in order to reset.
     * @param message message to be displayed in the label corresponding to the method name
     */
    //empty string check might be used later in order to make messages disappear after X time
    public void setMessageTxt1(String message) {
        messageTxt1.setText(message);
        if(!(message.equals(""))){
            messageTxt1.setStyle("-fx-background-color: white; -fx-padding: 10px");
            messageTxt1.getStyleClass().add("roundedEdge");
        }
        else{
            messageTxt1.setStyle("-fx-background-color: none; -fx-padding: 0px");
            messageTxt1.getStyleClass().remove("roundedEdge");
        }
    }

    public void setMessageTxt2(String message) {
        messageTxt2.setText(message);
        if(!(message.equals(""))){
            messageTxt2.setStyle("-fx-background-color: white; -fx-padding: 10px");
            messageTxt2.getStyleClass().add("roundedEdge");
        }
        else{
            messageTxt2.setStyle("-fx-background-color: none; -fx-padding: 0px");
            messageTxt2.getStyleClass().remove("roundedEdge");
        }
    }

    public void setMessageTxt3(String message) {
        messageTxt3.setText(message);
        if(!(message.equals(""))){
            messageTxt3.setStyle("-fx-background-color: white; -fx-padding: 10px");
            messageTxt3.getStyleClass().add("roundedEdge");
        }
        else{
            messageTxt3.setStyle("-fx-background-color: none; -fx-padding: 0px");
            messageTxt3.getStyleClass().remove("roundedEdge");
        }
    }

    /**
     * Sets up the emoteMenu menubutton by first clearing anything that might be left in it from previous calls.
     * This is done to prevent errors from occurring. Then all the emotes from the list in the EmotesImpl class are
     * added and set such they trigger the sendEmote method when clicked. There's also some padding added to make it
     * easier to click the buttons.
     */
    public void setUpEmoteMenu(){
        emotesMenu.getItems().clear();
        emotesMenu.getItems().addAll(emotes.getEmotesList());
        for(MenuItem m : emotesMenu.getItems()){
            m.setStyle("-fx-padding: 0 25 0 25");
            m.setOnAction(a -> {
                emotes.sendEmote(m.getText());
            });
        }
    }
    @Override
    public void doublePoints() {
        if(!clientData.getUsedJokers().contains(JokerType.DOUBLE_POINTS)) {
            doublePoints = true;
            doublePointsJoker.setDisable(true);
            doublePointsJoker.setStyle("-fx-background-color: gray");
            clientData.addJoker(JokerType.DOUBLE_POINTS);
        }
    }

    @Override
    public void halfTimeForOthers() {
        if(!clientData.getUsedJokers().contains(JokerType.HALF_TIME_FOR_ALL_LOBBY)) {
            halfTimeJoker.setDisable(true);
            halfTimeJoker.setStyle("-fx-background-color: gray");
            clientData.addJoker(JokerType.HALF_TIME_FOR_ALL_LOBBY);
            System.out.println("Time was halved");
            jokerUtils.setLobbyJoker(JokerType.HALF_TIME_FOR_ALL_LOBBY);
            jokerUtils.sendJoker();
            emotes.sendJokerUsed();
        }
    }

    public Pane getHalfTimeJoker() {
        return halfTimeJoker;
    }

    public Text getHalfTimeText() {
        return halfTimeText;
    }

    public void answerOneSelected(){
        answer1.fire();
        if(revealedAnswer != 0) answerOneContainer.setStyle("-fx-background-color: gray");
        if(revealedAnswer != 1) answerTwoContainer.setStyle("-fx-background-color: white");
        if(revealedAnswer != 2) answerThreeContainer.setStyle("-fx-background-color: white");
    }

    public void answerTwoSelected(){
        answer2.fire();
        if(revealedAnswer != 0) answerOneContainer.setStyle("-fx-background-color: white");
        if(revealedAnswer != 1) answerTwoContainer.setStyle("-fx-background-color: gray");
        if(revealedAnswer != 2) answerThreeContainer.setStyle("-fx-background-color: white");
    }

    public void answerThreeSelected(){
        answer3.fire();
        if(revealedAnswer != 0) answerOneContainer.setStyle("-fx-background-color: white");
        if(revealedAnswer != 1) answerTwoContainer.setStyle("-fx-background-color: white");
        if(revealedAnswer != 2) answerThreeContainer.setStyle("-fx-background-color: gray");
    }

    public Pane getCommTab() {
        return commTab;
    }
}
