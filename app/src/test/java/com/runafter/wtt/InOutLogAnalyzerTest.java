package com.runafter.wtt;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

import static com.runafter.wtt.WorkingTime.*;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Created by runaf on 2017-01-16.
 */
public class InOutLogAnalyzerTest {

    private InOutLogAnalyzer analyzer;
    private InOutLogRepository mockInOutLogRepo;
    private WorkingTimeRepository workingTimeRepo;

    @Before
    public void setUp() {
        this.mockInOutLogRepo = mock(InOutLogRepository.class);
        this.workingTimeRepo = mock(WorkingTimeRepository.class);
        this.analyzer = spy(new InOutLogAnalyzer(this.mockInOutLogRepo, this.workingTimeRepo));
    }

    @Test
    public void shouldAppliedFromFirstDateInInOutLogs() {
        long from = todayNoon();
        List<InOutLog> logsDesc = logsOf(outLogOf(afterAnHour(from)), inLogOf(from));
        analyzer.analyze(from, logsDesc);

        verify(mockInOutLogRepo).findDesc(dateOf(from), from);
    }

    private long dateOf(long time) {
        return DateTimeUtils.minimumInDate(time);
    }

    @Test
    public void shouldAppliedWhenInOutLogIsEmpty() {
        long from = todayNoon();
        List<InOutLog> logsDesc = Collections.emptyList();

        assertThat(analyzer.analyze(from, logsDesc), is(emptyWorkingTimeCollection()));
        verify(mockInOutLogRepo, never()).findDesc(dateOf(from), from);
    }

    private Collection<WorkingTime> emptyWorkingTimeCollection() {
        return emptyWorkingTimeList();
    }

    @Test
    public void shouldAppliedToWorkingTimesDateFromFirstToLastInInOutLogs() {
        long today = todayNoon();
        long yesterdayNoon = yesterday(today);
        List<InOutLog> logsDesc = logsOf(outLogOf(afterAnHour(today)), inLogOf(today), outLogOf(yesterdayNoon));
//        when(mockInOutLogRepo.findDesc(minimumInDate(yesterdayNoon), today)).thenReturn(logsOf(inLogOf(beforeAnHour(yesterdayNoon))));

        analyzer.analyze(today, logsDesc);

        verify(workingTimeRepo).find(dateOf(yesterdayNoon), dateOf(today));
    }

    @Test
    public void shouldNewWorkingTimesListNotExistsWhenExistInOutLogs() {
        long today = todayNoon();
        long afterAnHourToday = afterAnHour(today);
        long yesterday = yesterday(today);

        List<InOutLog> inOutLogsDesc = logsOf(outLogOf(afterAnHourToday), inLogOf(today), outLogOf(yesterday));

        Collection<WorkingTime> workingTimes = analyzer.appliedWorkingTimes(emptyWorkingTimeList(), inOutLogsDesc);

        assertThat(workingTimes, notNullValue());
        assertThat(workingTimes.size(), is(2));
        assertThat(workingTimes,
                hasItems(
                        workingTimeMatcherOf(dateOf(today), today, afterAnHourToday),
                        workingTimeMatcherOf(dateOf(yesterday))));
    }
    @Test
    public void shouldReturnWorkingTimesWithMinStartMaxEndInInOutLogs() {
        long today = todayNoon();
        long afterAnHourToday = afterAnHour(today);
        long afterTwoHourToday = afterAnHour(afterAnHourToday);
        long afterThreeHourToday = afterAnHour(afterTwoHourToday);

        List<InOutLog> inOutLogsDesc = logsOf(outLogOf(afterThreeHourToday), inLogOf(afterTwoHourToday), outLogOf(afterAnHourToday), inLogOf(today));

        Collection<WorkingTime> workingTimes = analyzer.appliedWorkingTimes(emptyWorkingTimeList(), inOutLogsDesc);

        assertThat(workingTimes, notNullValue());
        assertThat(workingTimes.size(), is(1));
        assertThat(workingTimes, hasItem(workingTimeMatcherOf(dateOf(today), today, afterThreeHourToday)));
    }

    @Test
    public void shouldUpdateToExistedWorkingTime() {
        long today = todayNoon();
        long afterAnHourToday = afterAnHour(today);

        List<InOutLog> inOutLogsDesc = logsOf(outLogOf(afterAnHourToday), inLogOf(today));
        List<WorkingTime> existedWorkingTimes = workingTimesOf(workingTimeOf(dateOf(today), WORKING_TYPE_HALF));
        Collection<WorkingTime> workingTimes = analyzer.appliedWorkingTimes(existedWorkingTimes, inOutLogsDesc);

        assertThat(workingTimes, notNullValue());
        assertThat(workingTimes.size(), is(1));
        assertThat(workingTimes, hasItem(workingTimeMatcherOf(dateOf(today), today, afterAnHourToday, WORKING_TYPE_HALF)));
    }

    private List<WorkingTime> workingTimesOf(WorkingTime... workingTimes) {
        return Arrays.asList(workingTimes);
    }

    private WorkingTime workingTimeOf(long date, String workingType) {
        WorkingTime workingTime = new WorkingTime();
        workingTime.setDate(date);
        workingTime.setWorkingType(workingType);
        return workingTime;
    }
//
//    @Test
//    public void shouldNewWorkingTimesListNotExistsWhenExistInOutLogs() {
//
//    }

    private Matcher<WorkingTime> workingTimeMatcherOf(final long date, final Long start, final Long end, final String workingType) {
        return new TypeSafeDiagnosingMatcher<WorkingTime>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("need WorkngTime with date: ")
                        .appendValue(date);
                if (start != null)
                    description.appendText(", start: ")
                        .appendValue(start);
                if (end != null)
                    description.appendText(", end: ")
                        .appendValue(end);
                if (workingType != null)
                    description.appendText(", workingType: ")
                            .appendValue(workingType);
            }

            @Override
            protected boolean matchesSafely(WorkingTime item, Description mismatchDescription) {
                if (item.getDate() != date
                        || (start != null && !start.equals(item.getStart()))
                        || (end != null && !end.equals(item.getEnd()))
                        || (workingType != null && !workingType.equals(item.getWorkingType()))
                        ) {
                    mismatchDescription
                            .appendText(", but ")
                            .appendValue(item);
                    return false;
                } else
                    return true;
            }
        };
    }
    private Matcher<WorkingTime> workingTimeMatcherOf(final long date, final Long start, final Long end) {
        return workingTimeMatcherOf(date, start, end, null);
    }
    private Matcher<WorkingTime> workingTimeMatcherOf(final long date) {
        return workingTimeMatcherOf(date, null, null, null);
    }

    private long beforeAnHour(long time) {
        Calendar calendar = calendarOf(time);
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        return calendar.getTimeInMillis();
    }

    private Calendar calendarOf(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar;
    }

    private long afterAnHour(long time) {
        Calendar calendar = calendarOf(time);
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        return calendar.getTimeInMillis();
    }

    private long yesterday(long time) {
        Calendar calendar = calendarOf(time);
        calendar.add(Calendar.DATE, -1);
        return calendar.getTimeInMillis();
    }

    private List<WorkingTime> emptyWorkingTimeList() {
        return Collections.emptyList();
    }

    private long todayNoon() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        return calendar.getTimeInMillis();
    }

    private List<InOutLog> logsOf(InOutLog... logs) {
        return Arrays.asList(logs);
    }

    private InOutLog inLogOf(long time) {
        return new InOutLog(time, "IN", "");
    }

    private InOutLog outLogOf(long time) {
        return new InOutLog(time, "OUT", "");
    }
}