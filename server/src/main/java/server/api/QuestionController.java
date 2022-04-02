package server.api;

import commons.Question;
import commons.WebsocketMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;
import server.database.QuestionRepository;
import server.gameLogic.QuestionProvider;

import java.util.HashMap;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/question")
public class QuestionController {

    @Autowired
    private QuestionProvider questionProvider;

    @Autowired
    private QuestionRepository repository;

    private HashMap<String, Integer> lobbyToDifficultyMap;

    @GetMapping("/getQuestion")
    @ResponseBody
    public Question getQuestion(@RequestParam Long pointer, @RequestParam String lastLobby) {
        return questionProvider.getQuestion(pointer, lastLobby, 30);
    }

    @MessageMapping("/setDifficulty")
    public void setDifficulty(WebsocketMessage message)
    {
        String lobbyCode = message.getLobbyToken();
        Integer difficulty = message.getDifficultySetting();

        //don't need to check whether the lobby exists,
        //this step is already done in other configs
        if(lobbyToDifficultyMap == null) lobbyToDifficultyMap = new HashMap<>();
        lobbyToDifficultyMap.put(lobbyCode,difficulty);
    }

    @GetMapping("/getAll")
    @ResponseBody
    public List<Question> getAll()
    {
        return repository.findAll();
    }

    @GetMapping("/delete/{id}")
    public String deleteQuestion(@PathVariable("id") Long id) {
        Optional<Question> question = repository.findById(id);
        if (question.isEmpty()) {
            return "Question not found";
        }

        repository.deleteById(id);
        return "Success";
    }

    @PostMapping("/add")
    @ResponseBody
    public Question addQuestion(@RequestBody Question question){
        repository.save(question);
        return question;
    }

    /**
     * Websocket mapping that sends back to client a new ResponseMessage containing a question.
     * @param message
     * @return
     */
    @MessageMapping("/nextQuestion")
    @SendTo("/topic/nextQuestion")
    public WebsocketMessage nextQuestion(WebsocketMessage message){
        int difficulty = 30;
        //if not initialised <=> not making use of private lobbies settings
        if(lobbyToDifficultyMap != null && lobbyToDifficultyMap.containsKey(message.getLobbyToken()))
            difficulty = lobbyToDifficultyMap.get(message.getLobbyToken());

        Question generatedQuestion = questionProvider.getQuestion(message.getPointer(),
                message.getLobbyToken(), difficulty);
        System.out.println("Activities generated were " + generatedQuestion.getFoundActivities());
        return new WebsocketMessage(message.getCode(),
                message.getLobbyToken(), generatedQuestion);
    }
}
