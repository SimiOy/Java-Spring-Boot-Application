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
import java.util.Optional;
import java.util.Random;

import static constants.QuestionTypes.ENERGY_ALTERNATIVE_QUESTION;

public class EnergyAlternativeQuestionCtrl implements JokerPowerUps {
    private final ClientData clientData;
    private final ClientUtils client;
    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final Emotes emotes;
    private final Game game;

    @FXML
    private Text scoreTxt;
    @FXML
    private Text nQuestionsTxt;
    @FXML
    private ProgressBar pb;
    @FXML
    private Label insteadOfText;
    @FXML
    private Text actualWH1, actualWH2, actualWH3;

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
    protected boolean doublePoints = false;
    private JokerUtils jokerUtils;

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
    private ImageView imageView1;
    @FXML
    private ImageView imageView2;
    @FXML
    private ImageView imageView3;
    @FXML
    private ImageView bigImageView;

    @FXML
    private ImageView hourglassImageView;
    @FXML
    private ImageView insightImageView;
    @FXML
    private ImageView doubleImageView;

    @FXML
    private Pane answerOneContainer;
    @FXML
    private Pane answerTwoContainer;
    @FXML
    private Pane answerThreeContainer;

    @Inject
    public EnergyAlternativeQuestionCtrl(ClientData clientData, ClientUtils  client, ServerUtils server,
                                         JokerUtils jokerUtils, Emotes emotes, Game game, MainCtrl mainCtrl) {
        this.jokerUtils = jokerUtils;
        this.clientData = clientData;
        this.client = client;
        this.server = server;
        this.game = game;
        this.mainCtrl = mainCtrl;
        this.emotes = emotes;
    }

    public void load() {
        if(client.isInLobby()) {
            setUpEmoteMenu();
            Question question = clientData.getClientQuestion();
            resetUI(question);
        }
        hourglassImageView.setImage(new Image("/images/hourglass.png"));
        insightImageView.setImage(new Image("/images/insight.png"));
        doubleImageView.setImage(new Image("/images/double.png"));
    }

    public void leaveGame(){
        game.leaveLobby();
    }

    /**
     * Setting up the UI for this scene. Note that:
     * the first activity returned from the server is the 'instead of' activity
     * the second is the correct alternative
     * the third and fourth are the wrong alternatives
     * @param question
     */
    private void resetUI(Question question) {
        scoreTxt.setText("Score: " + clientData.getClientScore());
        nQuestionsTxt.setText(clientData.getQuestionCounter() + "/" + game.getQuestionsToEndGame());
        doublePoints = false;
        jokerUtils.resetJokerUI(halfTimeJoker, doublePointsJoker, eliminateAnswerJoker);

        revealedAnswer = -1;

        answer1.setToggleGroup(radioGroup);
        answer2.setToggleGroup(radioGroup);
        answer3.setToggleGroup(radioGroup);

        answer1.setDisable(false);
        answer2.setDisable(false);
        answer3.setDisable(false);

        actualWH1.setVisible(false);
        actualWH2.setVisible(false);
        actualWH3.setVisible(false);

        answerOneContainer.getStyleClass().add("image-button");
        answerTwoContainer.getStyleClass().add("image-button");
        answerThreeContainer.getStyleClass().add("image-button");

        answer1.setStyle(" -fx-background-color: transparent; ");
        answer2.setStyle(" -fx-background-color: transparent; ");
        answer3.setStyle(" -fx-background-color: transparent; ");
        answerOneContainer.setStyle(" -fx-background-color: white; ");
        answerTwoContainer.setStyle(" -fx-background-color: white; ");
        answerThreeContainer.setStyle(" -fx-background-color: white; ");

        Optional<Activity> act = server.getActivityByID(question.getFoundActivities().get(0));
        String textMethod = question.getText();
        if(act.isPresent()) {
            textMethod += " " + act.get().getTitle();
        }
        insteadOfText.setText(textMethod);
        Image mainImage = server.getImageFromActivityId(question.getFoundActivities().get(0));
        bigImageView.setImage(mainImage);

        if(answer1.isSelected()) answer1.setSelected(false);
        if(answer2.isSelected()) answer2.setSelected(false);
        if(answer3.isSelected()) answer3.setSelected(false);

        client.startTimer(pb,this, ENERGY_ALTERNATIVE_QUESTION);

        switch (correctAnswer)
        {
            case 0:
                //correct answer is first one
                randomizeFields(answer1,answer2,answer3,question);
                setImages(imageView1, imageView2, imageView3, question);
                break;
            case 1:
                //correct answer is second one
                randomizeFields(answer2,answer1,answer3,question);
                setImages(imageView2, imageView1, imageView3, question);
                break;
            case 2:
                //correct answer is third one
                randomizeFields(answer3,answer1,answer2,question);
                setImages(imageView3, imageView1, imageView2, question);
                break;
            default:
                break;
        }
    }

    public void disableAnswers(){
        answer1.setDisable(true);
        answer2.setDisable(true);
        answer3.setDisable(true);
    }

    public void randomizeFields(RadioButton a, RadioButton b, RadioButton c, Question question)
    {
        List<Activity> list = server.getActivitiesFromIDs(question.getFoundActivities());
        a.setText(list.get(1).getTitle());
        b.setText(list.get(2).getTitle());
        c.setText(list.get(3).getTitle());

        actualWH1.setText(list.get(0).getEnergyConsumption().toString() + " Wh");
        actualWH2.setText(list.get(1).getEnergyConsumption().toString() + " Wh");
        actualWH3.setText(list.get(2).getEnergyConsumption().toString() + " Wh");
    }

    private void setImages(ImageView a, ImageView b, ImageView c, Question question) {
        List<Long> activitiesIds = question.getFoundActivities();

        Image firstImage = server.getImageFromActivityId(activitiesIds.get(1));
        a.setImage(firstImage);

        Image secondImage = server.getImageFromActivityId(activitiesIds.get(2));
        b.setImage(secondImage);

        Image thirdImage = server.getImageFromActivityId(activitiesIds.get(3));
        c.setImage(thirdImage);
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

                    Platform.runLater(() -> client.getQuestion());
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
            server.send("/app/nextQuestion",
                    new WebsocketMessage(ResponseCodes.NEXT_QUESTION,
                            clientData.getClientLobby().getToken(), clientData.getClientPointer()));
        }
        actualWH1.setVisible(true);
        actualWH2.setVisible(true);
        actualWH3.setVisible(true);
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
                mainCtrl.showKickPopUp();
            }
        }
        scoreTxt.setText("Score: " + clientData.getClientScore());
        clientData.getClientPlayer().score = clientData.getClientScore();
        server.send("/app/updateScore", new WebsocketMessage(ResponseCodes.SCORE_UPDATED,
                clientData.getClientLobby().getToken(), clientData.getClientPlayer()));
    }

    public void eliminateRandomWrongAnswer() {
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
    public MenuButton getEmotesMenu() {
        return emotesMenu;
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
            messageTxt1.setStyle("-fx-background-color: darkgray; -fx-padding: 10px");
        }
        else{
            messageTxt1.setStyle("-fx-background-color: none; -fx-padding: 0px");
        }
    }

    public void setMessageTxt2(String message) {
        messageTxt2.setText(message);
        if(!(message.equals(""))){
            messageTxt2.setStyle("-fx-background-color: darkgray; -fx-padding: 10px");
        }
        else{
            messageTxt2.setStyle("-fx-background-color: none; -fx-padding: 0px");
        }
    }

    public void setMessageTxt3(String message) {
        messageTxt3.setText(message);
        if(!(message.equals(""))){
            messageTxt3.setStyle("-fx-background-color: darkgray; -fx-padding: 10px");
        }
        else{
            messageTxt3.setStyle("-fx-background-color: none; -fx-padding: 0px");
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

    /**
     * Methods that handle what happens when the client clicks one of the images
     */
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