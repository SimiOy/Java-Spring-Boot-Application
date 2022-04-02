package client.scenes.admin;

import client.scenes.MainCtrl;
import client.utils.ServerUtils;
import commons.Question;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import javax.inject.Inject;
import java.util.List;

public class QuestionAdminCtrl {

    private final ServerUtils server;

    private final MainCtrl mainCtrl;

    public List<Question> questions;

    @FXML
    private Text text;

    @FXML
    public ListView questionList;

    public static final ObservableList data =
            FXCollections.observableArrayList();

    @Inject
    public QuestionAdminCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
    }

    public void load() {

        questions = server.getAllQuestions();

        for(Question x : questions){
            data.add(new String(x.getId() + "; " + x.getType() + "; " + x.getText()));
        }

        questionList.setItems(data);

        questionList.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                if(event.getClickCount() % 2 == 0) {
                    String clicked = questionList.getSelectionModel().getSelectedItem().toString();
                    System.out.println("clicked on " + clicked);
                    String target = new String();
                    int i = 0;
                    while (clicked.charAt(i) != ';') {
                        target = target + clicked.charAt(i);
                        i++;
                    }
                    Long targetLong = Long.parseLong(target);
                    System.out.println(target);
                    Question question = new Question();
                    for (Question x : questions) {
                        if(x.getId().equals(targetLong)) {
                            question = x;
                            break;
                        }
                    }
                    mainCtrl.showQuestionsEdit(question);
                }
            }
        });

    }

    public void home(){
        mainCtrl.showGameModeSelection();
    }
}
