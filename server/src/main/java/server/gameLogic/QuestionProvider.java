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
    private HashMap<String, HashSet<Activity>> lobbyToActivitiesUsed;

    public QuestionProvider() {
        random = new Random();
        lobbyToQuestionsUsed = new HashMap<>();
        lobbyToActivitiesUsed = new HashMap<>();
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
                System.out.print(q.id + " ");
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

    /**
     * Check using the lobbyToActivitiesUsed hash map whether the next activity has already been used in the lobby
     */
    public boolean activityAlreadyUsed(Activity activity){
        HashSet<Activity> activitiesUsed = lobbyToActivitiesUsed.get(lastLobby);
        if(activitiesUsed != null){
            return activitiesUsed.contains(activity);
        }
        return false;
    }

    /**
     * Update the set containing all activities used in a given lobby
     */
    public void updateSetOfActivities(Activity activity){
        HashSet<Activity> set = lobbyToActivitiesUsed.get(lastLobby);
        if(set == null){
            set = new HashSet<>();
        }
        set.add(activity);
        System.out.println("Attempted to update activities....");
        lobbyToActivitiesUsed.put(lastLobby, set);
    }


    /**
     * Updates the pointer based on some probability
     * Currently the "threshold" on the server for the number of questions is \
     * 45000, when the chance of generating a new question gets close to 1%
     * at 5000 questions, the chance of generating a new question is 10%
     * The pointer is used to indicate the next question (similar to a linked list)
     */
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
        switch (Math.abs(random.nextInt(4))) {
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
                activitiesIDS = getGuessXQuestionActivities(activityPivot);
                break;
            default:
                activitiesIDS = null;
        }
        Set<Long> activitySet = new LinkedHashSet<>(activitiesIDS);
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
        Activity act = activities.get(random.nextInt(activities.size()));
        if(activityAlreadyUsed(act)) return getActivityPivot();
        else {
            //add the used pivot activity
            updateSetOfActivities(act);
            return act;
        }
    }

    /**
     * Method that uses an activity pivot, and a range determined by it
     * and the chosen difficulty to find 3 activities, 1 larger than the pivot
     * and 1 smaller. The arraylist is returned in decreasing order, and
     * the shuffling is done client-sided
     * @param activityPivot - the pivot around the other two activities are chosen
     * @return arrayList of activities IDS
     */
    public List<Long> getMultipleChoiceQuestionActivities(Activity activityPivot) {
        List<Long> activitiesIDs = new ArrayList<>();
        Activity activityLeft, activityRight;
        int i = 0;
        do {
             activityLeft = (activitiesRepository.findByEnergyConsumptionDesc(
                    activityPivot.getEnergyConsumption() * (100 - difficulty) / 100)).get(i);
             i++;
        }while (activityAlreadyUsed(activityLeft));
        updateSetOfActivities(activityLeft);

        i = 0;
        do {
            activityRight = (activitiesRepository.findByEnergyConsumptionDesc(
                    activityPivot.getEnergyConsumption() * (100 + difficulty) / 100)).get(i);
            i++;
        }while (activityAlreadyUsed(activityRight));
        updateSetOfActivities(activityRight);

        activitiesIDs.add(activityRight.getId());
        activitiesIDs.add(activityPivot.getId());
        activitiesIDs.add(activityLeft.getId());

        return activitiesIDs;
    }

    /**
     * Method that makes use of a pivot, and a chance of looking for
     * a bigger or smaller activity (by WH), to create an arrayList of activities IDS
     * @param activityPivot - the pivot around the other two activities are chosen
     * @return arrayList of activities IDS
     */
    public List<Long> getGuessXQuestionActivities(Activity activityPivot) {
        List<Long> activitiesIDs = new ArrayList<>();
        activitiesIDs.add(activityPivot.getId());

        int j = 0;
        for(int i = 0; i < 2; i++)
        {
            Activity activity;
            if(new Random().nextBoolean()) {
                do {
                    activity = (activitiesRepository.findByEnergyConsumptionDesc(
                            activityPivot.getEnergyConsumption() * (100 - difficulty) / 100)).get(j);
                    j++;
                }while (activityAlreadyUsed(activity));
                updateSetOfActivities(activity);
            }
            else
            {
                j = 0;
                do {
                    activity = (activitiesRepository.findByEnergyConsumptionDesc(
                            activityPivot.getEnergyConsumption() * (100 + difficulty) / 100)).get(j);
                    j++;
                }while (activityAlreadyUsed(activity));
                updateSetOfActivities(activity);
            }
            activitiesIDs.add(activity.getId());
        }

        return activitiesIDs;
    }

    /**
     * Method that uses an activity pivot, and a range determined by it
     * and the chosen difficulty to poll an approximate (correct answer)
     * activity, and 2 others that are wrong
     * @param activityPivot - the pivot around the other two activities are chosen
     * @return arrayList of activities IDS
     */
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
     * it will have to loop through, but it could have been a weird bug)
     * If by some weird reason there is no activity in the range of [small, big] then we recursively look in a
     * bigger range
     */
    private Activity getCorrectAlternative(Activity activityPivot, long small, long big){
        List<Activity> alternatives = activitiesRepository.findActivitiesInRange(small, big).get();
        for(Activity activity : alternatives){
            if(!activity.equals(activityPivot)){
                if(activityAlreadyUsed(activity)) return getCorrectAlternative(activityPivot, small / 2, big * 2);
                else {
                    updateSetOfActivities(activity);
                    return activity;
                }
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
        List<Activity> lowerWrongAlternatives = activitiesRepository.
                                                findActivitiesInRange(small / 100, small / 2).get();
        for(int i=0; i<numActivitiesLower; i++){
            if(activityAlreadyUsed(lowerWrongAlternatives.get(i)))
            {
                //skip one
                numActivitiesLower ++;
            }
            else {
                wrongActivities.add(lowerWrongAlternatives.get(i));
                updateSetOfActivities(lowerWrongAlternatives.get(i));
            }
        }

        // Get wrong, higher consumption, alternative(s)
        List<Activity> higherWrongAlternatives = activitiesRepository.
                                                findActivitiesInRange(big * 2, small * 100).get();
        for(int i=0; i<numActivitiesHigher; i++){
            if(activityAlreadyUsed(higherWrongAlternatives.get(i)))
            {
                //skip one
                numActivitiesHigher ++;
            }
            else {
                wrongActivities.add(higherWrongAlternatives.get(i));
                updateSetOfActivities(higherWrongAlternatives.get(i));
            }
        }
        return wrongActivities;
    }

    public List<Long> getSingleActivity(Activity activityPivot) {
        List<Long> activities = new ArrayList<>();
        activities.add(activityPivot.getId());
        return activities;
    }

    /**
     * Clears all questions which have been assigned to a certain lobby
     * @param lobbyId
     */
    public void clearAllQuestionsFromLobby(String lobbyId){
        lobbyToQuestionsUsed.remove(lobbyId);
        System.out.println("DELETED ALL QUESTIONS OF LOBBY " + lobbyId);
    }

    /**
     * Clears all activities which have been assigned to a certain lobby
     * @param lobbyId
     */
    public void clearAllActivitiesFromLobby(String lobbyId){
        lobbyToActivitiesUsed.remove(lobbyId);
        System.out.println("DELETED ALL ACTIVITIES OF LOBBY " + lobbyId);
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
