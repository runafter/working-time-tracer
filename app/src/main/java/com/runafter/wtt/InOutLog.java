package com.runafter.wtt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import rx.internal.util.InternalObservableUtils;

/**
 * Created by runaf on 2017-01-06.
 */
@RealmClass
public class InOutLog extends RealmObject {

    public static final String FIELD_TIME = "time";
    public static final String TYPE_IN = "IN";
    public static final String TYPE_OUT = "OUT";
    public static final String TYPE_UNKNOWN = "UNKNOWN";
    public static final InOutLog NULL = new InOutLog();
    static {
        NULL.setTime(0L);
        NULL.setType(TYPE_UNKNOWN);
        NULL.setDesc("unexpected log");
    }

    @PrimaryKey
    private Long time;
    private String type;
    private String desc;


    public InOutLog() {
        super();
    }
    public InOutLog(Long time, String type, String desc) {
        this();
        this.time = time;
        this.type = type;
        this.desc = desc;
    }

    public static InOutLog of(Long time, String type, String desc) {
        return new InOutLog(time, type, desc);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isInLog() {
        return TYPE_IN.equals(getType());
    }
    public boolean isOutLog() {
        return TYPE_OUT.equals(getType());
    }

    public static List<InOutLog> copyOf(List<InOutLog> src) {
        List<InOutLog> dst = new ArrayList<>(src.size());
        for (InOutLog s : src)
            dst.add(copyOf(s));
        return dst;
    }

    public static InOutLog copyOf(InOutLog src) {
        return of(src.getTime(), src.getType(), src.getDesc());
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Long time = getTime();
        return "InOutLog{" +
                "time=" + time + ":" + time != null ? sdf.format(new Date(time)) : "null" +
                ", type='" + getType() + '\'' +
                ", desc='" + getDesc() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !getClass().isInstance(o) ) return false;

        InOutLog inOutLog = (InOutLog) o;

        if (getTime() != null ? !getTime().equals(inOutLog.getTime()) : inOutLog.getTime() != null) return false;
        if (getType() != null ? !getType().equals(inOutLog.getType()) : inOutLog.getType() != null) return false;
        return getDesc() != null ? getDesc().equals(inOutLog.getDesc()) : inOutLog.getDesc() == null;

    }

    @Override
    public int hashCode() {
        int result = getTime() != null ? getTime().hashCode() : 0;
        result = 31 * result + (getType() != null ? getType().hashCode() : 0);
        result = 31 * result + (getDesc() != null ? getDesc().hashCode() : 0);
        return result;
    }
}
