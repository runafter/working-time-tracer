package com.runafter.wtt;


import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.realm.Realm;

/**
 * Created by runaf on 2017-01-06.
 */

public class MdmNotificationListenerService extends NotificationListenerService {
    private static final String TAG = "MNLService";
    private Map<Long, Realm> realms = new ConcurrentHashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        initDB();
        Log.d(TAG, Thread.currentThread().getName() + " : onCreate ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeDB();
        Log.d(TAG, Thread.currentThread().getName() + " : onDestroy ");
    }

    private void closeDB() {
        Collection<Realm> realms = new ArrayList<>(this.realms.values());
        for (Realm realm : realms) {
            try {
                realm.close();
            } catch (Throwable t) {
                Log.w(TAG, "cannot close realm " + realm);
            }
        }
    }

    private void initDB() {
        Realm.init(this);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        Log.d(TAG, Thread.currentThread().getName() + " : onNotificationPosted " + sbn);
        insert("onNotificationPosted(sbn)");
    }

    private void insert(String type) {
        Realm realm = realm();
        realm.beginTransaction();
        realm.insert(WorkingTimeLog.of(GregorianCalendar.getInstance().getTimeInMillis(), type));
        realm.commitTransaction();
    }

    private Realm realm() {
        long key = Thread.currentThread().getId();
        Realm realm = realms.get(key);
        if (realm == null) {
            realm = Realm.getDefaultInstance();
            realms.put(key, realm);
        }
        return realm;
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        Log.d(TAG, Thread.currentThread().getName() + " : onNotificationRemoved " + sbn);
        insert("onNotificationRemoved(sbn)");
    }
}
