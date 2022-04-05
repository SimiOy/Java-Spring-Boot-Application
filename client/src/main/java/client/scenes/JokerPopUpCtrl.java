package client.scenes;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import javax.inject.Inject;

public class JokerPopUpCtrl {
    @FXML
    private ImageView hourglass;

    @FXML
    private ImageView insight;

    @FXML
    private ImageView doublepts;

    @FXML
    private Pane bigPane;

    private MainCtrl mainCtrl;

    @Inject
    public JokerPopUpCtrl(MainCtrl mainCtrl){
        this.mainCtrl = mainCtrl;
    }

    public void load(){
        hourglass.setImage(new Image("images/hourglass.png"));
        insight.setImage(new Image("images/eye.png"));
        doublepts.setImage(new Image("images/double.png"));
    }

    public void close(){
        mainCtrl.closeJokerInfo();
    }
}
