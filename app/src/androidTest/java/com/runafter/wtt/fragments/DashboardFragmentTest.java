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
import java.util.List;

import io.realm.Realm;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.runafter.wtt.fragments.utils.Matchers.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

/**
 * Created by runaf on 2017-01-14.
 */
@RunWith(AndroidJUnit4.class)
public class DashboardFragmentTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule(MainActivity.class);
    private Realm realm;

    @Before
    public void setUp() {
        Realm.init(mActivityRule.getActivity());
        this.realm = Realm.getDefaultInstance();
    }
    @After
    public void tearDown() {
        if (this.realm != null)
            this.realm.close();
    }

    @Test
    public void shouldDisplayListWorkingTImesOnStartup() {
        onView(withId(R.id.list_working_times)).check(matches(isDisplayed())).check(matches(withListSizeLeast(5)));
    }

    @Test
    public void shouldDisplayWorkedTimeInList() {
        long lastWeekDayTime = lastWeekDayTime();
        updateWorkingTime(lastWeekDayTime, 0, 11, 1);

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
        long lastWeekDayTime = lastWeekDayTime();
        List<WorkingTime> wts = new ArrayList<>();
        wts.add(updateWorkingTime(lastWeekDayTime, 0, 9, 1));
        wts.add(updateWorkingTime(lastWeekDayTime, -1, 12, 2));
        wts.add(updateWorkingTime(lastWeekDayTime, -2, 15, 3));
        wts.add(updateWorkingTime(lastWeekDayTime, -3, 9, 4));

        int expected = 0;
        for (WorkingTime wt : wts)
            if (wt.getWorkingTypeAsHours() > 0)
                expected += DateTimeUtils.hoursOf(wt.getEnd() - wt.getStart());

        String expectedWorkedTime = String.format("%02d:00:00", expected);

        onView(withId(R.id.worked_time)).check(matches(withText(expectedWorkedTime)));
    }

    private WorkingTime updateWorkingTime(long lastWeekDayTime, int offsetDate, int start, int hours) {
        WorkingTime workingTime = new WorkingTime();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(lastWeekDayTime);

        calendar.add(Calendar.DATE, offsetDate);
        workingTime.setDate(calendar.getTimeInMillis());

        calendar.set(Calendar.HOUR_OF_DAY, start);
        workingTime.setStart(calendar.getTimeInMillis());

        calendar.add(Calendar.HOUR_OF_DAY, hours);
        workingTime.setEnd(calendar.getTimeInMillis());

        WorkingTime old = realm.where(WorkingTime.class).equalTo(WorkingTime.FIELD_DATE, workingTime.getDate()).findFirst();
        if (old != null)
            workingTime.setWorkingType(old.getWorkingType());

        realm.beginTransaction();
        Log.d("TEST", "insertOrUpdate " + toString(workingTime));
        realm.insertOrUpdate(workingTime);
        realm.commitTransaction();
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