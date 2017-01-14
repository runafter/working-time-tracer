package com.runafter.wtt.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.runafter.wtt.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import io.realm.Realm;
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
    private Realm realm;
    private Handler handler;
    private WorkingTimesAdapter workingTimesAdapter;

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
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, this + ".onCreate");
        this.realm = Realm.getDefaultInstance();
        this.handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment = inflater.inflate(R.layout.fragment_dashboard, container, false);
        this.lvWorkingTimes = (ListView) fragment.findViewById(R.id.list_working_times);
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
        private final long timeZoneOffset;

        public WorkingTimesAdapter(Activity activity, int textViewResourceId, List<WorkingTIme> objects) {
            super(activity, textViewResourceId, objects);
            this.activity = activity;
            this.dateFormat = new SimpleDateFormat("MM-dd");
            this.timeFormat = new SimpleDateFormat("HH:mm");
            Calendar calendar = Calendar.getInstance();
            calendar.set(0, 0, 0, 0,0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.getTimeInMillis();
            this.timeZoneOffset = calendar.getTimeInMillis();
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
            view.start.setText(timeFormat.format(item.start));
            view.end.setText(timeFormat.format(item.end));
            view.workedTime.setText(timeFormat.format(timeZoneOffset + item.end - item.start));
            view.workingTypes.setText(item.workingType);

            applyStyles(rowView, view, item);
            return rowView;
        }

        private void applyStyles(View rowView, ViewHolder view, WorkingTIme item) {
            if ((item.style & WorkingTIme.STYLE_SUNDAY) != 0)
                view.date.setTextColor(Color.RED);
            if ((item.style & WorkingTIme.STYLE_SATURDAY) != 0)
                view.date.setTextColor(Color.BLUE);
            if ((item.style & WorkingTIme.STYLE_WORK_DATE) == 0)
                rowView.setBackgroundColor(Color.LTGRAY);
            if ((item.style & WorkingTIme.STYLE_WEEK_PAST) != 0) {
                view.date.setTextColor(Color.GRAY);
                view.start.setTextColor(Color.GRAY);
                view.end.setTextColor(Color.GRAY);
                view.workedTime.setTextColor(Color.GRAY);
                view.workingTypes.setTextColor(Color.GRAY);
            }
        }

        protected class ViewHolder {
            protected TextView date;
            protected TextView start;
            protected TextView end;
            protected TextView workedTime;
            protected TextView workingTypes;
        }
    }


    private WorkingTimesAdapter workingTImesApdapter() {
        List<WorkingTIme> list = new ArrayList<>();
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
        public static final String WORKING_TYPE_ALL = "8H";
        public static final String WORKING_TYPE_NONE = "0H";
        public static final int STYLE_WORK_DATE = 1 << 1;
        public static final int STYLE_SATURDAY = 1 << 2;
        public static final int STYLE_SUNDAY = 1 << 3;
        public static final int STYLE_HOLIDAY = 1 << 4;
        public static final int STYLE_WEEK_CURRENT = 1 << 5;
        public static final int STYLE_WEEK_PAST = 1 << 6;
        public static final int STYLE_WEEK_FUTURE = 1 << 7;
        public static final int STYLE_TODAY = 1 << 8;
        private long date;
        private long start;
        private long end;
        private String workingType;

        private int style;
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
        this.handler.post(taskSetWorkingTimeAdapter());
    }

    private Runnable taskSetWorkingTimeAdapter() {
        return new Runnable() {
            @Override
            public void run() {
                workingTimesAdapter = workingTImesApdapter();
                lvWorkingTimes.setAdapter(workingTimesAdapter);
                addWorkingTimes(0);
            }
        };
    }

    private void addWorkingTimes(int offsetWeek) {
        new WorkingTimesFetchAyncTask(this.handler, workingTimesAdapter).execute(offsetWeek, 2);
    }

    public static class WorkingTimesFetchAyncTask extends AsyncTask<Integer, Integer, Integer> {
        private final Handler handler;
        private final WorkingTimesAdapter workingTimesAdapter;
        private long today;
        private Calendar firstDateTimeOfThisWeek;
        private Calendar lastDateTimeOfThisWeek;

        public WorkingTimesFetchAyncTask(Handler handler, WorkingTimesAdapter workingTimesAdapter) {
            super();
            this.handler = handler;
            this.workingTimesAdapter = workingTimesAdapter;
        }
        @Override
        protected Integer doInBackground(Integer... params) {
            Calendar calendar = Calendar.getInstance();

            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            this.today = calendar.getTimeInMillis();
            firstDateTimeOfThisWeek = firstDateTimeOfWeek(calendar);
            lastDateTimeOfThisWeek = lastDateTimeOfWeek(calendar);

            int offsetWeek = params[0];
            int weekCount = params.length < 2 || params[1] == null ? 1 : params[1];
            calendar.add(Calendar.WEEK_OF_YEAR, offsetWeek);

            calendar.set(Calendar.DAY_OF_WEEK, getLastDayOfWeek(calendar));

            int count = 0;

            final List<WorkingTIme> workingTimes = new ArrayList<>();
            for (int w = 0 ; w < weekCount ; w++) {
                for (int d = 0; d < 7; d++) {
                    workingTimes.add(workingTimeOf(calendar));
                    calendar.add(Calendar.DATE, -1);
                    count++;
                }
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    workingTimesAdapter.addAll(workingTimes);
                }
            });

            return count;
        }

        private Calendar lastDateTimeOfWeek(Calendar calendar) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(calendar.getTime());
            cal.set(Calendar.DAY_OF_WEEK, getLastDayOfWeek(cal));
            int fields[] = new int[] {Calendar.HOUR, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND};
            for (int field : fields)
                cal.set(field, cal.getMaximum(field));
            return cal;
        }

        private Calendar firstDateTimeOfWeek(Calendar calendar) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(calendar.getTime());
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            return cal;
        }

        private int getLastDayOfWeek(Calendar calendar) {
            return (calendar.getFirstDayOfWeek() + 7 - 1) % 7;
        }

        private WorkingTIme workingTimeOf(Calendar calendar) {
            WorkingTIme workingTIme = new WorkingTIme();
            workingTIme.date = calendar.getTime().getTime();
            workingTIme.style = styleOf(calendar);
            workingTIme.start = workingTIme.date;
            workingTIme.end = workingTIme.date;

            if (isWorkDate(calendar))
                workingTIme.workingType = WorkingTIme.WORKING_TYPE_ALL;
            else
                workingTIme.workingType = WorkingTIme.WORKING_TYPE_NONE;

            return workingTIme;
        }

        private int styleOf(Calendar calendar) {
            boolean workDate = isWorkDate(calendar);
            int style = 0;
            if (workDate)
                style |= WorkingTIme.STYLE_WORK_DATE;
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
                style |= WorkingTIme.STYLE_SATURDAY;
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
                style |= WorkingTIme.STYLE_SUNDAY;
            if (isHoliday(calendar))
                style |= WorkingTIme.STYLE_HOLIDAY;
            if (isCurrentWeek(calendar))
                style |= WorkingTIme.STYLE_WEEK_CURRENT;
            if (isPastWeek(calendar))
                style |= WorkingTIme.STYLE_WEEK_PAST;
            if (isFutureWeek(calendar))
                style |= WorkingTIme.STYLE_WEEK_FUTURE;
            if (isToday(calendar))
                style |= WorkingTIme.STYLE_TODAY;
            return style;
        }

        private boolean isToday(Calendar calendar) {
            return today == calendar.getTimeInMillis();
        }

        private boolean isFutureWeek(Calendar calendar) {
            return calendar.after(lastDateTimeOfThisWeek);
        }

        private boolean isPastWeek(Calendar calendar) {
            return calendar.before(firstDateTimeOfThisWeek);
        }

        private boolean isCurrentWeek(Calendar calendar) {
            long time = calendar.getTimeInMillis();
            return time >= firstDateTimeOfThisWeek.getTimeInMillis() && time <= lastDateTimeOfThisWeek.getTimeInMillis();
        }

        private boolean isHoliday(Calendar calendar) {
            return false;
        }

        private boolean isWorkDate(Calendar calendar) {
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            return dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY;
        }
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
