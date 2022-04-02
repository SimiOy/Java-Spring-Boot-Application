package server.api;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import server.database.ActivitiesRepository;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import commons.Activity;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    @Autowired
    private ActivitiesRepository repository;

    public ActivityController(ActivitiesRepository repository){
        this.repository = repository;
    }

    public ActivityController(){

    }

    @GetMapping("/getAll")
    @ResponseBody
    public List<Activity> getAll()
    {
        return repository.findAll();
    }

    @GetMapping("/delete/{activityID}")
    public String deleteActivity(@PathVariable("activityID") String activityId) {
        Optional<Activity> activity = repository.findByActivityID(activityId);
        if (activity.isEmpty()) {
            return "Activity not found";
        }

        repository.deleteActivityByActivityID(activityId);
        return "Success";
    }

    @PostMapping("/add")
    @ResponseBody
    public Activity addActivity(@RequestBody Activity activity){
        repository.save(activity);
        return activity;
    }

    /**
     * given the energy consumption of an activity, find that activity
     * @param energyConsumption the energy consumption of an activity in Wh
     * @return the activity that has the energy consumption of the given parameter
     */
    @GetMapping("/getActivityByWh")
    @ResponseBody
    public Activity getActivityByWh(@RequestParam Long energyConsumption) {
        List<Activity> act = repository.findByEnergyConsumptionDesc(energyConsumption);
        if(act.isEmpty())
            return null;
        return act.get(0);
    }

    /**
     * get a random activity from the repo
     * @return the activity found in random
     */
    @GetMapping("/getRandomActivity")
    @ResponseBody
    public Activity getRandomActivity() {
        List<Activity> allAct = repository.findAll();
        Random ran = new Random();
        if(allAct.isEmpty())
            return null;
        return allAct.get(ran.nextInt(allAct.size()));
    }

    @GetMapping("/getActivityByID")
    @ResponseBody
    public Optional<Activity> getActivityByID(Long id) {
        return repository.findById(id);
    }
}
