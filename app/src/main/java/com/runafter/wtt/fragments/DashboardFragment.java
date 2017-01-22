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

import com.runafter.wtt.DateTimeUtils;
import com.runafter.wtt.MainActivity;
import com.runafter.wtt.R;
import com.runafter.wtt.WorkingTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import me.everything.android.ui.overscroll.IOverScrollDecor;
import me.everything.android.ui.overscroll.IOverScrollState;
import me.everything.android.ui.overscroll.ListenerStubs;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

import static com.runafter.wtt.DateTimeUtils.*;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnDashboardFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment {

    private static final String TAG = "WTT";

    private OnDashboardFragmentInteractionListener mListener;
    private ListView lvWorkingTimes;
    private Realm realm;
    private Handler handler;
    private WorkingTimesAdapter workingTimesAdapter;
    private Timer timer;
    private TextView tvRemainTime;
    private TextView tvTargetTime;
    private TextView tvWorkedTime;
    private TextView tvInOfficeTime;
    private TextView tvOutOfficeTime;
    private SimpleDateFormat timeFormat;
    private long timeZoneOffset;
    private Map<Period, RealmResults> workingTimesRealmResults;
    private RealmResults<WorkingTime> resultsThisWeekWorkingTime;

    public DashboardFragment() {
        this.workingTimesRealmResults = new ConcurrentHashMap<>();
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
        Log.d(TAG, "onCreate");

        this.handler = new Handler();
        this.timeFormat = new SimpleDateFormat("HH:mm:ss");
        this.timeZoneOffset = timeZoneOffset();
        Log.d(TAG, "onCreate finish");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment = inflater.inflate(R.layout.fragment_dashboard, container, false);
        this.lvWorkingTimes = (ListView) fragment.findViewById(R.id.list_working_times);
        this.tvRemainTime = (TextView)fragment.findViewById(R.id.remain_time);
        this.tvTargetTime = (TextView)fragment.findViewById(R.id.target_time);
        this.tvWorkedTime = (TextView)fragment.findViewById(R.id.worked_time);
        this.tvInOfficeTime = (TextView)fragment.findViewById(R.id.in_office_time);
        this.tvOutOfficeTime = (TextView)fragment.findViewById(R.id.out_office_time);

        Log.d(TAG, "DashboardFragment.onCreateView");
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
        Log.d(TAG, "DashboardFragment.onCreateView finish");
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


    public class WorkingTimesAdapter extends ArrayAdapter<WorkingTime> {
        private final Activity activity;
        private final SimpleDateFormat dateFormat;
        private final SimpleDateFormat timeFormat;
        private final long timeZoneOffset;
        private Map<Long, WorkingTime> map = new HashMap<>();

        public WorkingTimesAdapter(Activity activity, int textViewResourceId, List<WorkingTime> objects) {
            super(activity, textViewResourceId, objects);
            this.activity = activity;
            this.dateFormat = new SimpleDateFormat("MM-dd");
            this.timeFormat = new SimpleDateFormat("HH:mm");
            this.timeZoneOffset = timeZoneOffset();
        }
        private void debug(String prefix, long  time) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            Log.d(TAG, prefix + " : " + sdf.format(time));
        }

        @Override
        public void add(WorkingTime object) {
            super.add(object);
            map.put(object.getDate(), object);
        }

        @Override
        public void addAll(Collection<? extends WorkingTime> collection) {
            super.addAll(collection);
            for (WorkingTime i : collection)
                map.put(i.getDate(), i);
        }

        @Override
        public void addAll(WorkingTime... items) {
            super.addAll(items);
            for (WorkingTime item : items)
                map.put(item.getDate(), item);
        }

        @Override
        public void insert(WorkingTime object, int index) {
            super.insert(object, index);
            map.put(object.getDate(), object);
        }

        @Override
        public void remove(WorkingTime object) {
            super.remove(object);
            map.remove(object.getDate());
        }

        @Override
        public void clear() {
            super.clear();
            map.clear();
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(TAG, "WorkingTimesAdapter.getView");
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
                view.workingType = (TextView) rowView.findViewById(R.id.tvWorkingTimeTypes);

                rowView.setTag(view);
            } else {
                view = (ViewHolder) rowView.getTag();
            }

            /** Set data to your Views. */
            WorkingTime item = getItem(position);
            //debug("start:", item.getStart());
            String date = dateFormat.format(item.getDate());
            view.date.setText(date);
            String start = item.getStart() > 0 ? timeFormat.format(item.getStart()) : "00:00";
            view.start.setText(start);
            String end = item.getEnd() > 0 ? timeFormat.format(item.getEnd()) : "00:00";
            view.end.setText(end);
            long workedTime = item.getEnd() == 0 || item.getStart() == 0 ? 0 : item.getEnd() - item.getStart();
            String workedTimeString = DateTimeUtils.formatElapseTime(workedTime);
            view.workedTime.setText(workedTimeString);
            view.workingType.setText(item.getWorkingType());

//            Log.d(TAG, "WorkingTimesAdapter.getView date: " + date);
//            Log.d(TAG, "WorkingTimesAdapter.getView start: " + start);
//            Log.d(TAG, "WorkingTimesAdapter.getView end: " + end);
//            Log.d(TAG, "WorkingTimesAdapter.getView workedTime: " + workedTime);
//            Log.d(TAG, "WorkingTimesAdapter.getView workingType: " + item.getWorkingType());

            applyStyles(rowView, view, item);
            Log.d(TAG, "WorkingTimesAdapter.getView finish");
            return rowView;
        }

        private void applyStyles(View rowView, ViewHolder view, WorkingTime item) {
            int style = item.getStyle();
            if ((style & WorkingTime.STYLE_SUNDAY) != 0)
                view.date.setTextColor(Color.RED);
            if ((style & WorkingTime.STYLE_SATURDAY) != 0)
                view.date.setTextColor(Color.BLUE);
            if ((style & WorkingTime.STYLE_WORK_DATE) == 0)
                rowView.setBackgroundColor(Color.LTGRAY);
            if ((style & WorkingTime.STYLE_WEEK_PAST) != 0) {
                view.date.setTextColor(Color.GRAY);
                view.start.setTextColor(Color.GRAY);
                view.end.setTextColor(Color.GRAY);
                view.workedTime.setTextColor(Color.GRAY);
                view.workingType.setTextColor(Color.GRAY);
            }
        }

        public boolean updateWithoutNotify(WorkingTime n) {
            WorkingTime o = this.map.get(n.getDate());
            if (o != null && !o.equals(n)) {
                //logDebugWorkingTimes(n, o);

                o.setInOffice(n.getInOffice());
                o.setOutOffice(n.getOutOffice());
                o.setStart(n.getStart());
                o.setEnd(n.getEnd());
                o.setWorkingType(n.getWorkingType());

                return true;
            } else
                return false;
        }

        protected class ViewHolder {
            protected TextView date;
            protected TextView start;
            protected TextView end;
            protected TextView workedTime;
            protected TextView workingType;
        }
    }

//    private void logDebugWorkingTimes(WorkingTime n, WorkingTime o) {
//        Log.d(TAG, "Date        : " + o.getDate() + " => " + n.getDate());
//        Log.d(TAG, "InOffice    : " + o.getInOffice() + " => " + n.getInOffice());
//        Log.d(TAG, "OutOffice   : " + o.getOutOffice() + " => " + n.getOutOffice());
//        Log.d(TAG, "Start       : " + o.getStart() + " => " + n.getStart());
//        Log.d(TAG, "End         : " + o.getEnd() + " => " + n.getEnd());
//        Log.d(TAG, "WorkingType : " + o.getWorkingType() + " => " + n.getWorkingType());
//    }

    private WorkingTimesAdapter workingTImesApdapter() {
        List<WorkingTime> list = new ArrayList<>();
        return new WorkingTimesAdapter(getActivity(), R.layout.listview_working_time_item, list);
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
        Log.d(TAG, "DashboardFragment.onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        Log.d(TAG, "DashboardFragment.onDetach");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "DashboardFragment.onDestroy");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "DashboardFragment.onResume");
        if (this.realm != null) {
            this.realm.close();
            workingTimesRealmResults.clear();
        }
        this.realm = Realm.getInstance(MainActivity.realmConfiguration());
        this.handler.post(taskSetWorkingTimeAdapter());
        startTimer();
        setUpDashboardUpdater();
        //setUpWorkingTImeListUpdater();
        Log.d(TAG, "DashboardFragment.onResume finish");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "DashboardFragment.onPause");
        if (this.realm != null) {
            workingTimesRealmResults.clear();
            resultsThisWeekWorkingTime = null;
            this.realm.close();
            this.realm = null;
        }
        Log.d(TAG, "DashboardFragment.onPause finish");
    }

    private void setUpDashboardUpdater() {
        Log.d(TAG, "setUpDashboardUpdater");
        Calendar calendar = Calendar.getInstance();
        Calendar to = lastDateTimeOfWeek(calendar);
        Calendar fr = firstDateTimeOfWeek(calendar);
        resultsThisWeekWorkingTime = realm.where(WorkingTime.class)
                .lessThanOrEqualTo(WorkingTime.FIELD_DATE, to.getTimeInMillis())
                .greaterThanOrEqualTo(WorkingTime.FIELD_DATE, fr.getTimeInMillis())
                .findAllAsync();
        final AtomicInteger seq = new AtomicInteger(0);
        RealmChangeListener<RealmResults<WorkingTime>> listener = new RealmChangeListener<RealmResults<WorkingTime>>() {
            @Override
            public void onChange(RealmResults<WorkingTime> result) {
                Log.d(TAG, "setUpDashboardUpdater.resultsThisWeekWorkingTime.onChange");
                int s = seq.getAndIncrement();
                Iterator<WorkingTime> iterator = result.iterator();
                Dashboard dashboard = new Dashboard();
                dashboard.workedTime = 0;
                dashboard.target = 0;
                dashboard.inOfficeTime = 0;
                dashboard.outOfficeTime = 0;

                while (iterator.hasNext()) {
                    WorkingTime workingTime = iterator.next();
                    int hoursWorkingType = WorkingTime.hoursValueOf(workingTime.getWorkingType());
                    if (hoursWorkingType > 0) {
                        long workedTime = workingTime.getEnd() - workingTime.getStart();
                        if (workedTime > 0)
                            dashboard.workedTime += workedTime;
                    }
                    dashboard.target += hoursWorkingType;
                    dashboard.inOfficeTime += workingTime.getInOffice();
                    dashboard.outOfficeTime += workingTime.getOutOffice();
                }

                dashboard.remain = hoursToMilliseconds(dashboard.target) - dashboard.workedTime;
                updateDashboard(dashboard);
                Log.d(TAG, "setUpDashboardUpdater.resultsThisWeekWorkingTime.onChange finish");
            }
        };
        resultsThisWeekWorkingTime.addChangeListener(listener);
        Log.d(TAG, "setUpDashboardUpdater finish");
    }


    private void updateDashboard(final Dashboard dashboard) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "updateDashboard.handler.post.run");
                tvRemainTime.setText(formatElapseTime(dashboard.remain));
                tvTargetTime.setText(formatHour(dashboard.target));
                tvWorkedTime.setText(formatElapseTime(dashboard.workedTime));
                tvInOfficeTime.setText(formatElapseTime(dashboard.inOfficeTime));
                tvOutOfficeTime.setText(formatElapseTime(dashboard.outOfficeTime));

                Log.d(TAG, "updateDashboard.handler.post.run remain: " + dashboard.remain);
                Log.d(TAG, "updateDashboard.handler.post.run target: " + dashboard.target);
                Log.d(TAG, "updateDashboard.handler.post.run workedTime: " + dashboard.workedTime);
                Log.d(TAG, "updateDashboard.handler.post.run inOfficeTime: " + dashboard.inOfficeTime);
                Log.d(TAG, "updateDashboard.handler.post.run outOfficeTime: " + dashboard.outOfficeTime);

                Log.d(TAG, "updateDashboard.handler.post.run finish");
            }
        });

    }

    private String formatHour(int hour) {
        return String.format("%dH", hour);
    }
    private void startTimer() {
        timer = new Timer();
    }
    private void scheduleTimerTask() {
        timer.schedule(timerTask(), 1000);
    }

    private boolean isInOffice() {
        return true;
    }


    @NonNull
    private TimerTask timerTask() {
        return new TimerTask() {

            @Override
            public void run() {
                new UpdateWorkTimeAsyncTask().execute(isInOffice());
                scheduleTimerTask();
            }
        };
    }

    private void stopTimer() {
        timer.cancel();
        timer.purge();
    }

    private Runnable taskSetWorkingTimeAdapter() {
        return new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "taskSetWorkingTimeAdapter.Runnable.run");
                workingTimesAdapter = workingTImesApdapter();
                lvWorkingTimes.setAdapter(workingTimesAdapter);
                addWorkingTimes(0);
                Log.d(TAG, "taskSetWorkingTimeAdapter.Runnable.run finish");
            }
        };
    }

    private void addWorkingTimes(int offsetWeek) {
        new WorkingTimesFetchAsyncTask(workingTimesRealmResults, workingTimesAdapter).execute(offsetWeek, 2);
    }

    public static class Dashboard {
        private long remain;
        private int target;
        private long workedTime;
        private long inOfficeTime;
        private long outOfficeTime;
        private WorkingTime today;
    }

    public static class UpdateWorkTimeAsyncTask extends AsyncTask<Object, Integer, Dashboard> {

        @Override
        protected Dashboard doInBackground(Object... objects) {
            Log.d(TAG, "UpdateWorkTimeAsyncTask.doInBackground");
            Boolean inOffice = (Boolean)objects[0];
            Dashboard dashboard = (Dashboard)objects[1];
            WorkingTime workingTime = dashboard.today;
            long time = Calendar.getInstance().getTimeInMillis();
            if (inOffice != null && inOffice) {
                Realm realm = Realm.getInstance(MainActivity.realmConfiguration());
                workingTime = realm.where(WorkingTime.class).equalTo("date", workingTime.getDate()).findFirst();
                workingTime.setEnd(time);
                realm.close();
            } else {

            }
            Log.d(TAG, "UpdateWorkTimeAsyncTask.doInBackground finish");
            return dashboard;
        }

        @Override
        protected void onPostExecute(Dashboard dashboard) {
            super.onPostExecute(dashboard);
        }
    }

    public static class WorkingTimesFetchAsyncTask extends AsyncTask<Integer, Integer, List<WorkingTime>> {
        private final WorkingTimesAdapter workingTimesAdapter;
        private final Map<Period, RealmResults> workingTimesRealmResults;
        private long today;
        private Calendar firstDateTimeOfThisWeek;
        private Calendar lastDateTimeOfThisWeek;
        private Realm realm;


        public WorkingTimesFetchAsyncTask(Map<Period, RealmResults> workingTimesRealmResults, WorkingTimesAdapter workingTimesAdapter) {
            super();
            this.workingTimesAdapter = workingTimesAdapter;
            this.workingTimesRealmResults = workingTimesRealmResults;
        }

        @Override
        protected void onPostExecute(List<WorkingTime> workingTimes) {
            super.onPostExecute(workingTimes);
            Log.d(TAG, "WorkingTimesFetchAsyncTask.onPostExecute");
            workingTimesAdapter.addAll(workingTimes);
            setUpWorkingTimeAdapterUpdater(workingTimes);
            Log.d(TAG, "WorkingTimesFetchAsyncTask.onPostExecute finish");
        }

        @Override
        protected List<WorkingTime> doInBackground(Integer... params) {
            Log.d(TAG, "WorkingTimesFetchAsyncTask.doInBackground");
            this.realm = Realm.getInstance(MainActivity.realmConfiguration());
            try {
                Calendar calendar = Calendar.getInstance();

                calendar = minimumInDate(calendar);
                this.today = calendar.getTimeInMillis();
                firstDateTimeOfThisWeek = firstDateTimeOfWeek(calendar);
                lastDateTimeOfThisWeek = lastDateTimeOfWeek(calendar);

                int offsetWeek = params[0];
                int weekCount = params.length < 2 || params[1] == null ? 1 : params[1];
                calendar.add(Calendar.WEEK_OF_YEAR, offsetWeek);

                calendar.set(Calendar.DAY_OF_WEEK, getLastDayOfWeek(calendar));

                final List<WorkingTime> workingTimes = new ArrayList<>();
                realm.beginTransaction();
                for (int w = 0; w < weekCount; w++) {
                    for (int d = 0; d < 7; d++) {
                        workingTimes.add(workingTimeOf(calendar));
                        calendar.add(Calendar.DATE, -1);
                    }
                }
                realm.commitTransaction();
                return workingTimes;
            } finally {
                realm.close();
                Log.d(TAG, "WorkingTimesFetchAsyncTask.doInBackground finish");
            }

        }

        private void setUpWorkingTimeAdapterUpdater(List<WorkingTime> workingTimes) {
            Log.d(TAG, "WorkingTimesFetchAsyncTask.setUpWorkingTimeAdapterUpdater");
            int count = workingTimes.size();
            if (count == 0) {
                Log.d(TAG, "WorkingTimesFetchAsyncTask.setUpWorkingTimeAdapterUpdater canceled");
                return;
            }
            WorkingTime first = workingTimes.get(count - 1);
            WorkingTime last = workingTimes.get(0);
            Realm realm = Realm.getInstance(MainActivity.realmConfiguration());
            try {
                RealmResults<WorkingTime> realmResults = realm.where(WorkingTime.class)
                        .greaterThanOrEqualTo("date", first.getDate())
                        .lessThanOrEqualTo("date", last.getDate())
                        .findAllAsync();

                realmResults.addChangeListener(new RealmChangeListener<RealmResults<WorkingTime>>() {
                       @Override
                       public void onChange(RealmResults<WorkingTime> element) {
                           Log.d(TAG, "WorkingTimesFetchAsyncTask.setUpWorkingTimeAdapterUpdater.realm.onChange");
                           Iterator<WorkingTime> iterator = element.iterator();
                           boolean changed = false, c;
                           while (iterator.hasNext()) {
                               WorkingTime e = iterator.next();
                               c = workingTimesAdapter.updateWithoutNotify(e);
                               changed |= c;
                           }
                           if (changed)
                               workingTimesAdapter.notifyDataSetChanged();
                           Log.d(TAG, "WorkingTimesFetchAsyncTask.setUpWorkingTimeAdapterUpdater.realm.onChange finish");
                       }
                   }
                );
                workingTimesRealmResults.put(keyOf(first, last), realmResults);
            } finally {
                realm.close();
                Log.d(TAG, "WorkingTimesFetchAsyncTask.setUpWorkingTimeAdapterUpdater finish");
            }
        }

        private Period keyOf(WorkingTime first, WorkingTime last) {
            return new Period(first.getDate(), last.getDate());
        }

        private WorkingTime workingTimeOf(Calendar calendar) {
            long time = calendar.getTimeInMillis();
            WorkingTime workingTIme = realm.where(WorkingTime.class).equalTo("date", time).findFirst();
            if (workingTIme == null) {
                workingTIme = new WorkingTime();
                workingTIme.setDate(time);
                workingTIme.setStart(0);
                workingTIme.setEnd(0);
                if (isWorkDate(calendar))
                    workingTIme.setWorkingType(WorkingTime.WORKING_TYPE_ALL);
                else
                    workingTIme.setWorkingType(WorkingTime.WORKING_TYPE_NONE);
                realm.insert(workingTIme);
                //debug("db ", calendar);
            } else {
                workingTIme = WorkingTime.copyOf(workingTIme);
                //debug("new ", calendar);
            }
            workingTIme.setStyle(styleOf(calendar));

            return workingTIme;
        }

        private int styleOf(Calendar calendar) {
            boolean workDate = isWorkDate(calendar);
            int style = 0;
            if (workDate)
                style |= WorkingTime.STYLE_WORK_DATE;
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
                style |= WorkingTime.STYLE_SATURDAY;
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
                style |= WorkingTime.STYLE_SUNDAY;
            if (isHoliday(calendar))
                style |= WorkingTime.STYLE_HOLIDAY;
            if (isCurrentWeek(calendar))
                style |= WorkingTime.STYLE_WEEK_CURRENT;
            if (isPastWeek(calendar))
                style |= WorkingTime.STYLE_WEEK_PAST;
            if (isFutureWeek(calendar))
                style |= WorkingTime.STYLE_WEEK_FUTURE;
            if (isToday(calendar))
                style |= WorkingTime.STYLE_TODAY;
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
        Log.d(TAG, "DashboardFragment.onStop");
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

    private static class Period {
        private long startTime;
        private long endTime;

        public Period(long startTime, long endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Period period = (Period) o;

            if (startTime != period.startTime) return false;
            return endTime == period.endTime;

        }

        @Override
        public int hashCode() {
            int result = (int) (startTime ^ (startTime >>> 32));
            result = 31 * result + (int) (endTime ^ (endTime >>> 32));
            return result;
        }
    }
}
