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

import com.runafter.wtt.fragments.DashboardFragment;
import com.runafter.wtt.fragments.InOutLogsFragment;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                    DashboardFragment.OnDashboardFragmentInteractionListener,
                    InOutLogsFragment.OnInOutFragmentInteractionListener {
    private static final String TAG = "Main";


    private Realm realm;
    private Handler handler;
    private MenuItem menuMonitoringStart;
    private MenuItem menuMonitoringRunning;
    private SharedPreferences prefs;
    private Fragment fragment;

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
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        realm.close();
    }
    private void initDB() {
        Realm.init(this.getApplicationContext());
        realm = Realm.getDefaultInstance();
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
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

        String packageNamePattern = prefs.getString(SharePreferenceConfig.KEY_MONITOR_NOTIFICATION_PACKAGE_NAME_PATTERN, ".*");
        String titlePattern = prefs.getString(SharePreferenceConfig.KEY_MONITOR_NOTIFICATION_TITLE_PATTERN, ".*");
        String textPattern = prefs.getString(SharePreferenceConfig.KEY_MONITOR_NOTIFICATION_TEXT_PATTERN, ".*");

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
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                realm.delete(InOutLog.class);
                realm.commitTransaction();
                realm.close();
                return null;
            }
        };
        task.execute();
    }
}
