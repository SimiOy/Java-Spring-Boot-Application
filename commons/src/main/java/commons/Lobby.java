package commons;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

@Entity
@Table(name = "Lobby")
public class Lobby {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;

    @Column(name = "token")
    public String token;

    @Column(name = "isPublic")
    public Boolean isPublic;

    @Column(name = "isStarted")
    public Boolean isStarted;

    @Column(name = "hostId")
    public Integer hostId;

    @Column(name = "listOfPlayers", length = 10000)
    public ArrayList<Long> playerIds;

    //might delete later
    //saves computation power when translating from ids to Players;
    //its even safer if we keep it can't hurt as if we don't store it in database
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    public List<Player> playersInLobby;

    public Lobby() {
        // for object mapper
    }

    /**
     * Instantiates the public lobby with empty list of players
     * hostId remains null, as there is no host of a public lobby
     * @param token
     */
    public Lobby(String token) {
        this.token = token;
        this.isPublic = true;
        this.playerIds = new ArrayList<>();
        this.playersInLobby = new ArrayList<>();
        this.hostId = null;
        this.isStarted = false;
    }

    /**
     * Instantiates a private lobby given a token, and a host id
     * @param token
     * @param hostId
     */
    public Lobby(String token, Integer hostId)
    {
        this.token = token;
        this.hostId = hostId;
        this.playerIds = new ArrayList<>();
        this.playersInLobby = new ArrayList<>();
        this.isPublic = false;
        this.isStarted = false;
    }

    public void addPlayerToLobby(Player player)
    {
        playerIds.add(player.getId());
        playersInLobby.add(player);
    }


    public void removePlayerFromLobby(Player player)
    {
        int toRemove = -1;
        playerIds.remove(player.getId());
        for(int i=0; i< playersInLobby.size(); ++i){
            if(playersInLobby.get(i).getId() == player.getId())
                toRemove = i;
        }
        if(toRemove != -1) {
            playersInLobby.remove(toRemove);
            System.out.println(player.getName() + " left the lobby: " + this.getToken());
        }
        //playersInLobby.remove(player);
    }

    public List<Player> getPlayersInLobby()
    {
        return playersInLobby;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getHostId() {
        return hostId;
    }

    public void setHostId(Integer hostId) {
        this.hostId = hostId;
    }

    public ArrayList<Long> getPlayerIds() {
        return playerIds;
    }

    public void setPlayerIds(ArrayList<Long> playerIds) {
        this.playerIds = playerIds;
    }

    public Boolean getIsStarted() {
        return isStarted;
    }

    public void setIsStarted(Boolean started) {
        isStarted = started;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, MULTI_LINE_STYLE);
    }
}
