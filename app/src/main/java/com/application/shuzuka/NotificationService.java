package com.application.shuzuka;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by dinesh on 28/3/15.
 */
public class NotificationService extends Service {


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
}
