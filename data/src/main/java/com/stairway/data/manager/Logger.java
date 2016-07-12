package com.stairway.data.manager;

import timber.log.Timber;

/**
 * Created by vidhun on 05/07/16.
 */

public class Logger {
    private static boolean isTimberInitialized = false;

    public static void init() {

        initTimber();
    }

    private static void initTimber() {
        Timber.plant(new Timber.DebugTree());
        isTimberInitialized = true;
        i("[ APPLICATION STARTED ]");
    }

    public static void i(String message) {
        if (isTimberInitialized)
            Timber.i(message);
    }

    public static void d(String message) {
        if (isTimberInitialized)
            Timber.d(message);
    }

    public static void w(String message) {
        if (isTimberInitialized)
            Timber.w(message);
    }

    public static void w(String message, String desc) {
        if (isTimberInitialized)
            Timber.w(message, desc);
    }

    public static void e(String message) {
        if (isTimberInitialized)
            Timber.e(message);
    }

    public static void v(String message) {
        if (isTimberInitialized)
            Timber.v(message);
    }

    public static void v(String message, Throwable e) {
        if (isTimberInitialized)
            Timber.v(message, e);
    }
}
