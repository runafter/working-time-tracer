package com.runafter.wtt.fragments;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.runafter.wtt.DateTimeUtils;
import com.runafter.wtt.MainActivity;
import com.runafter.wtt.R;
import com.runafter.wtt.WorkingTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import io.realm.Realm;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.runafter.wtt.utils.Matchers.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

/**
 * Created by runafter on 2017-01-14.
 */
@RunWith(AndroidJUnit4.class)
public class DashboardFragmentTest {
    private static final String TAG = "WTTT";
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule(MainActivity.class);
    private Realm realm;

    @Before
    public void setUp() {
        Log.d(TAG, "DashboardFragmentTest.setUp()");
        Realm.init(mActivityRule.getActivity());
        this.realm = Realm.getDefaultInstance();
        Log.d(TAG, "DashboardFragmentTest.setUp() finish");
    }
    @After
    public void tearDown() {
        Log.d(TAG, "DashboardFragmentTest.tearDown()");
        if (this.realm != null) {
            Log.d(TAG, "DashboardFragmentTest.tearDown() deleteAll");
            realm.beginTransaction();
            realm.deleteAll();
            realm.commitTransaction();
            Log.d(TAG, "DashboardFragmentTest.tearDown() deleteAll finish");
            this.realm.close();
        }
        Log.d(TAG, "DashboardFragmentTest.tearDown() finish");
    }

    @Test
    public void shouldDisplayListWorkingTImesOnStartup() {
        Log.d(TAG, "shouldDisplayListWorkingTImesOnStartup");
        onView(withId(R.id.list_working_times)).check(matches(isDisplayed())).check(matches(withListSizeLeast(5)));
    }

    @Test
    public void shouldDisplayWorkedTimeInList() {
        Log.d(TAG, "shouldDisplayWorkedTimeInList");
        onView(withId(R.id.list_working_times)).check(matches(isDisplayed())).check(matches(withListSizeLeast(5)));
        Log.d(TAG, "shouldDisplayWorkedTimeInList waiting finish");

        long lastWeekDayTime = lastWeekDayTime();
        updateWorkingTime(lastWeekDayTime, 0, 11, 1, true);

        onData(is(instanceOf(WorkingTime.class)))
                .inAdapterView(withId(R.id.list_working_times))
                .atPosition(0)
                .check(matches(hasDescendant(
                        allOf(withId(R.id.tvStart), withText("11:00")))))
                .check(matches(hasDescendant(
                        allOf(withId(R.id.tvEnd), withText("12:00")))))
                .check(matches(hasDescendant(
                        allOf(withId(R.id.tvWorkedTime), withText("01:00")))));
    }

    @Test
    public void shouldUpdateDashboardWhenWorkingTimesUpdated() {
        Log.d(TAG, "shouldUpdateDashboardWhenWorkingTimesUpdated");
        onView(withId(R.id.list_working_times)).check(matches(isDisplayed())).check(matches(withListSizeLeast(5)));
        Log.d(TAG, "shouldDisplayWorkedTimeInList waiting finish");

        Calendar calendar = Calendar.getInstance();
        Calendar fr = DateTimeUtils.firstDateTimeOfWeek(calendar);
        Calendar to = DateTimeUtils.lastDateTimeOfWeek(calendar);
        long lastWeekDayTime = to.getTimeInMillis();

        realm.beginTransaction();
        updateWorkingTime(lastWeekDayTime, 0, 9, 1, WorkingTime.WORKING_TYPE_NONE, false);
        updateWorkingTime(lastWeekDayTime, -1, 12, 2, WorkingTime.WORKING_TYPE_HALF, false);
        updateWorkingTime(lastWeekDayTime, -2, 15, 3, false);
        updateWorkingTime(lastWeekDayTime, -3, 9, 4, WorkingTime.WORKING_TYPE_ALL, false);
        realm.commitTransaction();

        Log.d(TAG, "shouldUpdateDashboardWhenWorkingTimesUpdated.updateWorkingTime finished");

        List<WorkingTime> wts = findAll(fr, to);

        int expectedWorkedTime = 0;
        int expectedTarget = 0;
        for (WorkingTime wt : wts) {
            expectedTarget += wt.getWorkingTypeAsHours();
            if (wt.getWorkingTypeAsHours() > 0)
                expectedWorkedTime += DateTimeUtils.hoursOf(wt.getEnd() - wt.getStart());
        }

        onView(withId(R.id.worked_time)).check(matches(withText(String.format("%02d:00:00", expectedWorkedTime))));
        onView(withId(R.id.target_time)).check(matches(withText(String.format("%dH", expectedTarget))));
        onView(withId(R.id.remain_time)).check(matches(withText(String.format("%02d:00:00", expectedTarget - expectedWorkedTime))));
    }

    private List<WorkingTime> findAll(Calendar fr, Calendar to) {
        Iterator<WorkingTime> iterator = realm.where(WorkingTime.class)
                .greaterThanOrEqualTo(WorkingTime.FIELD_DATE, fr.getTimeInMillis())
                .lessThanOrEqualTo(WorkingTime.FIELD_DATE, to.getTimeInMillis())
                .findAll().iterator();

        List<WorkingTime> list = new ArrayList<>();
        while (iterator.hasNext())
            list.add(iterator.next());
        return list;
    }

    private WorkingTime updateWorkingTime(long lastWeekDayTime, int offsetDate, int start, int hours, boolean withTransaction) {
        return updateWorkingTime(lastWeekDayTime, offsetDate, start, hours, null, withTransaction);
    }
    private WorkingTime updateWorkingTime(long lastWeekDayTime, int offsetDate, int start, int hours, String workingType, boolean withTransaction) {
        WorkingTime workingTime = new WorkingTime();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(lastWeekDayTime);
        calendar = DateTimeUtils.minimumInDate(calendar);

        calendar.add(Calendar.DATE, offsetDate);
        workingTime.setDate(calendar.getTimeInMillis());

        calendar.set(Calendar.HOUR_OF_DAY, start);
        workingTime.setStart(calendar.getTimeInMillis());

        calendar.add(Calendar.HOUR_OF_DAY, hours);
        workingTime.setEnd(calendar.getTimeInMillis());

        if (workingType == null) {
            WorkingTime old = realm.where(WorkingTime.class).equalTo(WorkingTime.FIELD_DATE, workingTime.getDate()).findFirst();
            if (old != null)
                workingTime.setWorkingType(old.getWorkingType());
        } else {
            workingTime.setWorkingType(workingType);
        }

        if (withTransaction)
            realm.beginTransaction();
        Log.d(TAG, "insertOrUpdate " + toString(workingTime));
        realm.insertOrUpdate(workingTime);
        if (withTransaction)
            realm.commitTransaction();
        //Log.d(TAG, "thread " + Thread.currentThread().getName());
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
        }
        return workingTime;
    }

    private String toString(WorkingTime workingTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.format(new Date(workingTime.getDate()));
    }

    private long lastWeekDayTime() {
        return DateTimeUtils.minimumInDate(DateTimeUtils.lastDateTimeOfWeek(Calendar.getInstance())).getTimeInMillis();
    }
}