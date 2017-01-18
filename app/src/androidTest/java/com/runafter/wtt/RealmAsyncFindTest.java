package com.runafter.wtt;

import android.app.Activity;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
    private RealmChangeListener<RealmResults<WorkingTime>> realmChangeListenr;

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
        realm.insert(obj(10L));
        realm.commitTransaction();

        assertThat(find(10L), is(obj(10L)));
    }

    @Test
    public void testAsyncFind() {
        final CountDownLatch latch = new CountDownLatch(10);

        handler().post(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(activity.realmConfiguration());
                realm.where(WorkingTime.class).findAllAsync()
                        .addChangeListener(realmChangeListenr(realm, latch));
                realm.close();
            }
        });

        postInsert(10L);
        insert(11L);
        postInsert(12L);
        insert(13L);
        postInsert(14L);

        try {
            latch.await(10L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
        assertThat(latch.getCount(), is(9L));
    }

    @Test
    @Ignore
    public void testAsyncFindPermanently() {
        final CountDownLatch latch = new CountDownLatch(10);

        handler().post(new Runnable() {
            @Override
            public void run() {
                findAllAsyncWithListener(latch, 10);
            }
        });

        postInsert(10L);
        postDelayInsert(11L, 1000L);
        postInsert(12L);
        insert(13L);
        postInsert(14L);
        postDelayInsert(15L, 1000L);
        insert(16L);
        postInsert(17L);
        postDelayInsert(18L, 1000L);
        insert(19L);
        postInsert(20L);
        postDelayInsert(21L, 1000L);
        insert(16L);
        postInsert(14L);
        postDelayInsert(11, 1000L);
        insert(18L);
        postInsert(13L);
        postDelayInsert(16L, 1000L);
        insert(20L);


        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
        assertThat(latch.getCount(), is(4L));
    }

    private void findAllAsyncWithListener(CountDownLatch latch, long invokeCount) {
        Realm realm = Realm.getInstance(activity.realmConfiguration());
        realmChangeListenr = realmChangePermanentListenr(latch, invokeCount);
        realm.where(WorkingTime.class).findAllAsync()
                .addChangeListener(realmChangeListenr);
        realm.close();
    }

    private RealmChangeListener<RealmResults<WorkingTime>> realmChangePermanentListenr(final CountDownLatch latch, final long invokeCount) {
        return new RealmChangeListener<RealmResults<WorkingTime>>() {
            @Override
            public void onChange(RealmResults<WorkingTime> element) {
                Log.d(TAG, "invokeCount: " + invokeCount + " latch: " + latch.getCount() + " size: " + element.size());
                for (WorkingTime e : element)
                    Log.d(TAG, "e " + e);
                Log.d(TAG, "-------------------------------------------------------------------------\n");
                latch.countDown();
                //element.removeChangeListener(this);
                //findAllAsyncWithListener(latch, latch.getCount());
            }
        };
    }

    @NonNull
    private RealmChangeListener<RealmResults<WorkingTime>> realmChangeListenr(final Realm realm, final CountDownLatch latch) {
        return new RealmChangeListener<RealmResults<WorkingTime>>() {
            @Override
            public void onChange(RealmResults<WorkingTime> element) {
                latch.countDown();
            }
        };
    }

    private void postDelayInsert(final long key, long delayMilliseconds) {
        handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                insert(key);
            }
        }, delayMilliseconds);
    }

    private void postInsert(final long key) {
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

    private void insert(long key) {
        Realm realm = Realm.getInstance(activity.realmConfiguration());
        realm.beginTransaction();
        realm.insertOrUpdate(obj(key));
        realm.commitTransaction();
        realm.close();
    }

    private WorkingTime find(long key) {
        return realm.where(WorkingTime.class).equalTo(WorkingTime.FIELD_DATE, key).findFirst();
    }

    private WorkingTime obj(long key) {
        WorkingTime workingTime = new WorkingTime();
        workingTime.setDate(key);
        return workingTime;
    }


}
