package com.runafter.wtt;

import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

/**
 * Created by runaf on 2017-01-06.
 */
@Config
public class NotificationListenerServiceTest {
    @Test
    public void test() {
        Robolectric.setupService(MdmNotificationListenerService.class);
    }
}
