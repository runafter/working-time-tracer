package com.runafter.wtt;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import io.realm.Realm;

/**
 * Created by runaf on 2017-01-06.
 */

public class MdmNotificationListenerService extends NotificationListenerService {
    private static final String TAG = "MNLService";
    private Map<Long, Realm> realms = new ConcurrentHashMap<>();
    private SharedPreferences prefs;
    private Pattern packageNamePattern;
    private String packageNameRegex;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, Thread.currentThread().getName() + " : onCreate ");
        initDB();
        this.prefs = this.getSharedPreferences(SharePreferenceConfig.NAME, Context.MODE_PRIVATE);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, Thread.currentThread().getName() + " : onDestroy ");
        closeDB();
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
        Realm.init(this.getApplicationContext());
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        Log.d(TAG, Thread.currentThread().getName() + " : onNotificationPosted " + sbn.getId());
        if (match(sbn)) {
            insert("IN " + sbn.getPackageName());
            log(sbn);
        }
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
        if (match(sbn)) {
            insert("OUT " + sbn.getPackageName());
            log(sbn);
        }
    }

    private void log(StatusBarNotification sbn) {
        Log.d(TAG, "=================== StatusBarNotification ===================");
        Log.d(TAG, "sbn.Id " + sbn.getId());
        Log.d(TAG, "sbn.PackageName " + sbn.getPackageName());
        Log.d(TAG, "sbn.Tag " + sbn.getTag());
        Log.d(TAG, "sbn.PostTime " + sbn.getPostTime());
        Log.d(TAG, "------------------- notification -------------------");
        Notification notification = sbn.getNotification();
        Log.d(TAG, "notification " + notification.toString());

        Bundle extras = notification.extras;
        Log.d(TAG, "EXTRA_TEXT " + extras.get(Notification.EXTRA_TEXT));
        Log.d(TAG, "EXTRA_TEXT_LINES " + extras.get(Notification.EXTRA_TEXT_LINES));
        Log.d(TAG, "EXTRA_BIG_TEXT " + extras.get(Notification.EXTRA_BIG_TEXT));
        Log.d(TAG, "EXTRA_INFO_TEXT " + extras.get(Notification.EXTRA_INFO_TEXT));
        Log.d(TAG, "EXTRA_MESSAGES " + extras.get(Notification.EXTRA_MESSAGES));
        Log.d(TAG, "EXTRA_SUB_TEXT " + extras.get(Notification.EXTRA_SUB_TEXT));
        Log.d(TAG, "EXTRA_SUMMARY_TEXT " + extras.get(Notification.EXTRA_SUMMARY_TEXT));
        Log.d(TAG, "EXTRA_TITLE " + extras.get(Notification.EXTRA_TITLE));
        Log.d(TAG, "EXTRA_TITLE_BIG " + extras.get(Notification.EXTRA_TITLE_BIG));

    }

    private boolean match(StatusBarNotification sbn) {
        Pattern pattern = getPackageNamePattern();
        String packageName = sbn.getPackageName();
        return pattern.matcher(packageName).find();
    }

    private Pattern getPackageNamePattern() {
        String regex = prefs.getString(SharePreferenceConfig.KEY_MONITOR_NOTIFICATION_PACKAGE_NAME_PATTERN, ".*");
        if (this.packageNameRegex != null && this.packageNameRegex.equals(regex))
            return this.packageNamePattern;

        try {
            Pattern pattern = Pattern.compile(regex);
            this.packageNameRegex = regex;
            return this.packageNamePattern = pattern;
        } catch (Throwable t) {
            return this.packageNamePattern;
        }
    }
}
