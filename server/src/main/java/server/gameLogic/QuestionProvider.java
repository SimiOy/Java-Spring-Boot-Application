package server.gameLogic;

import commons.Activity;
import commons.Question;
import constants.QuestionTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.database.ActivitiesRepository;
import server.database.QuestionRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionProvider {
    @Autowired
    private ActivitiesRepository activitiesRepository;

    @Autowired
    private QuestionRepository questionRepository;

    private long pointer;
    private long newPointer;
    private int difficulty;
    private String lastLobby;
    private Random random;

    private HashMap<String, HashSet<Question>> lobbyToQuestionsUsed;

    public QuestionProvider() {
        random = new Random();
        lobbyToQuestionsUsed = new HashMap<>();
    }

    /**
     * The important method of this class, it gets the next linked question or creates a new one
     * @return the next or newly created question
     */
    public Question getQuestion(long pointer, String lastLobby, int difficulty) {
        this.pointer = pointer;
        this.lastLobby = lastLobby;
        this.difficulty = difficulty;
        Optional<Question> foundQuestion;
        foundQuestion = questionRepository.findByIdAndLastLobbyToken(pointer, lastLobby);
        System.out.println("[LOBBY] " + lastLobby + " has used: ");
        if(lobbyToQuestionsUsed.containsKey(lastLobby)) {
            for (Question q : lobbyToQuestionsUsed.get(lastLobby)) {
                System.out.println(q.id);
            }
        }
        updatePointer();

        if (foundQuestion.isEmpty()) {
            return createNewQuestion();
        } else {
            return getUnusedQuestionOrNewQuestion(foundQuestion);
        }
    }

    /**
     * Method that scans through the linked structure of questions until either a new question is created or a
     * question which has not yet been used is found
     * @param foundQuestion
     * @return
     */
    public Question getUnusedQuestionOrNewQuestion(Optional<Question> foundQuestion){
        Question nextQuestion = useQuestionAfter(foundQuestion.get());
        if(nextQuestion.equals(foundQuestion.get()) || questionAlreadyUsed(nextQuestion)){
            nextQuestion = createNewQuestion();
            System.out.println("------------------");
            System.out.println("DUPLICATE FOUND");
            System.out.println("[NEW QUESTION CREATED]");
            System.out.println("------------------");
        }

        return nextQuestion;
    }

    /**
     * Check using the lobbyToQuestionsUsed hash map whether the next question has already been used in the lobby
     */
    public boolean questionAlreadyUsed(Question question){
        HashSet<Question> questionsUsed = lobbyToQuestionsUsed.get(lastLobby);
        if(questionsUsed != null){
            return questionsUsed.contains(question);
        }
        return false;
    }

    /**
     * Update the set containing all questions used in a given lobby
     */
    public void updateSetOfQuestions(Question question){
        HashSet<Question> set = lobbyToQuestionsUsed.get(lastLobby);
        if(set == null){
            set = new HashSet<>();
        }
        set.add(question);
        System.out.println("Attempted to update questions....");
        lobbyToQuestionsUsed.put(lastLobby, set);
    }


    public void updatePointer() {
        long questionPool = questionRepository.count();
        double probabilityOfReusingExisingQuestion = clamp(1 + (49729.0 / (questionPool + 1)),0,100);
        Random random = new Random();
        if(random.nextDouble() * 100 <= probabilityOfReusingExisingQuestion)
        {
            //pointer points to new question
            newPointer = questionPool + 1;
            System.out.println("Generating new question, with probability: " + probabilityOfReusingExisingQuestion);
        }
        else
        {
            //reuse a question
            newPointer = random.nextInt((int) questionPool);
        }

        System.out.println("[OLD POINTER] " + pointer + ", " + "[NEW POINTER]" + newPointer);
        if (pointer == newPointer) newPointer++;
    }

    public QuestionTypes getRandomQuestionType() {
        switch (2/*Math.abs(random.nextInt(4))*/) {
            case 0:
                return QuestionTypes.MULTIPLE_CHOICE_QUESTION;
            case 1:
                return QuestionTypes.ESTIMATION_QUESTION;
            case 2:
                return QuestionTypes.ENERGY_ALTERNATIVE_QUESTION;
            case 3:
                return QuestionTypes.GUESS_X;
            default:
                return null;
        }
    }

    public Question createNewQuestion() {
        System.out.println("New question created");
        QuestionTypes questionType = getRandomQuestionType();
        List<Long> activitiesIDS;
        Activity activityPivot = getActivityPivot();

        switch (questionType) {
            case MULTIPLE_CHOICE_QUESTION:
                activitiesIDS = getMultipleChoiceQuestionActivities(activityPivot);
                break;
            case ESTIMATION_QUESTION:
                activitiesIDS = getSingleActivity(activityPivot);
                break;
            case ENERGY_ALTERNATIVE_QUESTION:
                activitiesIDS = getAlternativeEnergyQuestionActivities(activityPivot);
                break;
            case GUESS_X:
                activitiesIDS = getMultipleChoiceQuestionActivities(activityPivot);
                break;
            default:
                activitiesIDS = null;
        }

        Set<Long> activitySet = new HashSet<>(activitiesIDS);
        Question question = new Question(pointer, questionType, newPointer, activitySet, lastLobby);
        updateSetOfQuestions(question);
        questionRepository.save(question);
        System.out.println(question);
        return question;
    }


    public Question useQuestionAfter(Question q) {
        if (!Objects.equals(q.getLastLobbyToken(), lastLobby)) {
            q.setPointer(newPointer);
            q.setLastLobbyToken(lastLobby);
            System.out.println("A question has been reused");
            questionRepository.save(q);
        }
        return q;
    }

    public Activity getActivityPivot() {
        List<Activity> activities = activitiesRepository.findAll();
        return activities.get(random.nextInt(activities.size()));
    }

    public List<Long> getMultipleChoiceQuestionActivities(Activity activityPivot) {
        List<Long> activitiesIDs = new ArrayList<>();
        Activity activityLeft = (activitiesRepository.findByEnergyConsumptionDesc(
                activityPivot.getEnergyConsumption() * (100 - difficulty) / 100)).get(0);
        Activity activityRight = (activitiesRepository.findByEnergyConsumptionDesc(
                activityPivot.getEnergyConsumption() * (100 + difficulty) / 100)).get(0);

        activitiesIDs.add(activityLeft.getId());
        activitiesIDs.add(activityPivot.getId());
        activitiesIDs.add(activityRight.getId());
        return activitiesIDs;
    }

    public List<Long> getAlternativeEnergyQuestionActivities(Activity activityPivot) {
        long small = activityPivot.getEnergyConsumption() * (100 - difficulty) / 100;
        long big = activityPivot.getEnergyConsumption() * (100 + difficulty) / 100;

        Activity correctAlternative = getCorrectAlternative(activityPivot, small, big);
        List<Activity> wrongAlternatives = getWrongAlternatives(activityPivot, small, big);
        List<Long> activities = new ArrayList<>();

        // Pivot/'instead of' question goes first
        activities.add(activityPivot.getId());

        // Correct alternative goes next
        activities.add(correctAlternative.getId());

        //Wrong alternatives go last
        activities.addAll(wrongAlternatives.stream().map(Activity::getId).collect(Collectors.toList()));
        return activities;
    }

    /**
     * Loop through alternative candidate activities until one that is not the pivot is found (chances are very small
     * it will have to loop through but it could have been a weird bug)
     * If by some weird reason there is no activity in the range of [small, big] then we recursivelly look in a
     * biggger range
     */
    private Activity getCorrectAlternative(Activity activityPivot, long small, long big){
        List<Activity> alternatives = activitiesRepository.findActivitiesInRange(small, big).get();
        for(Activity activity : alternatives){
            if(!activity.equals(activityPivot)){
                return activity;
            }
        }
        return getCorrectAlternative(activityPivot, small / 2, big * 2);
    }

    /**
     * Gets two wrong alternative activities. It randomly selects two activities out of the [small, big] range.
     * It could be one activity with lower energy consumption that the correct answer and one higher
     */
    private List<Activity> getWrongAlternatives(Activity activityPivot, long small, long big){
        Random random = new Random();
        int numActivitiesLower = random.nextInt(3);
        int numActivitiesHigher = 2 - numActivitiesLower;
        List<Activity> wrongActivities = new ArrayList<>();

        // Get wrong, lower consumptions, alternative(s)
        List<Activity> lowerWrongAlternatives =activitiesRepository.findActivitiesInRange(small / 100, small / 2).get();
        for(int i=0; i<numActivitiesLower; i++){
            wrongActivities.add(lowerWrongAlternatives.get(i));
        }

        // Get wrong, higher consumption, alternative(s)
        List<Activity> higherWrongAlternatives = activitiesRepository.findActivitiesInRange(big * 2, small * 100).get();
        for(int i=0; i<numActivitiesHigher; i++){
            wrongActivities.add(higherWrongAlternatives.get(i));
        }
        return wrongActivities;
    }

    public List<Long> getSingleActivity(Activity activityPivot) {
        List<Long> activities = new ArrayList<>();
        activities.add(activityPivot.getId());
        return activities;
    }

    /**
     * Clears all questions which have been assined to a certain lobby
     * @param lobbyId
     */
    public void clearAllQuestionsFromLobby(String lobbyId){
        lobbyToQuestionsUsed.remove(lobbyId);
        System.out.println("DELETED ALL QUESTIONS OF LOBBY " + lobbyId);
    }

    /**
     * Method that clamps a value between two other values
     * @param val - value to clamp
     * @param min - minimum value
     * @param max - maximum value
     * @return
     */
    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

}
