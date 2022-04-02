package client.scenes.admin;

import client.scenes.MainCtrl;
import client.utils.ServerUtils;
import commons.Activity;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import javax.inject.Inject;

public class EditActivitiesCtrl {

    private final MainCtrl mainCtrl;
    private final ServerUtils server;
    private final ActivityAdminCtrl activityAdminCtrl;
    private Activity activity;

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

    @Inject
    public EditActivitiesCtrl(MainCtrl mainCtrl, ServerUtils server, ActivityAdminCtrl activityAdminCtrl) {
        this.activityAdminCtrl = activityAdminCtrl;
        this.mainCtrl = mainCtrl;
        this.server = server;
    }

    public void load(Activity act) {
        activity = act;
        title.setText("Title: " + act.getTitle());
        energy.setText("Energy Consumption in Wh: " + act.getEnergyConsumption().toString());
        source.setText("Source: " + act.getSource());
        id.setText("ID: " + act.getActivityID());
    }

    public void delete(){
        server.deleteActivity(activity.getActivityID());
        activityAdminCtrl.data.remove(activity.getActivityID() + "; " + activity.getTitle());
        mainCtrl.showAdminActivities();
    }

    public void edit(){
        Activity activity1 = new Activity(activity.getActivityID() + "New", activity.getImagePath(),
                newTitle.getText(), Long.parseLong(newEnergy.getText()), activity.getSource());
        server.deleteActivity(activity.getActivityID());
        activityAdminCtrl.data.remove(activity.getActivityID() + "; " + activity.getTitle());
        server.addActivity(activity1);
        activityAdminCtrl.data.add(activity1.getActivityID() + "; " + activity1.getTitle());
        mainCtrl.showAdminActivities();
    }

    public void back(){
        mainCtrl.showAdminActivities();
    }
}
