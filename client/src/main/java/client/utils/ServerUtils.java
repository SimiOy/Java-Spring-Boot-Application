/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.utils;

import commons.*;
import constants.ConnectionStatusCodes;
import exceptions.InvalidServerException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.UriBuilder;
import javafx.scene.image.Image;
import org.glassfish.jersey.client.ClientConfig;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

public class ServerUtils {
    private String host;
    private int port;
    private String SERVER;
    private StompSession session;

    /**
     * Setter for host, port and initiator of SERVER
     * @param host
     * @param port
     */
    public void setHostAndPort(String host, int port) throws InvalidServerException{
        this.host = host;
        this.port = port;
        SERVER = UriBuilder.newInstance()
                .scheme("http")
                .host(host)
                .port(port)
                .build()
                .toString();
        String sessionString = UriBuilder.newInstance()
                .scheme("ws")
                .host(host)
                .port(port)
                .path("/websocket")
                .build()
                .toString();
        if(port < 1024){
            throw new InvalidServerException("Port cannot be accessed. Make sure the port is above 1023!");
        }
        session = connect(sessionString);
    }

    public List<Player> getPlayers() {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("/api/player/getAllPlayers") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<List<Player>>() {});
    }


    public Player addPlayer(Player player) {
        System.out.println("\n\n\n" + SERVER + "\n\n\n");
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("/api/player/addPlayer") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .post(Entity.entity(player, APPLICATION_JSON), Player.class);
    }

    public Lobby addLobby(Lobby lobby) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("/api/lobby/addLobby") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .post(Entity.entity(lobby, APPLICATION_JSON), Lobby.class);
    }

    public Activity addActivity(Activity activity1) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("/api/activity/add") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .post(Entity.entity(activity1, APPLICATION_JSON), Activity.class);
    }

    public Question addQuestion(Question question) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("/api/question/add") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .post(Entity.entity(question, APPLICATION_JSON), Question.class);
    }

    public List<Lobby> getAllLobbies() {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("/api/lobby/getAllLobbies") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<List<Lobby>>() {});
    }

    public Lobby getLobbyByToken(String token){
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("/api/lobby/getLobbyByToken")
                .queryParam("token", token)//
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<Lobby>() {});
    }

    public ConnectionStatusCodes getConnectPermission(String token, String playerUsername){
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("/api/lobby/getConnectPermission") //
                .queryParam("token", token)//
                .queryParam("playerUsername", playerUsername)
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<ConnectionStatusCodes>(){});
    }

    public Activity getRandomActivity() {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("/api/activity/getRandomActivity")
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<Activity>() {});
    }

    public List<LeaderboardEntry> getTop10Scores() {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("/api/leaderboard/getTop10")
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<List<LeaderboardEntry>>() {});
    }

    public LeaderboardEntry persistScore(LeaderboardEntry score) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("/api/leaderboard/persistScore")
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .post(Entity.entity(score, APPLICATION_JSON), LeaderboardEntry.class);
    }

    public Question getQuestion(long pointer, String lastLobby){
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("/api/question/getQuestion") //
                .queryParam("pointer", pointer)//
                .queryParam("lastLobby", lastLobby)//
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<Question>(){}); 
    }

    public List<Activity> getAllActivities() {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("/api/activity/getAll")
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<List<Activity>>() {});
    }

    public String deleteActivity(String activityID) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("/api/activity/delete/" + activityID) //
                //.queryParam("activityID", activityID)
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<String>() {});
    }

    public List<Question> getAllQuestions() {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("/api/question/getAll")
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<List<Question>>() {});
    }

    public String deleteQuestion(Long id) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("/api/question/delete/" + id) //
                //.queryParam("activityID", activityID)
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<String>() {});
    }

    public ConnectionStatusCodes startLobby(String token) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("/api/lobby/startLobby") //
                .queryParam("token", token)//
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<ConnectionStatusCodes>(){});
    }

    public List<Player> getTopByLobbyToken(String token) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("/api/lobby/getTopByLobbyToken") //
                .queryParam("token", token)//
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<List<Player>>() {});
    }

    public Player updateScore(Player player) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("/api/player/updateScore") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .put(Entity.entity(player, APPLICATION_JSON), Player.class);
    }

    public List<Activity> getActivitiesFromIDs(List<Long> listIds) {
        List<Activity> result = new ArrayList<>();
        for(Long l : listIds)
        {
            Activity activity = getActivityByID(l).get();
            result.add(activity);
        }
        return result;
    }

    public Optional<Activity> getActivityByID(Long id)
    {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("/api/activity/getActivityByID") //
                .queryParam("id", id)//
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<>() {
                });
    }

    public Image getImageFromActivityId(Long id)
    {
        Activity act = getActivityByID(id).get();
        String imagePath = act.getImagePath();
        URI uri = UriBuilder.newInstance()
                .scheme("http")
                .host(host)
                .port(port)
                .path("/api/images/getImageByActivityId")
                .queryParam("path", imagePath)
                .build();
        Image image = new Image(uri.toString());
        return image;
    }

    public Image getImageFromActivity(Activity activity)
    {
        String imagePath = activity.getImagePath();
        URI uri = UriBuilder.newInstance()
                .scheme("http")
                .host(host)
                .port(port)
                .path("/api/images/getImageByActivityId")
                .queryParam("path", imagePath)
                .build();
        Image image = new Image(uri.toString());
        return image;
    }


    private StompSession connect(String url) throws InvalidServerException {
        StandardWebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stomp = new WebSocketStompClient(client);

        stomp.setMessageConverter(new MappingJackson2MessageConverter());

        try{
            return stomp.connect(url, new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    System.out.println("connected");
                }
            }).get();
        }catch (ExecutionException e) {
            Thread.currentThread().interrupt();
            System.out.println("not working");
        } catch (InterruptedException e) {
            System.out.println("not working");
            throw new RuntimeException(e);
        } throw new InvalidServerException("Cannot find server");
    }


    public StompSession.Subscription registerForMessages(String dest, Consumer<WebsocketMessage> consumer){
        System.out.println("registered for " + dest);
        StompSession.Subscription subscription = session.subscribe(dest, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return WebsocketMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println("Message " + ((WebsocketMessage) payload).getCode() +
                        " for lobby " + ((WebsocketMessage) payload).getLobbyToken());
                consumer.accept((WebsocketMessage) payload);
            }
        });
        return subscription;
    }


    public Lobby addMeToLobby(String token, Player player){
        ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("/api/lobby/addMeToLobby") //
                .queryParam("token", token)//
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON)
                .post(Entity.entity(player, APPLICATION_JSON), Player.class);
        return getLobbyByToken(token);
    }

    public void send(String dest, Object o){
        System.out.println("Sending " + ((WebsocketMessage) o).getCode() + " to " + dest);
        session.send(dest, o);
    }
}