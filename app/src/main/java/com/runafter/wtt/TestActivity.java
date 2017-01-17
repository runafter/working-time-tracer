package com.runafter.wtt;


import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;

/**
 * Created by runaf on 2017-01-17.
 */

public class TestActivity extends AppCompatActivity {
    Realm realm;
    private Handler handler;

    @Override
    protected void onResume() {
        super.onResume();
        handler = new Handler();
        Realm.init(this);
        this.realm = Realm.getInstance(realmConfiguration());
    }

    public RealmConfiguration realmConfiguration() {
        return new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(0)
                .build();
    }


    @Override
    protected void onPause() {
        super.onPause();
        realm.close();
    }

    public Handler handler() {
        return this.handler;
    }
}
