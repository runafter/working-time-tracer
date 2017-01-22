package com.runafter.wtt;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.runafter.wtt.dialogs.InOutLogDialogFragment;
import com.runafter.wtt.fragments.DashboardFragment;
import com.runafter.wtt.fragments.InOutLogsFragment;

import java.util.Collection;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                    DashboardFragment.OnDashboardFragmentInteractionListener,
                    InOutLogsFragment.OnInOutFragmentInteractionListener {
    private static final String TAG = "WTT";


    private Realm realm;
    private Handler handler;
    private MenuItem menuMonitoringStart;
    private MenuItem menuMonitoringRunning;
    private SharedPreferences prefs;
    private Fragment fragment;
    private RealmChangeListener inOutLogChangeListener;
    private RealmResults<InOutLog> inOutLogRealmResults;
    private InOutLogDialogFragment.InOutLogDialogListener inOutLogDailogListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null)
            initFragment();

        this.prefs = this.getSharedPreferences(SharePreferenceConfig.NAME, Context.MODE_PRIVATE);

        menuMonitoringStart = navigationView.getMenu().findItem(R.id.monitoring_start);
        menuMonitoringRunning = navigationView.getMenu().findItem(R.id.monitoring_running);
        drawer.addDrawerListener(drawerListener());


        this.handler = new Handler();

        initDB();

        startMonitoringService();


    }

    private void initInOutLogDialogListener() {
        this.inOutLogDailogListener = new InOutLogDialogFragment.InOutLogDialogListenerAdapter() {
            @Override
            public void onCreate(final InOutLog inOutLog) {
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.insertOrUpdate(inOutLog);
//                        InOutLog object = realm.createObject(InOutLog.class, inOutLog.getTime());
//                        object.setType(inOutLog.getType());
//                        object.setDesc(inOutLog.getDesc());
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        toastMessage("신규 출입 기록이 추가되었습니다.");
                    }
                });
            }
        };
    }

    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public static RealmConfiguration realmConfiguration() {
        return new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(0)
                .build();
    }

    private void initFragment() {
        Log.d(TAG, "initFragment");
        replaceFragment(DashboardFragment.newInstance());
    }

    private void replaceFragment(Fragment fragment) {
        if (this.fragment == fragment || (this.fragment != null && this.fragment.getClass() == fragment.getClass()))
            return;

        Log.d(TAG, "replaceFragment " + fragment);
        FragmentManager fm = getFragmentManager();

        String tag = fragment.getClass().getName();
        Fragment lastFragment = fm.findFragmentByTag(tag);
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if (this.fragment != null) {
            if (lastFragment != null)
                fragment = lastFragment;
            fragmentTransaction.replace(R.id.frame, fragment, tag);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        } else
            fragmentTransaction.add(R.id.frame, fragment, tag);
        this.fragment = fragment;
        fragmentTransaction.commit();

    }

    private DrawerLayout.DrawerListener drawerListener() {
        return new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                updateMenuToggleService();
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        };
    }


    public boolean isMonitoringServiceRunning() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        String name = MdmNotificationListenerService.class.getName();
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (name.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void updateMenuToggleService() {
        if (isMonitoringServiceRunning()) {
            menuMonitoringRunning.setVisible(true);
            menuMonitoringStart.setVisible(false);
        } else {
            menuMonitoringRunning.setVisible(false);
            menuMonitoringStart.setVisible(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity.onResume");
        if (realm != null)
            realm.close();
        realm = Realm.getInstance(realmConfiguration());
        setUpWorkingTimesDataUpdater(lastWorkingTimesUpdatedTime());
        initInOutLogDialogListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "MainActivity.onPause");
        if (realm != null) {
            realm.removeChangeListener(inOutLogChangeListener);
            inOutLogChangeListener = null;
            inOutLogRealmResults = null;
            realm.close();
            realm = null;
        }
    }

    private long lastWorkingTimesUpdatedTime() {
        return 0;
    }

    private void setUpWorkingTimesDataUpdater(final long from) {
        this.inOutLogChangeListener = new RealmChangeListener<RealmResults<InOutLog>>() {
            @Override
            public void onChange(RealmResults<InOutLog> results) {
                Log.d(TAG, "setUpWorkingTimesDataUpdater.onChange " + results.size());
                new WorkingTimesDataUpdateAsyncTask().execute(from, InOutLog.copyOf(results));
            }
        };
        inOutLogRealmResults = realm.where(InOutLog.class)
                .greaterThanOrEqualTo(InOutLog.FIELD_TIME, from)
                .findAllSortedAsync(InOutLog.FIELD_TIME, Sort.DESCENDING);
        inOutLogRealmResults.addChangeListener(inOutLogChangeListener);
    }

    public static class WorkingTimesDataUpdateAsyncTask extends AsyncTask<Object, Void, Collection<WorkingTime>> {
        private Realm realm;

        @Override
        protected Collection<WorkingTime> doInBackground(Object... params) {
            this.realm = Realm.getInstance(realmConfiguration());
            try {
                WorkingTimeRepository workingTimeRepo = new WorkingTimeRepository(realm);
                InOutLogAnalyzer analyzer = new InOutLogAnalyzer(
                        new InOutLogRepository(realm),
                        workingTimeRepo
                );
                long from = (Long) params[0];
                List<InOutLog> results = (List<InOutLog>) params[1];
                //realm.beginTransaction();
                Collection<WorkingTime> updatedWorkingTimes = analyzer.analyze(from, results);
                //realm.commitTransaction();
                Log.d(TAG, "updatedWorkingTimes " + updatedWorkingTimes);
                workingTimeRepo.updateAll(updatedWorkingTimes);
                return updatedWorkingTimes;
            } finally {
                this.realm.close();
                this.realm = null;
            }

        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
    private void initDB() {
        Realm.init(this.getApplicationContext());
    }

    @Override
    public void onDashboardFragmentInteraction(Uri uri) {

    }

    @Override
    public void onInOutFragmentInteraction(Uri uri) {

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_manage) {
            openNotificationListenrSettings();
        } else if (id == R.id.monitoring_pattern) {
            openMonitorPatternUpdateDialong();
        } else if (id == R.id.nav_send) {
            clearAllLogs();
        } else if (id == R.id.monitoring_start) {
            startMonitoringService();
        } else if (id == R.id.monitoring_running) {
            updateMenuToggleService();
        } else if (id == R.id.menu_dashboard) {
            viewDashboardFragment();
        } else if (id == R.id.menu_in_out_logs) {
            viewInOutLogsFragrment();
        } else if (id == R.id.menu_add_in_log_now) {
            addInLogNow();
        } else if (id == R.id.menu_add_out_log_now) {
            addOutLogNow();
        } else if (id == R.id.menu_add_inout_log) {
            showInOutDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showInOutDialog() {
        InOutLogDialogFragment fragment = new InOutLogDialogFragment();
        fragment.setResultListener(this.inOutLogDailogListener);
        fragment.show(getFragmentManager(), "add-new-inoutlog");
    }

    private void addOutLogNow() {
        addInOutLog(DateTimeUtils.nowTime(), InOutLog.TYPE_OUT);
    }

    private void addInLogNow() {
        addInOutLog(DateTimeUtils.nowTime(), InOutLog.TYPE_IN);
    }

    private void addInOutLog(final long time, final String type) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                InOutLog log = bgRealm.createObject(InOutLog.class, time);
                log.setType(type);
                log.setDesc("manual");
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                // Transaction was a success.
                Log.d(TAG, "addInOutLog.executeTransactionAsync.onSuccess");
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Log.d(TAG, "addInOutLog.executeTransactionAsync.onError " + error);
            }
        });
    }

    private void viewInOutLogsFragrment() {
        Log.d(TAG, "viewInOutLogsFragrment");
        replaceFragment(InOutLogsFragment.newInstance());
    }

    private void viewDashboardFragment() {
        Log.d(TAG, "viewDashboardFragment");
        replaceFragment(DashboardFragment.newInstance());
    }

    private void openMonitorPatternUpdateDialong() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("알림 감지 패턴 설정");
        builder.setMessage("정규표현식으로 입력 가능합니다.");
        builder.setCancelable(true);

        String packageNamePattern = prefs.getString(SharePreferenceConfig.KEY_MONITOR_NOTIFICATION_PACKAGE_NAME_PATTERN, BuildConfig.MONITORING_PATTERN_PACKAGNE_NAME);
        String titlePattern = prefs.getString(SharePreferenceConfig.KEY_MONITOR_NOTIFICATION_TITLE_PATTERN, BuildConfig.MONITORING_PATTERN_TITLE);
        String textPattern = prefs.getString(SharePreferenceConfig.KEY_MONITOR_NOTIFICATION_TEXT_PATTERN, BuildConfig.MONITORING_PATTERN_TEXT);

        View convertView = LayoutInflater.from(this).inflate(R.layout.pattern_dialog_layout, null, false);

        final EditText packageName = (EditText)convertView.findViewById(R.id.etPackageName);
        final EditText title = (EditText)convertView.findViewById(R.id.etTitle);
        final EditText text = (EditText)convertView.findViewById(R.id.etText);

        packageName.setText(packageNamePattern);
        title.setText(titlePattern);
        text.setText(textPattern);

        builder.setView(convertView);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                prefs.edit()
                        .putString(SharePreferenceConfig.KEY_MONITOR_NOTIFICATION_PACKAGE_NAME_PATTERN, packageName.getText().toString())
                        .putString(SharePreferenceConfig.KEY_MONITOR_NOTIFICATION_TITLE_PATTERN, title.getText().toString())
                        .putString(SharePreferenceConfig.KEY_MONITOR_NOTIFICATION_TEXT_PATTERN, text.getText().toString())
                        .commit();
                startMonitoringService();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.show();
    }


    private void startMonitoringService() {
        AsyncTask task = new AsyncTask() {
            @Nullable
            @Override
            protected Object doInBackground(Object[] params) {
                startService(new Intent(MainActivity.this, MdmNotificationListenerService.class));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateMenuToggleService();
                    }
                });
                return null;
            }
        };
        task.execute();
    }

    private void openNotificationListenrSettings() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                startActivity(intent);
            }
        });
    }

    private void clearAllLogs() {
        AsyncTask task = new AsyncTask() {
            @Nullable
            @Override
            protected Object doInBackground(Object[] params) {
                Realm realm = Realm.getInstance(realmConfiguration());
                try {
                    realm.beginTransaction();
                    realm.delete(InOutLog.class);
                    realm.commitTransaction();
                } finally {
                    realm.close();
                }
                return null;
            }
        };
        task.execute();
    }
}
