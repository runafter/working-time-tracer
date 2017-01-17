package com.runafter.wtt;


import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Created by runaf on 2017-01-14.
 */
@RealmClass
public class WorkingTime extends RealmObject {
    public static final String WORKING_TYPE_ALL = "8H";
    public static final String WORKING_TYPE_HALF = "4H";
    public static final String WORKING_TYPE_NONE = "0H";
    public static final int STYLE_WORK_DATE = 1 << 1;
    public static final int STYLE_SATURDAY = 1 << 2;
    public static final int STYLE_SUNDAY = 1 << 3;
    public static final int STYLE_HOLIDAY = 1 << 4;
    public static final int STYLE_WEEK_CURRENT = 1 << 5;
    public static final int STYLE_WEEK_PAST = 1 << 6;
    public static final int STYLE_WEEK_FUTURE = 1 << 7;
    public static final int STYLE_TODAY = 1 << 8;
    public static final String FIELD_DATE = "date";

    @PrimaryKey
    private long date;
    private long start;
    private long end;
    private long inOffice;
    private long outOffice;
    private String workingType;

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getWorkingType() {
        return workingType;
    }

    public void setWorkingType(String workingType) {
        this.workingType = workingType;
    }

    public long getInOffice() {
        return inOffice;
    }

    public void setInOffice(long inOffice) {
        this.inOffice = inOffice;
    }

    public long getOutOffice() {
        return outOffice;
    }

    public void setOutOffice(long outOffice) {
        this.outOffice = outOffice;
    }

    @Ignore
    private int style;

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof WorkingTime)) return false;

        WorkingTime that = (WorkingTime) o;

        if (getDate() != that.getDate()) return false;
        if (getStart() != that.getStart()) return false;
        if (getEnd() != that.getEnd()) return false;
        if (getInOffice() != that.getInOffice()) return false;
        if (getOutOffice() != that.getOutOffice()) return false;
        return getWorkingType() != null ? getWorkingType().equals(that.getWorkingType()) : that.getWorkingType() == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (getDate() ^ (getDate() >>> 32));
        result = 31 * result + (int) (getStart() ^ (getStart() >>> 32));
        result = 31 * result + (int) (getEnd() ^ (getEnd() >>> 32));
        result = 31 * result + (int) (getInOffice() ^ (getInOffice() >>> 32));
        result = 31 * result + (int) (getOutOffice() ^ (getOutOffice() >>> 32));
        result = 31 * result + (getWorkingType() != null ? getWorkingType().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return "WorkingTime{" +
                "date=" + getDate() + ":" + sdf.format(new Date(getDate())) +
                ", start=" + getStart() + ":" + sdf.format(new Date(getStart())) +
                ", end=" + getEnd() + ":" + sdf.format(new Date(getEnd())) +
                ", inOffice=" + getInOffice() +
                ", outOffice=" + getOutOffice() +
                ", workingType='" + getWorkingType() + '\'' +
                ", style=" + getStyle() +
                '}';
    }

    public int getWorkingTypeAsHours() {
        return hoursValueOf(getWorkingType());
    }

    public static int hoursValueOf(String workingType) {
        if (WORKING_TYPE_ALL.equals(workingType))
            return 8;
        if (WORKING_TYPE_HALF.equals(workingType))
            return 4;
        return 0;
    }

    public static WorkingTime copyOf(WorkingTime src) {
        WorkingTime dst = new WorkingTime();
        dst.setWorkingType(src.getWorkingType());
        dst.setStart(src.getStart());
        dst.setEnd(src.getEnd());
        dst.setDate(src.getDate());
        dst.setInOffice(src.getInOffice());
        dst.setOutOffice(dst.getOutOffice());
        return dst;
    }
}
