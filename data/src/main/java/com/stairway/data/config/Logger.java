package com.stairway.data.config;

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
    }

    public static void i(Object o, String message) {
        if (isTimberInitialized)
            Timber.i("["+o.getClass().getSimpleName()+"] "+message);
    }

    public static void d(Object o, String message) {
        if (isTimberInitialized)
            Timber.d("["+o.getClass().getSimpleName()+"] "+message);
    }

    public static void w(Object o, String message) {
        if (isTimberInitialized)
            Timber.w("["+o.getClass().getSimpleName()+"] "+message);
    }

    public static void w(Object o, String message, String desc) {
        if (isTimberInitialized)
            Timber.w("["+o.getClass().getSimpleName()+"] "+message, desc);
    }

    public static void e(Object o, String message) {
        if (isTimberInitialized)
            Timber.e("["+o.getClass().getSimpleName()+"] "+message);
    }

    public static void v(Object o, String message) {
        if (isTimberInitialized)
            Timber.v("["+o.getClass().getSimpleName()+"] "+message);
    }

    public static void v(Object o, String message, Throwable e) {
        if (isTimberInitialized)
            Timber.v("["+o.getClass().getSimpleName()+"] "+message, e);
    }
}
