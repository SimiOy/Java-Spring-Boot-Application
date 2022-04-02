package server.api.Mocks;

import commons.Activity;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import server.database.ActivitiesRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class TestActivitiesRepository implements ActivitiesRepository {

    List<Activity> activities = new ArrayList<Activity>();

    @Override
    public void deleteActivityByActivityID(String title) {

    }

    @Override
    public Optional<Activity> findByActivityID(String id) {
        return Optional.empty();
    }

    public List<Activity> findByEnergyConsumptionDesc(long energyConsumption){
        ArrayList<Activity> ret = new ArrayList<Activity>();
        for(Activity a : activities){
            if(a.getEnergyConsumption() == energyConsumption)
                ret.add(a);
        }
        return ret;
    }

    @Override
    public Optional<List<Activity>> findActivitiesInRange(long small, long big) {
        return Optional.empty();
    }

    @Override
    public List<Activity> findAll() {
        return activities;
    }

    @Override
    public List<Activity> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<Activity> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<Activity> findAllById(Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long s) {

    }

    @Override
    public void delete(Activity entity) {
        if(activities.contains(entity))
            activities.remove(entity);
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends Activity> entities) {

    }

    @Override
    public void deleteAll() {
        activities = new ArrayList<Activity>();
    }

    @Override
    public <S extends Activity> S save(S entity) {
        activities.add(entity);
        return entity;
    }

    @Override
    public <S extends Activity> List<S> saveAll(Iterable<S> entities) {
        for(S a : entities){
            activities.add(a);
        }
        return null;
    }

    @Override
    public Optional<Activity> findById(Long l) {
        for(Activity a : activities){
            if(a.getId().equals(l))
                return Optional.of(a);
        }
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long l) {
        return false;
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends Activity> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends Activity> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<Activity> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public Activity getOne(Long aLong) {
        return null;
    }

    @Override
    public Activity getById(Long aLong) {
        return null;
    }

    @Override
    public <S extends Activity> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Activity> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends Activity> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends Activity> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Activity> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Activity> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends Activity, R> R findBy(Example<S> example,
                                            Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }
}
