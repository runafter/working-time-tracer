package com.runafter.wtt;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import io.realm.RealmQuery;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "Main";
    private ListView listLogs;
    private Realm realm;
    private Handler handler;
    private MenuItem menuMonitoringStart;
    private MenuItem menuMonitoringRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        menuMonitoringStart = navigationView.getMenu().findItem(R.id.monitoring_start);
        menuMonitoringRunning = navigationView.getMenu().findItem(R.id.monitoring_running);
        drawer.addDrawerListener(drawerListener());

        this.listLogs = (ListView)findViewById(R.id.logs);

        this.handler = new Handler();

        initDB();

        this.handler.post(taskInitLogListView());
        startMonitoringService();
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

    private Runnable taskInitLogListView() {
        return new Runnable() {
            @Override
            public void run() {
                MainActivity.this.listLogs.setAdapter(logsListAdapter());
            }
        };
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
    private static class WorkingTimeLogAdapter extends RealmBaseAdapter<WorkingTimeLog> {
        private DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        public WorkingTimeLogAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<WorkingTimeLog> data) {
            super(context, data);
        }

        private static class ViewHolder {
            TextView typeView;
            TextView timeView;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater.from(context).inflate(R.layout.listview_logs_item, parent, false);

            WorkingTimeLogAdapter.ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listview_logs_item, parent, false);
                viewHolder = new WorkingTimeLogAdapter.ViewHolder();
                viewHolder.timeView = (TextView)convertView.findViewById(R.id.logs_time);
                viewHolder.typeView = (TextView)convertView.findViewById(R.id.logs_type);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (WorkingTimeLogAdapter.ViewHolder) convertView.getTag();
            }

            WorkingTimeLog item = adapterData.get(position);
            viewHolder.timeView.setText(format(item.getTime()));
            viewHolder.typeView.setText(item.getType().toString());
            return convertView;
        }

        private String format(long time) {
            return format.format(new Date(time));
        }
    }
    private ListAdapter logsListAdapter() {
        RealmQuery<WorkingTimeLog> q = realm.where(WorkingTimeLog.class);
        RealmBaseAdapter<WorkingTimeLog> adapter = new WorkingTimeLogAdapter(this, q.findAllSorted("time", Sort.DESCENDING)) ;
        return adapter;
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
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {
            clearAllLogs();
        } else if (id == R.id.monitoring_start) {
            startMonitoringService();
        } else if (id == R.id.monitoring_running) {
            updateMenuToggleService();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void startMonitoringService() {
        AsyncTask task = new AsyncTask() {
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
            @Override
            protected Object doInBackground(Object[] params) {
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                realm.delete(WorkingTimeLog.class);
                realm.commitTransaction();
                realm.close();
                return null;
            }
        };
        task.execute();
    }
}
