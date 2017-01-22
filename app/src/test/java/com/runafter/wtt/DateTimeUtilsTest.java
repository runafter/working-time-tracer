package com.runafter.wtt;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static com.runafter.wtt.DateTimeUtils.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created by runaf on 2017-01-14.
 */
public class DateTimeUtilsTest {
    @Test
    public void shouldGetLastDateTimeOfWeek() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.SUNDAY);

        calendar = calendarOf(calendar, 2017, 1, 3);
        Calendar expected = minInDate(calendarOf(calendar, 2017, 1, 7));
        assertThat(lastDateTimeOfWeek(calendar), is(expected));

        calendar = calendarOf(calendar, 2017, 1, 1);
        expected = minInDate(calendarOf(calendar, 2017, 1, 7));
        assertThat(lastDateTimeOfWeek(calendar), is(expected));

        calendar = calendarOf(calendar, 2017, 1, 7);
        expected = minInDate(calendarOf(calendar, 2017, 1, 7));
        assertThat(lastDateTimeOfWeek(calendar), is(expected));

        calendar = calendarOf(calendar, 2017, 2, 2);
        expected = minInDate(calendarOf(calendar, 2017, 2, 4));
        assertThat(lastDateTimeOfWeek(calendar), is(expected));
    }

    private Calendar minInDate(Calendar calendar) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(calendar.getTimeInMillis());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    @Test
    public void shouldGetFirstDateTimeOfWeek() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.SUNDAY);

        calendar = calendarOf(calendar, 2017, 1, 3);
        Calendar expected = floorDate(calendarOf(calendar, 2017, 1, 1));
        assertThat(firstDateTimeOfWeek(calendar), is(expected));

        calendar = calendarOf(calendar, 2017, 1, 1);
        expected = floorDate(calendarOf(calendar, 2017, 1, 1));
        assertThat(firstDateTimeOfWeek(calendar), is(expected));

        calendar = calendarOf(calendar, 2017, 1, 7);
        expected = floorDate(calendarOf(calendar, 2017, 1, 1));
        assertThat(firstDateTimeOfWeek(calendar), is(expected));

        calendar = calendarOf(calendar, 2017, 2, 2);
        expected = floorDate(calendarOf(calendar, 2017, 1, 29));
        assertThat(firstDateTimeOfWeek(calendar), is(expected));
    }

    private Calendar floorDate(Calendar calendar) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(calendar.getTimeInMillis());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    private Calendar calendarOf(Calendar base, int year, int month, int date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(base.getTimeInMillis());
        cal.set(year, month - 1, date);
        return cal;
    }

    @Test
    public void shouldGetLastDayOfWeek() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.SUNDAY);

        assertThat(getLastDayOfWeek(calendar), is(Calendar.SATURDAY));
    }

    @Test
    public void shouldTimeZoneOffset9HourInSeoul() throws Exception {
        Calendar calendarGMT = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
        assertThat(timeZoneOffset(calendarGMT) - timeZoneOffset(calendar) , is(1000L * 60 * 60 * 9));
    }

    @Test
    public void shouldCalculateSeconds() {
        assertThat(secondsOf(0 * TIME_1_SECOND), is(0));
        assertThat(secondsOf(60 * TIME_1_SECOND), is(0));
        assertThat(secondsOf(10 * TIME_1_SECOND), is(10));
        assertThat(secondsOf(70 * TIME_1_SECOND), is(10));
        assertThat(secondsOf(210 * TIME_1_SECOND), is(30));
    }
    @Test
    public void shouldCalculateMinutes() {
        assertThat(minutesOf(0 * TIME_1_MINUTE), is(0));
        assertThat(minutesOf(10 * TIME_1_MINUTE), is(10));
        assertThat(minutesOf(60 * TIME_1_MINUTE), is(0));
        assertThat(minutesOf(210 * TIME_1_MINUTE), is(30));
    }
}