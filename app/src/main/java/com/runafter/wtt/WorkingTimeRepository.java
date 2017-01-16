package com.runafter.wtt;

import java.util.Collection;
import java.util.List;

import io.realm.Realm;

/**
 * Created by runaf on 2017-01-16.
 */

public class WorkingTimeRepository {
    private final Realm realm;

    public WorkingTimeRepository(Realm realm) {
        this.realm = realm;
    }
    public List<WorkingTime> find(long fr, long to) {
        return realm.where(WorkingTime.class)
                .greaterThanOrEqualTo(WorkingTime.FIELD_DATE, fr)
                .lessThanOrEqualTo(WorkingTime.FIELD_DATE, to)
                .findAll();
    }

    public void updateAll(Collection<WorkingTime> workingTimes) {
        realm.beginTransaction();
        realm.insertOrUpdate(workingTimes);
        realm.commitTransaction();
    }
}
