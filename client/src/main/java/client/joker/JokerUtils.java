package client.joker;

import client.data.ClientData;
import client.scenes.questions.EnergyAlternativeQuestionCtrl;
import client.scenes.questions.EstimationQuestionCtrl;
import client.scenes.questions.GameMCQCtrl;
import client.utils.ClientUtils;
import client.utils.ServerUtils;
import commons.WebsocketMessage;
import constants.JokerType;
import javafx.scene.layout.Pane;
import org.springframework.messaging.simp.stomp.StompSession;

import javax.inject.Inject;

/**
 * Class where the client interacts with the server through websockets
 * so every player of the same lobby gets the same affects from the jokers
 *
 * clientJoker -> joker applicable only to this specific client (ex double points)
 * lobbyJoker -> joker applicable to all players in the same lobby as this specific client, except this client
 *              (ex half available time for the others)
 */
public class JokerUtils {

    private JokerType lobbyJoker;

    private ClientData clientData;

    private ClientUtils client;

    private ServerUtils server;

    private StompSession.Subscription jokersSubscription;

    @Inject
    public JokerUtils(ClientUtils client, ServerUtils server, ClientData clientData) {
        this.client = client;
        this.server = server;
        this.clientData = clientData;
    }

    /**
     * After just joining a lobby, the client registers for joker updates
     */
    public void registerForJokerUpdates(){
        if(jokersSubscription == null) {
            jokersSubscription = server.registerForMessages("/topic/updateJoker", msg -> {
                if (msg.getLobbyToken().equals(clientData.getClientLobby().token)) {
                    lobbyJoker = msg.getJokerType();
                    System.out.println("received joker " + lobbyJoker);
                    handleJokerCases(msg.getSenderName());
                }
            });
        }
        clientData.resetJokers();
    }

    /**
     * Method that sends joker that was clicked on this client to server which will then distribute the message to
     * all other players of the lobby
     */
    public void sendJoker(){
        System.out.println("sending joker");
        server.send("/app/updateJoker", new WebsocketMessage(
                lobbyJoker, clientData.getClientLobby().getToken(), clientData.getClientPlayer().getName()
        ));
    }

    private void handleJokerCases(String senderName){
        switch(lobbyJoker){
            case DOUBLE_POINTS:
                doublePointsForClient();
                break;
            case ELIMINATE_ANSWERS:
                eliminateAnAnswerForClient();
                break;
            case HALF_TIME_FOR_ALL_LOBBY:
                halfTime(senderName);
                break;
        }
    }

    /**
     * Method that checks what type of controller the client is currently on and then calls doublePoints
     * which is a method all the game controllers have
     */
    private void doublePointsForClient(){
        Object curController = client.getCurrentSceneCtrl();
        if(curController instanceof GameMCQCtrl){
            ((GameMCQCtrl) curController).doublePoints();
        }else if(curController instanceof EstimationQuestionCtrl){
            ((EstimationQuestionCtrl) curController).doublePoints();
        }else if(curController instanceof EnergyAlternativeQuestionCtrl){
            ((EnergyAlternativeQuestionCtrl) curController).doublePoints();
        }
    }

    /**
     * Randomly eliminates one of the wrong answers
     * It is only applicable to the MCQ and AlternativeQuestions scenes
     */
    private void eliminateAnAnswerForClient(){
        Object curController = client.getCurrentSceneCtrl();
        if(curController instanceof GameMCQCtrl){
            ((GameMCQCtrl) curController).eliminateRandomWrongAnswer();
        }else if(curController instanceof EnergyAlternativeQuestionCtrl){
            ((EnergyAlternativeQuestionCtrl) curController).eliminateRandomWrongAnswer();
        }
    }

    /** method to be called on all resetUI scenes, to reduce code duplication
     * First and second parameter are always valid, the third one could be null
     * for the EstimationQuestion scene, in which case we just return
     */
    public void resetJokerUI(Pane  halfTimeJoker, Pane doublePointsJoker, Pane eliminateAnswerJoker)
    {
        halfTimeJoker.setDisable(clientData.getUsedJokers().contains(JokerType.HALF_TIME_FOR_ALL_LOBBY));
        doublePointsJoker.setDisable(clientData.getUsedJokers().contains(JokerType.DOUBLE_POINTS));
        if(eliminateAnswerJoker != null){
            eliminateAnswerJoker.setDisable(clientData.getUsedJokers().contains(JokerType.ELIMINATE_ANSWERS));
        }

        if(clientData.getUsedJokers().contains(JokerType.HALF_TIME_FOR_ALL_LOBBY)){
            halfTimeJoker.setStyle("-fx-background-color: gray");
            halfTimeJoker.getStyleClass().remove("image-button");
        }
        else {
            halfTimeJoker.setStyle("-fx-background-color: #ccffff");
            halfTimeJoker.getStyleClass().add("image-button");
        }
        if(clientData.getUsedJokers().contains(JokerType.DOUBLE_POINTS)){
            doublePointsJoker.setStyle("-fx-background-color: gray");
            doublePointsJoker.getStyleClass().remove("image-button");
        }
        else {
            doublePointsJoker.setStyle("-fx-background-color: #ccffff");
            doublePointsJoker.getStyleClass().add("image-button");
        }
        if(eliminateAnswerJoker != null) {
            if (clientData.getUsedJokers().contains(JokerType.ELIMINATE_ANSWERS)) {
                eliminateAnswerJoker.setStyle("-fx-background-color: gray");
                eliminateAnswerJoker.getStyleClass().remove("image-button");
            } else {
                eliminateAnswerJoker.setStyle("-fx-background-color: #ccffff");
                eliminateAnswerJoker.getStyleClass().add("image-button");
            }
        }
    }

    /**
     * Half the time remaining as requested by another player in the lobby
     */
    public void halfTime(String senderName){
        System.out.println("checking sendername");
        if(!senderName.equals(clientData.getClientPlayer().getName()))
            client.halfTime();
    }

    public void setLobbyJoker(JokerType lobbyJoker) {
        this.lobbyJoker = lobbyJoker;
    }
}
