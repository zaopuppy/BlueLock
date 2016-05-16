package com.example.zero.androidskeleton;

import com.example.zero.androidskeleton.bt.BtService;
import com.example.zero.androidskeleton.storage.BtDeviceStorage;

/**
 * Created by zero on 2016/4/4.
 */
public class Application extends android.app.Application {

    private static final String TAG = "Application";

    private static class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        private final Thread.UncaughtExceptionHandler mOldHandler;

        MyUncaughtExceptionHandler(Thread.UncaughtExceptionHandler oldHandler) {
            mOldHandler = oldHandler;
        }

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            mOldHandler.uncaughtException(thread, ex);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // setup default uncaught exception handler, so app can send log to me automatically.
        Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(oldHandler));

        BtDeviceStorage.INSTANCE.init(getApplicationContext());
        BtService.INSTANCE.init(getApplicationContext());
    }

}
