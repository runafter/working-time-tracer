package com.runafter.wtt.fragments;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.runafter.wtt.MainActivity;
import com.runafter.wtt.R;
import com.runafter.wtt.InOutLog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import io.realm.RealmQuery;
import io.realm.Sort;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnInOutFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InOutLogsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InOutLogsFragment extends Fragment {
    private static final String TAG = "WTT";

    private ListView listLogs;
    private OnInOutFragmentInteractionListener mListener;
    private Realm realm;
    private Handler handler;

    public InOutLogsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *

     * @return A new instance of fragment InOutLogsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InOutLogsFragment newInstance() {
        InOutLogsFragment fragment = new InOutLogsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, this + ".onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, this + ".onCreateView");
        View view = inflater.inflate(R.layout.fragment_in_out_logs, container, false);
        this.listLogs = (ListView)view.findViewById(R.id.logs);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onInOutFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, this + ".onAttach");
        if (context instanceof OnInOutFragmentInteractionListener) {
            mListener = (OnInOutFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnInOutFragmentInteractionListener");
        }

        this.handler = new Handler();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, this + ".onResume");
        if (this.realm != null)
            this.realm.close();
        this.realm = Realm.getInstance(MainActivity.realmConfiguration());
        if (this.handler != null)
            this.handler.post(taskInitLogListView());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (realm != null) {
            realm.close();
            realm = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, this + ".onDetach");
        mListener = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, this + ".onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, this + ".onDestroy");
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnInOutFragmentInteractionListener {
        // TODO: Update argument type and name
        void onInOutFragmentInteraction(Uri uri);
    }

    private Runnable taskInitLogListView() {
        return new Runnable() {
            @Override
            public void run() {
                InOutLogsFragment.this.listLogs.setAdapter(logsListAdapter());
            }
        };
    }
    private ListAdapter logsListAdapter() {
        RealmQuery<InOutLog> q = realm.where(InOutLog.class);
        RealmBaseAdapter<InOutLog> adapter = new WorkingTimeLogAdapter(this.getActivity(), q.findAllSorted("time", Sort.DESCENDING)) ;
        return adapter;
    }

    private static class WorkingTimeLogAdapter extends RealmBaseAdapter<InOutLog> {
        private DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        private View.OnClickListener typeOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag() instanceof WorkingTimeLogAdapter.ViewHolder) {
                    WorkingTimeLogAdapter.ViewHolder viewHolder = (WorkingTimeLogAdapter.ViewHolder)v.getTag();
                    String tag = (viewHolder.typeView.getTag() instanceof String) ? (String)viewHolder.typeView.getTag() : "";
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Description");
                    builder.setMessage(tag);
                    builder.show();
                }
            }
        };

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
            String type = item.getType();
            viewHolder.typeView.setText(type != null && !type.isEmpty() ? type.toString() : "<EMPTY>");
            viewHolder.typeView.setTag(item.getDesc());
            convertView.setOnClickListener(typeOnClickListener);
            return convertView;
        }

        private String format(long time) {
            return format.format(new Date(time));
        }
    }
}
