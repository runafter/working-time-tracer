package com.runafter.wtt;

import android.app.Activity;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by runaf on 2017-01-17.
 */

@RunWith(AndroidJUnit4.class)
public class RealmAsyncFindTest {
    private static final String TAG = "RAFT";
    @Rule
    public ActivityTestRule<TestActivity> mActivityRule = new ActivityTestRule(TestActivity.class);
    private TestActivity activity;
    private Realm realm;
    private Handler handler;
    private RealmChangeListener<RealmResults<TestRealmObject>> realmChangeListenr;

    @Before
    public void setUp() {
        activity = mActivityRule.getActivity();
        realm = Realm.getInstance(activity.realmConfiguration());
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
    }
    @After
    public void tearDown() {
        realm.close();
    }
    @Test
    public void testInsert() {
        realm.beginTransaction();
        realm.insert(obj("key"));
        realm.commitTransaction();

        assertThat(find("key"), is(obj("key")));
    }

    @Test
    public void testAsyncFind() {
        final CountDownLatch latch = new CountDownLatch(10);

        handler().post(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(activity.realmConfiguration());
                realm.where(TestRealmObject.class).findAllAsync()
                        .addChangeListener(realmChangeListenr(realm, latch));
                realm.close();
            }
        });

        postInsert("k1");
        insert("k2");
        postInsert("k3");
        insert("k4");
        postInsert("k5");

        try {
            latch.await(10L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
        assertThat(latch.getCount(), is(9L));
    }

    @Test
    public void testAsyncFindPermanently() {
        final CountDownLatch latch = new CountDownLatch(10);

        handler().post(new Runnable() {
            @Override
            public void run() {
                findAllAsyncWithListener(latch, 10);
            }
        });

        postInsert("k1");
        postDelayInsert("k2", 1000L);
        postInsert("k3");
        insert("k4");
        postInsert("k5");
        postDelayInsert("k6", 1000L);
        insert("k7");
        postInsert("k5");
        postDelayInsert("k6", 1000L);
        insert("k7");
        postInsert("k5");
        postDelayInsert("k6", 1000L);
        insert("k7");
        postInsert("k5");
        postDelayInsert("k6", 1000L);
        insert("k7");
        postInsert("k5");
        postDelayInsert("k6", 1000L);
        insert("k7");


        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
        assertThat(latch.getCount(), is(4L));
    }

    private void findAllAsyncWithListener(CountDownLatch latch, long invokeCount) {
        Realm realm = Realm.getInstance(activity.realmConfiguration());
        realmChangeListenr = realmChangePermanentListenr(latch, invokeCount);
        realm.where(TestRealmObject.class).findAllAsync()
                .addChangeListener(realmChangeListenr);
        realm.close();
    }

    private RealmChangeListener<RealmResults<TestRealmObject>> realmChangePermanentListenr(final CountDownLatch latch, final long invokeCount) {
        return new RealmChangeListener<RealmResults<TestRealmObject>>() {
            @Override
            public void onChange(RealmResults<TestRealmObject> element) {
                Log.d(TAG, "invokeCount: " + invokeCount + " latch: " + latch.getCount() + " size: " + element.size());
                for (TestRealmObject e : element)
                    Log.d(TAG, "e " + e);
                Log.d(TAG, "-------------------------------------------------------------------------\n");
                latch.countDown();
                //element.removeChangeListener(this);
                //findAllAsyncWithListener(latch, latch.getCount());
            }
        };
    }

    @NonNull
    private RealmChangeListener<RealmResults<TestRealmObject>> realmChangeListenr(final Realm realm, final CountDownLatch latch) {
        return new RealmChangeListener<RealmResults<TestRealmObject>>() {
            @Override
            public void onChange(RealmResults<TestRealmObject> element) {
                latch.countDown();
            }
        };
    }

    private void postDelayInsert(final String key, long delayMilliseconds) {
        handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                insert(key);
            }
        }, delayMilliseconds);
    }

    private void postInsert(final String key) {
        handler().post(new Runnable() {
            @Override
            public void run() {
                insert(key);
            }
        });
    }

    private Handler handler() {
        return activity.handler();
    }

    private void insert(String key) {
        Realm realm = Realm.getInstance(activity.realmConfiguration());
        realm.beginTransaction();
        realm.insertOrUpdate(obj(key));
        realm.commitTransaction();
        realm.close();
    }

    private TestRealmObject find(String key) {
        return realm.where(TestRealmObject.class).equalTo(TestRealmObject.FIELD_KEY, key).findFirst();
    }

    private TestRealmObject obj(String key) {
        return new TestRealmObject(key);
    }


}
