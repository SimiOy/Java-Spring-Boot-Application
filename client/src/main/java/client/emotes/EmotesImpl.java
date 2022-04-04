package client.emotes;

import client.data.ClientData;
import client.utils.ServerUtils;
import commons.WebsocketMessage;
import javafx.scene.control.MenuItem;

import javax.inject.Inject;
import java.util.List;

public class EmotesImpl implements Emotes {

    private ServerUtils server;
    private ClientData clientData;

    private final List<MenuItem> menuItems = List.of(
            new MenuItem(new String(Character.toChars(0x1F35D)), null),
            new MenuItem(new String(Character.toChars(0x1F480)), null),
            new MenuItem(new String(Character.toChars(0x1F44B)), null),
            new MenuItem(new String(Character.toChars(0x1F6C0)), null)
    );

    @Inject
    public EmotesImpl(ServerUtils server, ClientData clientData) {
        this.server = server;
        this.clientData = clientData;
    }

    /**
     * @return returns this class' menuItems list which is basically the list of emotes that are usable
     */
    public List<MenuItem> getEmotesList() {
        return menuItems;
    }

    /**
     * Takes a string and sends this string to the /app/updateMessages endpoint, from where it is sent to the
     * updateMessages method in clientUtils.
     * @param emote the emote to be send to other players
     * !note that this can also be used to send other things through the communication labels!
     */
    public void sendEmote(String emote) {
        server.send("/app/updateMessages", new WebsocketMessage(
                clientData.getClientPlayer().getName() + ": " + emote,
                clientData.getClientLobby().getToken()));
    }

    public void sendDisconnect() {
        server.send("/app/updateMessages", new WebsocketMessage(
                clientData.getClientPlayer().getName() + " left the game",
                clientData.getClientLobby().getToken()));
    }

    public void sendJokerUsed() {
        server.send("/app/updateMessages", new WebsocketMessage(
                clientData.getClientPlayer().getName() + " halved the time!",
                clientData.getClientLobby().getToken()));
    }
}
