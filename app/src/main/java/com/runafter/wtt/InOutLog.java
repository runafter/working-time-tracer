package com.runafter.wtt;

import io.realm.RealmObject;

/**
 * Created by runaf on 2017-01-06.
 */

public class InOutLog extends RealmObject {
    public InOutLog() {
        super();
    }
    public InOutLog(Long time, String type) {
        this();
        this.time = time;
        this.type = type;
    }

    public static InOutLog of(long time, String type) {
        return new InOutLog(time, type);
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
