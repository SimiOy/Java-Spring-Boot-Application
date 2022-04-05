package client.scenes;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class JokerPopUpCtrl {
    @FXML
    private ImageView hourglass;

    @FXML
    private ImageView insight;

    @FXML
    private ImageView doublepts;

    public void load(){
        hourglass.setImage(new Image("images/hourglass.png"));
        insight.setImage(new Image("images/eye.png"));
        doublepts.setImage(new Image("images/double.png"));
    }
}
