package com.stairway.spotlight.core;

import rx.Scheduler;

/**
 * Created by vidhun on 04/07/16.
 */
public interface BaseView {
    Scheduler getUiScheduler();
}
