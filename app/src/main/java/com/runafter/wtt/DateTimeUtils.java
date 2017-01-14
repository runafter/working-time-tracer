package com.runafter.wtt;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by runaf on 2017-01-14.
 */

public class DateTimeUtils {
    public static Calendar lastDateTimeOfWeek(Calendar calendar) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(calendar.getTime());
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DATE, getLastDayOfWeek(cal) - dayOfWeek);
        int fields[] = new int[] {Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND};
        for (int field : fields)
            cal.set(field, cal.getMaximum(field));
        return cal;
    }
    public static Calendar firstDateTimeOfWeek(Calendar calendar) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(calendar.getTime());
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - dayOfWeek);
        int fields[] = new int[] {Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND};
        for (int field : fields)
            cal.set(field, 0);
        return cal;
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
}
