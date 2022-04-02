package client.scenes.admin;

import client.scenes.MainCtrl;
import client.utils.ServerUtils;
import commons.Question;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import javax.inject.Inject;

public class EditQuestionsCtrl {

    private final MainCtrl mainCtrl;
    private final ServerUtils server;
    private final QuestionAdminCtrl questionAdminCtrl;
    private Question question;

    @FXML
    private Text title;

    @FXML
    private Text id;

    @FXML
    private Text questionType;

    @FXML
    private TextField newTitle;


    @Inject
    public EditQuestionsCtrl(MainCtrl mainCtrl, ServerUtils server, QuestionAdminCtrl questionAdminCtrl) {
        this.questionAdminCtrl = questionAdminCtrl;
        this.mainCtrl = mainCtrl;
        this.server = server;
    }

    public void load(Question q) {
        question = q;
        title.setText("Question: " + q.getText());
        questionType.setText("Type: " + q.getType());
        id.setText("ID: " + q.getId());
    }

    public void delete(){
        server.deleteQuestion(question.getId());
        questionAdminCtrl.data.remove(question.getId() + "; " + question.getType() + "; " + question.getText());
        mainCtrl.showAdminQuestions();
    }

   /* This method should be implemented if you can get the questionList
    public void edit(){
        Question question1 = new Question(question.getId() , question.getType(),
                newTitle.getText(), Long.parseLong(newEnergy.getText()), activity.getSource());
        server.deleteActivity(activity.getActivityID());
        activityAdminCtrl.data.remove(activity.getActivityID() + "; " + activity.getTitle());
        server.addActivity(activity1);
        activityAdminCtrl.data.add(activity1.getActivityID() + "; " + activity1.getTitle());
        mainCtrl.showAdminActivities();
    }*/

    public void back(){
        mainCtrl.showAdminQuestions();
    }
}
