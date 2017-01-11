package com.runafter.wtt;


import android.app.Notification;
import android.content.Context;
import android.content.Intent;
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
    private Pattern titlePattern;
    private Pattern textPattern;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, Thread.currentThread().getName() + " : onCreate ");
        initDB();
        this.prefs = this.getSharedPreferences(SharePreferenceConfig.NAME, Context.MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int condition =  super.onStartCommand(intent, flags, startId);
        updateNotificationPattern();
        return condition;
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
            insert("IN", sbn);
            log(sbn);
        }
    }

    private void insert(String type, StatusBarNotification sbn) {
        Realm realm = realm();
        realm.beginTransaction();
        String desc = descOf(sbn);
        realm.insert(InOutLog.of(GregorianCalendar.getInstance().getTimeInMillis(), type, desc));
        realm.commitTransaction();
    }

    private String descOf(StatusBarNotification sbn) {
        StringBuilder builder = new StringBuilder();
        builder.append("sbn.id : " + sbn.getId() + "\n");
        builder.append("sbn.packageName : " + sbn.getPackageName() + "\n");
        builder.append("sbn.tag : " + sbn.getTag() + "\n");
        builder.append("sbn.postTime : " + sbn.getPostTime() + "\n");

        Notification notification = sbn.getNotification();
        builder.append("sbn.notification.tickerText : " + notification.tickerText + "\n");
        Bundle extras = notification.extras;

        builder.append("sbn.notification.extras.text : " + extras.get(Notification.EXTRA_TEXT) + "\n");
        builder.append("sbn.notification.extras.textLines : " + extras.get(Notification.EXTRA_TEXT_LINES) + "\n");
        builder.append("sbn.notification.extras.bigText : " + extras.get(Notification.EXTRA_BIG_TEXT) + "\n");
        builder.append("sbn.notification.extras.infoText : " + extras.get(Notification.EXTRA_INFO_TEXT) + "\n");
        builder.append("sbn.notification.extras.messages : " + extras.get(Notification.EXTRA_MESSAGES) + "\n");
        builder.append("sbn.notification.extras.subText : " + extras.get(Notification.EXTRA_SUB_TEXT) + "\n");
        builder.append("sbn.notification.extras.summaryText : " + extras.get(Notification.EXTRA_SUMMARY_TEXT) + "\n");
        builder.append("sbn.notification.extras.title : " + extras.get(Notification.EXTRA_TITLE) + "\n");
        builder.append("sbn.notification.extras.titleBig : " + extras.get(Notification.EXTRA_TITLE_BIG) + "\n");

        return builder.toString();
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
            insert("OUT", sbn);
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
        String packageName = sbn.getPackageName();
        if (!packageNamePattern.matcher(packageName != null ? packageName : "").find())
            return false;

        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString(Notification.EXTRA_TITLE);
        if (!titlePattern.matcher(title != null ? title : "").find())
            return false;

        String text = extras.getString(Notification.EXTRA_TEXT);
        return textPattern.matcher(text).find();
    }

    private void updateNotificationPattern() {
        String packageName = prefs.getString(SharePreferenceConfig.KEY_MONITOR_NOTIFICATION_PACKAGE_NAME_PATTERN, ".*");
        String title = prefs.getString(SharePreferenceConfig.KEY_MONITOR_NOTIFICATION_TITLE_PATTERN, ".*");
        String text = prefs.getString(SharePreferenceConfig.KEY_MONITOR_NOTIFICATION_TEXT_PATTERN, ".*");

        try {
            Pattern packageNamePattern = Pattern.compile(packageName);
            Pattern titlePattern = Pattern.compile(title);
            Pattern textPattern = Pattern.compile(text);

            this.packageNamePattern = packageNamePattern;
            this.titlePattern = titlePattern;
            this.textPattern = textPattern;
        } catch (Throwable t) {
            Log.e(TAG, "cannot update notification pattern packageName[" + packageName + "] titlePattern[" + titlePattern + "] textPattern[" + textPattern + "]", t);
        }
    }
}
