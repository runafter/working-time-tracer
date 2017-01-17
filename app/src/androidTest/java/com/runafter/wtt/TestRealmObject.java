package com.runafter.wtt;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by runaf on 2017-01-17.
 */

public class TestRealmObject extends RealmObject {
    public static final String FIELD_KEY = "string";
    private Integer i;
    private Long l;
    @PrimaryKey
    private String string;

    public TestRealmObject() {
    }

    public TestRealmObject(Integer i, Long l, String string) {
        this();
        this.i = i;
        this.l = l;
        this.string = string;
    }

    public TestRealmObject(String key) {
        this.string = key;
    }

    public Integer getI() {
        return i;
    }

    public void setI(Integer i) {
        this.i = i;
    }

    public Long getL() {
        return l;
    }

    public void setL(Long l) {
        this.l = l;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass().isInstance(o)) return false;

        TestRealmObject that = (TestRealmObject) o;

        if (i != null ? !i.equals(that.i) : that.i != null) return false;
        if (l != null ? !l.equals(that.l) : that.l != null) return false;
        return string != null ? string.equals(that.string) : that.string == null;

    }

    @Override
    public int hashCode() {
        int result = i != null ? i.hashCode() : 0;
        result = 31 * result + (l != null ? l.hashCode() : 0);
        result = 31 * result + (string != null ? string.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TestRealmObject{" +
                "i=" + i +
                ", l=" + l +
                ", string='" + string + '\'' +
                '}';
    }
}
