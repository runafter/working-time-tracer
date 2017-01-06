package com.runafter.wtt;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by runaf on 2017-01-06.
 */

public class WorkingTimeLog extends RealmObject {
    public WorkingTimeLog() {
        super();
    }
    public WorkingTimeLog(Long time, String type) {
        this();
        this.time = time;
        this.type = type;
    }

    public static WorkingTimeLog of(long time, String type) {
        return new WorkingTimeLog(time, type);
    }

    enum Type {
        IN, OUT
    }

    private String type;
    private Long time;

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
}
