package com.runafter.wtt;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created by runaf on 2017-01-23.
 */
public class WorkingTimeCalculatorTest {
    private WorkingTimeCalculator calculator;

    @Before
    public void setUp() {
        this.calculator = new WorkingTimeCalculator();
    }

    @Test
    public void shouldNotSubtractWhenWorkedUnder4Hours() {
        assertThat(calculator.dailyWorkingTimeOf(timeOf(0, 0), timeOf(0, 1)), is(elapseOf(0, 1)));
        assertThat(calculator.dailyWorkingTimeOf(timeOf(0, 43), timeOf(4, 42)), is(elapseOf(3, 59)));
    }

    @Test
    public void shouldSubtract30MinutesWhenWorkedUnder8HoursOver4Hours() {
        assertThat(calculator.dailyWorkingTimeOf(timeOf(0, 0), timeOf(4, 0)), is(elapseOf(3, 30)));
        assertThat(calculator.dailyWorkingTimeOf(timeOf(0, 0), timeOf(4, 30)), is(elapseOf(4, 0)));
        assertThat(calculator.dailyWorkingTimeOf(timeOf(0, 0), timeOf(7, 59)), is(elapseOf(7, 29)));
    }

    @Test
    public void shouldSubtract1HourWhenWorkedOver8HoursUnder13Hours() {
        assertThat(calculator.dailyWorkingTimeOf(timeOf(0, 0), timeOf(8, 0)), is(elapseOf(7, 0)));
        assertThat(calculator.dailyWorkingTimeOf(timeOf(0, 0), timeOf(9, 0)), is(elapseOf(8, 0)));
        assertThat(calculator.dailyWorkingTimeOf(timeOf(0, 0), timeOf(10, 0)), is(elapseOf(9, 0)));
        assertThat(calculator.dailyWorkingTimeOf(timeOf(0, 0), timeOf(13, 0)), is(elapseOf(12, 0)));
    }

    @Test
    public void shouldBe12HoursWhenWorkedOver13Hours() {
        assertThat(calculator.dailyWorkingTimeOf(timeOf(0, 0), timeOf(13, 0)), is(elapseOf(12, 0)));
        assertThat(calculator.dailyWorkingTimeOf(timeOf(0, 0), timeOf(14, 0)), is(elapseOf(12, 0)));
    }

    private long elapseOf(int hour, int minute) {
        return DateTimeUtils.TIME_1_HOUR * hour + DateTimeUtils.TIME_1_MINUTE * minute;
    }

    private long timeOf(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}