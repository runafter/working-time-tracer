package com.runafter.wtt;

import java.util.List;

import io.realm.Realm;
import io.realm.Sort;

/**
 * Created by runaf on 2017-01-16.
 */

public class InOutLogRepository {
    private final Realm realm;

    public InOutLogRepository(Realm realm) {
        this.realm = realm;
    }

    public List<InOutLog> findDesc(long fr, long to) {
        return realm.where(InOutLog.class)
                .greaterThanOrEqualTo(InOutLog.FIELD_TIME, fr)
                .lessThan(InOutLog.FIELD_TIME, to)
                .findAllSorted(InOutLog.FIELD_TIME, Sort.DESCENDING);
    }
}
