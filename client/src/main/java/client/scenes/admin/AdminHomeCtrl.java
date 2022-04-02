package client.scenes.admin;

import client.scenes.MainCtrl;

import javax.inject.Inject;

public class AdminHomeCtrl {

    private final MainCtrl mainCtrl;

    @Inject
    public AdminHomeCtrl(MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
    }

    public void questions(){
        mainCtrl.showAdminQuestions();
    }

    public void activities(){
        mainCtrl.showAdminActivities();
    }
}
