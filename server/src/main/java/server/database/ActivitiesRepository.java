package server.database;

import commons.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Repository of Activities can be created, contains all the methods from crud repository.
 * useful methods: save, findAll, delete().
 */
@Transactional
public interface ActivitiesRepository extends JpaRepository<Activity, Long> {

    /**
     * Given an energy consumption in wh
     * Order the activities in the repo that has smaller energy consumptions than the parameter
     * according to the energy consumption descending
     * @param energyConsumption
     * @return a list of activities
     */
    @Query("select a from Activity a where a.energyConsumption <= ?1 order by a.energyConsumption desc")
    public List<Activity> findByEnergyConsumptionDesc(long energyConsumption );

    /**
     * Find all the activities that has energy consumption values between the given parameters
     * @param small
     * @param big
     * @return a list of activities
     */
    @Query("select a from Activity a where a.energyConsumption >= ?1 and a.energyConsumption <= ?2")
    public Optional<List<Activity>> findActivitiesInRange(long small, long big);

    public Optional<Activity> findById(Long id);

    public void deleteActivityByActivityID(String title);

    public Optional<Activity> findByActivityID(String id);
}
