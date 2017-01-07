package com.runafter.wtt;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "Main";
    private ListView listLogs;
    private Realm realm;
    private Handler handler;
    private MenuItem menuMonitoringStart;
    private MenuItem menuMonitoringRunning;
    private WebView webview;
    private SharedPreferences prefs;
    private Settings settings;

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

        initSharedPreference();

        menuMonitoringStart = navigationView.getMenu().findItem(R.id.monitoring_start);
        menuMonitoringRunning = navigationView.getMenu().findItem(R.id.monitoring_running);
        drawer.addDrawerListener(drawerListener());

        this.webview = (WebView)findViewById(R.id.webview);
        this.listLogs = (ListView)findViewById(R.id.logs);

        this.handler = new Handler();

        initWebView();
        initDB();

        this.handler.post(taskInitLogListView());
        startMonitoringService();
    }

    private void initSharedPreference() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.settings = new Settings(prefs);
    }

    private static class Settings {
        private final SharedPreferences prefs;
        private long time;

        public Settings(SharedPreferences prefs) {
            this.prefs = prefs;
        }

        public String getWebViewURI() {
            return "http://asunhs.github.io/dongsu/";
        }

        public long getLastAppliedLogTime() {
            return time;
        }
        public void setLastAppliedLogTime(long time) {
            this.time = time;
        }

    }

    private Page webViewPage;
    private void initWebView() {
        WebSettings settings = webview.getSettings();
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setTextZoom(80);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webViewPage = new Page(webview);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        webViewPage.isAutoMode().then(new DoneCallback() {
                            @Override
                            public void onDone(Object result) {
                                webViewPage.setManualMode().then(new DoneCallback() {
                                    @Override
                                    public void onDone(Object result) {
                                        applyLogs();
                                    }
                                });
                            }
                        }, new FailCallback() {
                            @Override
                            public void onFail(Object result) {
                                applyLogs();
                            }
                        });
                    }
                });
            }
        });



        this.handler.post(new Runnable() {
            @Override
            public void run() {
                webview.loadUrl(MainActivity.this.settings.getWebViewURI());
            }
        });
    }

    private void applyLogs() {
        Log.d(TAG, "applyLogs ");

        long lastAppliedTime = settings.getLastAppliedLogTime();
        RealmResults<InOutLog> results = realm.where(InOutLog.class).greaterThanOrEqualTo("time", lastAppliedTime).findAllSorted("time");

        Map<Date, WorkingTime> workingTimes = new LinkedHashMap<>();

        for (InOutLog result : results)
            summeryWorkingTime(workingTimes, result);

        scheduleupdateWebViewLog(new LinkedList<>(workingTimes.values()));
    }
    private void scheduleupdateWebViewLog(final LinkedList<WorkingTime> queue) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateWebViewLogs(queue);
            }
        });
    }
    private void updateWebViewLogs(final LinkedList<WorkingTime> queue) {
        if (queue.isEmpty())
            return;
        final WorkingTime workingTime = queue.poll();
        webViewPage.updateStartToMin(workingTime.date, workingTime.start).done(new DoneCallback() {
            @Override
            public void onDone(Object result) {
                webViewPage.updateEndToMax(workingTime.date, workingTime.end).always(new AlwaysCallback(){
                    @Override
                    public void onAlways(Promise.State state, Object resolved, Object rejected) {
                        scheduleupdateWebViewLog(queue);
                    }
                });
            }
        }).fail(new FailCallback() {
            @Override
            public void onFail(Object result) {
                scheduleupdateWebViewLog(queue);
            }
        });
    }

    private void summeryWorkingTime(Map<Date, WorkingTime> workingTimes, InOutLog log) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(log.getTime()));
        Date time = cal.getTime();
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();

        boolean isIn = log.getType().contains("IN");

        WorkingTime workingTime = workingTimes.get(date);
        if (workingTime == null) {
            workingTime = new WorkingTime();
            workingTime.date = date;
            workingTimes.put(date, workingTime);
        }

        if (isIn)
            workingTime.start = min(workingTime.start, time);
        else
            workingTime.end = max(workingTime.end, time);
    }

    private Date max(Date end, Date time) {
        return end == null || end.getTime() < time.getTime() ? time : end;
    }

    private Date min(Date start, Date time) {
        return start == null || start.getTime() > time.getTime() ? time : start;
    }


    public static class WorkingTime {
        private Date date;
        private Date start;
        private Date end;
    }

    public static class Page {
        private final WebView webView;
        private String divAuto = "document.querySelector('div.hb.auto.clk')";
        private String divRecord = "document.querySelector('div.hb.record.clk')";

        public Page(WebView webView) {
            this.webView = webView;
        }

        public Promise isAutoMode() {
            final Deferred deferred = new DeferredObject();
            final Promise promise = deferred.promise();
            webView.evaluateJavascript(divAuto + ".innerText" , new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    Log.d(TAG, "isAutoMode " + value);
                    if ("\"AUTO\"".equals(value))
                        deferred.resolve(null);
                    else
                        deferred.reject(null);
                }
            });
            return promise;
        }
        public Promise isRecordMode() {
            final Deferred deferred = new DeferredObject();
            final Promise promise = deferred.promise();
            webView.evaluateJavascript(divRecord + ".innerText" , new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    Log.d(TAG, "isRecordMode " + value);
                    if ("\"RECODING\"".equals(value))
                        deferred.resolve(null);
                    else
                        deferred.reject(null);
                }
            });
            return promise;
        }

        public Promise setManualMode() {
            final Deferred deferred = new DeferredObject();
            webView.evaluateJavascript(divAuto + ".click()" , new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    Log.d(TAG, "setManualMode click() " + value);
                    deferred.resolve(null);
                }
            });
            return deferred.promise();
        }

        public Promise updateStartToMin(Date date, Date start) {
/**
 var date = "1/7"
 var startTime = null;
 var endTime = null;
 var trs = document.querySelectorAll('tbody[data-reactid=\\.0\\.1\\.1]>tr')
 var tr;
 for (var i in trs) if (trs[i].firstChild && trs[i].firstChild.innerText === date ) tr = trs[i];
 if (tr) {
 var startTd = tr.childNodes[1];
 var endTd = tr.childNodes[2];
 console.log(startTd);
 console.log(endTd);
 if (startTime) {
 new Date(startTime)
 }

 return true;
 } else {
 return false;
 }


 */
            return null;
        }

        public Promise updateEndToMax(Date date, Date end) {
            return null;
        }
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
    private static class WorkingTimeLogAdapter extends RealmBaseAdapter<InOutLog> {
        private DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        public WorkingTimeLogAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<InOutLog> data) {
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

            InOutLog item = adapterData.get(position);
            viewHolder.timeView.setText(format(item.getTime()));
            viewHolder.typeView.setText(item.getType().toString());
            return convertView;
        }

        private String format(long time) {
            return format.format(new Date(time));
        }
    }
    private ListAdapter logsListAdapter() {
        RealmQuery<InOutLog> q = realm.where(InOutLog.class);
        RealmBaseAdapter<InOutLog> adapter = new WorkingTimeLogAdapter(this, q.findAllSorted("time", Sort.DESCENDING)) ;
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
                realm.delete(InOutLog.class);
                realm.commitTransaction();
                realm.close();
                return null;
            }
        };
        task.execute();
    }
}
