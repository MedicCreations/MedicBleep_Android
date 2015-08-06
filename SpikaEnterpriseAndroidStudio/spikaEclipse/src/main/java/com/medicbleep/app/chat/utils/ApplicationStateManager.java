package com.medicbleep.app.chat.utils;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by mislav on 22/04/15.
 */
public class ApplicationStateManager  {

    public static final String APPLICATION_RESUMED = "application_resumed";
    public static final String APPLICATION_PAUSED = "application_paused";

    private LocalBroadcastManager localBroadcastManager;

    private boolean isApplicationActive = false;

    public ApplicationStateManager (Application application) {
        application.registerComponentCallbacks(new ComponentCallbacks2Implementation());
        application.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacksImplementation());
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        application.registerReceiver(new BroadcastReceiverImplementation(), intentFilter);
        localBroadcastManager = LocalBroadcastManager.getInstance(application);
    }

    private void applicationStateChange () {
        Intent intent;
        if (isApplicationActive) {
            intent = new Intent(APPLICATION_RESUMED);
            Log.i("APPLICATION", "***************************** FOREGROUND ****************************");
        }
        else {
            intent = new Intent(APPLICATION_PAUSED);
            Log.i("APPLICATION", "***************************** BACKGROUND ****************************");
        }
        localBroadcastManager.sendBroadcast(intent);
    }

    private class BroadcastReceiverImplementation extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) && isApplicationActive) {
                isApplicationActive = false;
                applicationStateChange();
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON) && !isApplicationActive) {
                isApplicationActive = true;
                applicationStateChange();
            }
        }
    }

    private class ComponentCallbacks2Implementation implements ComponentCallbacks2 {

        @Override
        public void onTrimMemory(int level) {
            if (isApplicationActive && level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
                isApplicationActive = false;
                applicationStateChange();
            }
        }

        @Override
        public void onConfigurationChanged(Configuration newConfig) {

        }

        @Override
        public void onLowMemory() {

        }
    }

    private class ActivityLifecycleCallbacksImplementation implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            if (!isApplicationActive) {
                isApplicationActive = true;
                applicationStateChange();
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.wtf("APPLICATION", "***************************** STATE_MANAGER_KILLED ****************************");
    }
}
