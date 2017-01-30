package com.stairway.spotlight.core;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.stairway.spotlight.application.SpotlightApplication;
import com.stairway.spotlight.core.di.component.ComponentContainer;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by vidhun on 04/07/16.
 */
public abstract class BaseFragment extends Fragment {
    protected abstract void injectComponent(ComponentContainer componentContainer);
}
