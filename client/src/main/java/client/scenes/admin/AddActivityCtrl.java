package client.scenes.admin;

import client.scenes.MainCtrl;
import client.utils.ServerUtils;
import commons.Activity;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import javax.inject.Inject;

public class AddActivityCtrl {

    private final MainCtrl mainCtrl;
    private final ServerUtils server;
    private final ActivityAdminCtrl activityAdminCtrl;

    @FXML
    private Text title;

    @FXML
    private Text id;

    @FXML
    private Text energy;

    @FXML
    private Text source;

    @FXML
    private TextField newTitle;

    @FXML
    private TextField newEnergy;

    @FXML
    private TextField newId;

    @FXML
    private TextField newSource;

    @Inject
    public AddActivityCtrl(MainCtrl mainCtrl, ServerUtils server, ActivityAdminCtrl activityAdminCtrl) {
        this.mainCtrl = mainCtrl;
        this.server = server;
        this.activityAdminCtrl = activityAdminCtrl;
    }

    public void load(){
        title.setText("Title: ");
        energy.setText("Energy Consumption in Wh: " );
        source.setText("Source: " );
        id.setText("ID: ");
    }

    public void add(){
        Activity activity = new Activity(newId.getText(), " ", newTitle.getText(), Long.parseLong(newEnergy.getText()),
                newSource.getText());
        server.addActivity(activity);
        activityAdminCtrl.data.add(activity.getActivityID() + "; " + activity.getTitle());
        mainCtrl.showAdminActivities();
    }

    public void back(){
        mainCtrl.showAdminActivities();
    }
}
