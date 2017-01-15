package com.runafter.wtt;

import io.realm.RealmObject;

/**
 * Created by runaf on 2017-01-06.
 */

public class InOutLog extends RealmObject {

    public static final String FIELD_TIME = "time";

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

    enum Type {
        IN, OUT
    }

    private String type;
    private Long time;
    private String desc;

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
}
