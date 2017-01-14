package com.runafter.wtt.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.runafter.wtt.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.everything.android.ui.overscroll.IOverScrollDecor;
import me.everything.android.ui.overscroll.IOverScrollState;
import me.everything.android.ui.overscroll.ListenerStubs;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnDashboardFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = "Dashboard";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnDashboardFragmentInteractionListener mListener;
    private ListView lvWorkingTimes;

    public DashboardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DashboardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DashboardFragment newInstance() {
        DashboardFragment fragment = new DashboardFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
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
        View fragment = inflater.inflate(R.layout.fragment_dashboard, container, false);
        this.lvWorkingTimes = (ListView) fragment.findViewById(R.id.list_working_times);
        this.lvWorkingTimes.setAdapter(workingTImesApdapter());
        Log.d(TAG, this + ".onCreateView");
        IOverScrollDecor iOverScrollDecor = OverScrollDecoratorHelper.setUpOverScroll(lvWorkingTimes);
        iOverScrollDecor.setOverScrollStateListener(new ListenerStubs.OverScrollStateListenerStub() {
            @Override
            public void onOverScrollStateChange(IOverScrollDecor decor, int oldState, int newState) {
                super.onOverScrollStateChange(decor, oldState, newState);
               
                Log.d(TAG, "onOverScrollStateChange oldState[" + oldState + ":" + overScrollStateOf(oldState) + "] newState[" + newState + ":" + overScrollStateOf(newState) + "]");
            }
        });
        iOverScrollDecor.setOverScrollUpdateListener(new ListenerStubs.OverScrollUpdateListenerStub(){
            @Override
            public void onOverScrollUpdate(IOverScrollDecor decor, int state, float offset) {
                super.onOverScrollUpdate(decor, state, offset);
                
                Log.d(TAG, "onOverScrollUpdate state[" + state + ":" + overScrollStateOf(state) + "] offset[" + offset + "]");
            }
        });
        return fragment;
    }

    private String overScrollStateOf(int overScrollState) {
        switch (overScrollState) {
            case IOverScrollState.STATE_BOUNCE_BACK:
                return "STATE_BOUNCE_BACK";
            case IOverScrollState.STATE_DRAG_END_SIDE:
                return "STATE_DRAG_END_SIDE";
            case IOverScrollState.STATE_DRAG_START_SIDE:
                return "STATE_DRAG_START_SIDE";
            case IOverScrollState.STATE_IDLE:
                return "STATE_IDLE";
            default:
                return "unknown";
        }
    }


    public class WorkingTimesAdapter extends ArrayAdapter<WorkingTIme> {
        private final Activity activity;
        private final SimpleDateFormat dateFormat;
        private final SimpleDateFormat timeFormat;

        public WorkingTimesAdapter(Activity activity, int textViewResourceId, List<WorkingTIme> objects) {
            super(activity, textViewResourceId, objects);
            this.activity = activity;
            this.dateFormat = new SimpleDateFormat("MM-dd");
            this.timeFormat = new SimpleDateFormat("HH:mm");
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            ViewHolder view;

            if(rowView == null)
            {
                // Get a new instance of the row layout view
                LayoutInflater inflater = activity.getLayoutInflater();
                rowView = inflater.inflate(R.layout.listview_working_time_item, null);

                // Hold the view objects in an object, that way the don't need to be "re-  finded"
                view = new ViewHolder();
                view.date = (TextView) rowView.findViewById(R.id.tvDate);
                view.start= (TextView) rowView.findViewById(R.id.tvStart);
                view.end = (TextView) rowView.findViewById(R.id.tvEnd);
                view.workedTime = (TextView) rowView.findViewById(R.id.tvWorkedTime);
                view.workingTypes = (TextView) rowView.findViewById(R.id.tvWorkingTimeTypes);

                rowView.setTag(view);
            } else {
                view = (ViewHolder) rowView.getTag();
            }

            /** Set data to your Views. */
            WorkingTIme item = getItem(position);
            view.date.setText(dateFormat.format(item.date));
            view.start.setText(timeFormat.format(item.date));
            view.end.setText(timeFormat.format(item.date));
            view.workedTime.setText(timeFormat.format(item.end - item.start));
            view.workingTypes.setText(item.workingType);

            return rowView;
        }
        protected class ViewHolder {
            protected TextView date;
            protected TextView start;
            protected TextView end;
            protected TextView workedTime;
            protected TextView workingTypes;
        }
    }


    private ListAdapter workingTImesApdapter() {
        List<WorkingTIme> list = new ArrayList<>();
        list.add(workingTimeOf(0, 8, 12, "8H"));
        list.add(workingTimeOf(-1, 8, 12, "8H"));
        list.add(workingTimeOf(-2, 11, 20, "0H"));
        list.add(workingTimeOf(-3, 10, 18, "8H"));
        list.add(workingTimeOf(-4, 9, 23, "4H"));
        list.add(workingTimeOf(-5, 9, 23, "4H"));
        list.add(workingTimeOf(-6, 9, 23, "4H"));
        list.add(workingTimeOf(-7, 9, 23, "4H"));
        list.add(workingTimeOf(-8, 9, 23, "4H"));
        list.add(workingTimeOf(-4, 9, 23, "4H"));
        list.add(workingTimeOf(-5, 9, 23, "4H"));
        list.add(workingTimeOf(-6, 9, 23, "4H"));
        list.add(workingTimeOf(-7, 9, 23, "4H"));
        list.add(workingTimeOf(-8, 9, 23, "4H"));
        list.add(workingTimeOf(-4, 9, 23, "4H"));
        list.add(workingTimeOf(-5, 9, 23, "4H"));
        list.add(workingTimeOf(-6, 9, 23, "4H"));
        list.add(workingTimeOf(-7, 9, 23, "4H"));
        list.add(workingTimeOf(-8, 9, 23, "4H"));
        list.add(workingTimeOf(-4, 9, 23, "4H"));
        list.add(workingTimeOf(-5, 9, 23, "4H"));
        list.add(workingTimeOf(-6, 9, 23, "4H"));
        list.add(workingTimeOf(-7, 9, 23, "4H"));
        list.add(workingTimeOf(-8, 9, 23, "4H"));
        return new WorkingTimesAdapter(getActivity(), R.layout.listview_working_time_item, list);
    }

    private WorkingTIme workingTimeOf(int dDay, int start, int end, String type) {
        WorkingTIme workingTIme = new WorkingTIme();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, cal.get(Calendar.DATE) + dDay);

        workingTIme.date = cal.getTime().getTime();

        cal.set(Calendar.HOUR, start);
        workingTIme.start = cal.getTime().getTime();

        cal.set(Calendar.HOUR, end);
        workingTIme.end = cal.getTime().getTime();

        workingTIme.workingType = type;

        return workingTIme;
    }

    public static class WorkingTIme {
        private long date;
        private long start;
        private long end;
        private String workingType;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onDashboardFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDashboardFragmentInteractionListener) {
            mListener = (OnDashboardFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDashboardFragmentInteractionListener");
        }
        Log.d(TAG, this + ".onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        Log.d(TAG, this + ".onDetach");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, this + ".onDestroy");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, this + ".onResume");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, this + ".onStop");
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
    public interface OnDashboardFragmentInteractionListener {
        // TODO: Update argument type and name
        void onDashboardFragmentInteraction(Uri uri);
    }
}
