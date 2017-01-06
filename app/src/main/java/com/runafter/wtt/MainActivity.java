package com.runafter.wtt;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import io.realm.RealmQuery;

public class MainActivity extends AppCompatActivity {

    private ListView listLogs;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDB();

        setContentView(R.layout.activity_main);
        this.listLogs = (ListView)findViewById(R.id.logs);
        this.listLogs.setAdapter(logsListAdapter());
    }

    private void initDB() {
        Realm.init(this);
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

            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listview_logs_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.timeView = (TextView)convertView.findViewById(R.id.logs_time);
                viewHolder.typeView = (TextView)convertView.findViewById(R.id.logs_type);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
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
        RealmBaseAdapter<WorkingTimeLog> adapter = new WorkingTimeLogAdapter(this, q.findAll()) ;
        return adapter;
    }

    private WorkingTimeLog logOf(Date time, String type) {
        return new WorkingTimeLog(time.getTime(), type);
    }


}
