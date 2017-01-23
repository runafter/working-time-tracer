package com.runafter.wtt;

import static com.runafter.wtt.DateTimeUtils.truncateMilliseconds;
import static com.runafter.wtt.DateTimeUtils.truncateSeconds;

/**
 * Created by runaf on 2017-01-23.
 */

public class WorkingTimeCalculator {
    static final long MILLISECONDS_OF_30_MINUTES = DateTimeUtils.TIME_1_MINUTE * 30;
    static final long MILLISECONDS_OF_1_HOUR = DateTimeUtils.TIME_1_HOUR;
    static final long MILLISECONDS_OF_4_HOURS = MILLISECONDS_OF_1_HOUR * 4;
    static final long MILLISECONDS_OF_8_HOURS = MILLISECONDS_OF_1_HOUR * 8;
    static final long MILLISECONDS_OF_12_HOURS = MILLISECONDS_OF_1_HOUR * 12;

    public long dailyWorkingTimeOf(long start, long end) {
        if (start == 0 || end == 0 || end <= start)
            return 0;
        long worked = end - start;
        if (worked >= MILLISECONDS_OF_8_HOURS)
            return Math.min(worked - MILLISECONDS_OF_1_HOUR, MILLISECONDS_OF_12_HOURS);
        if (worked >= MILLISECONDS_OF_4_HOURS)
            return worked - MILLISECONDS_OF_30_MINUTES;
        return worked;
    }

    public long getWithSecondsOnlyToday(long today, long date, long workedTime) {
        if (date != today)
            return truncateSeconds(workedTime);
        else
            return truncateMilliseconds(workedTime);
    }
}
