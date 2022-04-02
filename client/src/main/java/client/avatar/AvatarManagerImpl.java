package client.avatar;

import client.data.ClientData;
import client.scenes.MainCtrl;
import client.utils.ServerUtils;
import com.talanlabs.avatargenerator.Avatar;
import com.talanlabs.avatargenerator.eightbit.EightBitAvatar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AvatarManagerImpl implements AvatarManager {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final ClientData clientData;

    private final String baseAvatarName = "OOPPP";
    private int seed = 0;
    private Path avatarPath = null;
    private Avatar playerAvatar;

    private TextField name;
    private ImageView avatarImage;

    @Inject
    public AvatarManagerImpl(ServerUtils server, MainCtrl mainCtrl, ClientData clientData) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.clientData = clientData;
    }

    /**
     * Acts like a constructor, sets the TExtField and the avatarImage of this class
     * @param name TextField
     * @param avatarImage ImageView
     */
    public void setNameAndAvatarImage(TextField name, ImageView avatarImage){
        this.name = name;
        this.avatarImage = avatarImage;
    }

    /**
     * Method that updates the avatar based on a given inputName, from which it will construct the seed
     * If the avatarPath is null(first pass), it will assign it a new path, and
     * then only change the given image in that path
     */
    public void updateAvatar()
    {
        String inputName = baseAvatarName + seed + name.getText();
        avatarPath = AvatarSupplier.generateAvatar(playerAvatar, inputName, avatarPath);
    }

    public void initAvatar()
    {
        //clear past avatars, only if the client is restarted, otherwise use already generated files
        if(clientData.getClientPlayer() == null) AvatarSupplier.clearAllAvatars();
        playerAvatar = EightBitAvatar.newMaleAvatarBuilder().build();
    }

    public void updateImage()
    {
        try{
            avatarImage.setImage(new Image(Files.newInputStream(avatarPath)));
        }catch (IOException | NullPointerException e)
        {
            System.out.println("Failed to set image");
        }

    }

    public void renameFile()
    {
        String inputName = baseAvatarName + seed + name.getText();
        avatarPath = AvatarSupplier.renameAvatarFile(avatarPath,inputName);
    }

    public void incrementSeed()
    {
        seed ++;
        updateAvatar();
        updateImage();
    }

    public void decrementSeed()
    {
        seed --;
        updateAvatar();
        updateImage();
    }

    public void setAvatarOnClient(){
        renameFile();
        clientData.getClientPlayer().setAvatar(avatarPath.toString());
        clientData.getClientPlayer().setAvatarCode(baseAvatarName + seed + name.getText());
    }

    public void setAvatarImage()
    {
        if(clientData.getClientPlayer() == null ||
                clientData.getClientPlayer().getAvatar() == null ||
                clientData.getClientPlayer().getAvatar().equals("")
        ){
            //first time generating the resource
            updateAvatar();
            updateImage();
            System.out.println("Avatar image successfully generated at: " + avatarPath);
        }
        else
        {
            //reuse the same file
            avatarPath = Paths.get(clientData.getClientPlayer().getAvatar());
            updateImage();
        }


        //set listener to look for changes
        name.textProperty().addListener(((observable, oldValue, newValue) -> {
            avatarPath = AvatarSupplier.generateAvatar(playerAvatar,newValue,avatarPath);
            updateImage();
        }));
    }
}
