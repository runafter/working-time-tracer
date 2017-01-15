package com.runafter.wtt.utils;

import android.view.View;
import android.widget.ListView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Created by runaf on 2017-01-14.
 */

public class Matchers {
    public static Matcher<View> withListSize (final int size) {
        return new TypeSafeMatcher<View>() {
            @Override public boolean matchesSafely (final View view) {
                return ((ListView) view).getCount () == size;
            }

            @Override
            public void describeTo (final Description description) {
                description.appendText ("ListView should have " + size + " items");
            }
        };
    }
    public static Matcher<View> withListSizeLeast (final int leastSize) {
        return new TypeSafeMatcher<View>() {
            @Override public boolean matchesSafely (final View view) {
                return ((ListView) view).getCount () >= leastSize;
            }

            @Override
            public void describeTo (final Description description) {
                description.appendText ("ListView should have at least " + leastSize + " items");
            }
        };
    }
}
