package com.runafter.wtt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by runaf on 2017-01-06.
 */

public class InOutLog extends RealmObject {

    public static final String FIELD_TIME = "time";
    private static final String TYPE_IN = "IN";
    private static final String TYPE_OUT = "OUT";

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

    public static InOutLog of(long time, String type, String desc) {
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

    private static InOutLog copyOf(InOutLog src) {
        return of(src.getTime(), src.getType(), src.getDesc());
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return "InOutLog{" +
                "time=" + getTime() + ":" + sdf.format(new Date(getTime())) +
                ", type='" + getType() + '\'' +
                ", desc='" + getDesc() + '\'' +
                '}';
    }
}
