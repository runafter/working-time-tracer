package com.runafter.wtt;

import java.text.Format;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by runaf on 2017-01-14.
 */

public class DateTimeUtils {
    public static Calendar lastDateTimeOfWeek(Calendar calendar) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(calendar.getTime());
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DATE, getLastDayOfWeek(cal) - dayOfWeek);
        return minimumInDate(cal);
    }
    public static Calendar firstDateTimeOfWeek(Calendar calendar) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(calendar.getTime());
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - dayOfWeek);
        return minimumInDate(cal);
    }

    public static int getLastDayOfWeek(Calendar calendar) {
        return (calendar.getFirstDayOfWeek() + 7 - 1) % 7 + 7;
    }

    public static long timeZoneOffset() {
        return timeZoneOffset(Calendar.getInstance());
    }
    public static long timeZoneOffset(Calendar calendar) {
        calendar.set(0, 0, 0, 0,0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static Calendar minimumInDate(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static long hoursToMilliseconds(int hours) {
        return 1000L * 60L * 60L * hours;
    }

    public static String toString(Calendar calendar) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return df.format(calendar.getTime());
    }

    public static int hoursOf(long milliseconds) {
        return (int)(milliseconds / (1000L * 60L * 60L));
    }

    public static String formatElapseTime(long milliseconds) {
        final int seconds = (int)(milliseconds %  60000L) /  1000;
        final int minutes = (int)(milliseconds % 360000L) / 60000;
        final int hours = hoursOf(milliseconds) % 100;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static long minimumInDate(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return minimumInDate(cal).getTimeInMillis();
    }

    public static long nowTime() {
        Calendar cal = Calendar.getInstance();
        return cal.getTimeInMillis();
    }

    public static String formatDate(Calendar calendar) {
        return defaultDateFormat().format(calendar.getTime());
    }

    private static SimpleDateFormat defaultDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd");
    }
    private static SimpleDateFormat defaultTimeFormat() {
        return new SimpleDateFormat("HH:mm:ss");
    }

    public static Calendar parseDateToCalendar(String string) throws ParseException {
        Date date = defaultDateFormat().parse(string);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static Calendar parseTimeToCalendar(String string) throws ParseException {
        Date date = defaultTimeFormat().parse(string);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static String formatTime(Calendar calendar) {
        return defaultTimeFormat().format(calendar.getTime());
    }
}
