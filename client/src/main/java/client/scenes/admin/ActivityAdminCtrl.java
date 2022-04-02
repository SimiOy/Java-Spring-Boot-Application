package client.scenes.admin;

import client.scenes.MainCtrl;
import client.utils.ServerUtils;
import commons.Activity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import javax.inject.Inject;
import java.util.List;

public class ActivityAdminCtrl {

    private final ServerUtils server;

    private final MainCtrl mainCtrl;

    private List<Activity> activities;

    @FXML
    private Text text;

    @FXML
    public ListView activityList;

    public static final ObservableList data =
            FXCollections.observableArrayList();

    @Inject
    public ActivityAdminCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
    }

    public void load() {

        activities = server.getAllActivities();

        for(Activity x : activities){
            data.add(new String(x.getActivityID() + "; " + x.getTitle()));
        }

        activityList.setItems(data);

        activityList.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                if(event.getClickCount() % 2 == 0) {
                    String clicked = activityList.getSelectionModel().getSelectedItem().toString();
                    System.out.println("clicked on " + clicked);
                    String target = new String();
                    int i = 0;
                    while (clicked.charAt(i) != ';') {
                        target = target + clicked.charAt(i);
                        i++;
                    }
                    System.out.println(target);
                    Activity act = new Activity();
                    for (Activity x : activities) {
                        if(x.getActivityID().equals(target)) {
                            act = x;
                            break;
                        }
                    }
                    mainCtrl.showActivityEdit(act);
                }
            }
        });

    }

    public void home(){
        mainCtrl.showGameModeSelection();
    }
}
