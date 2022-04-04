package client.emotes;

import javafx.scene.control.MenuItem;

import java.util.List;

public interface Emotes {

    List<MenuItem> getEmotesList();

    void sendEmote(String emote);

    void sendDisconnect();

    void sendJokerUsed();
}
