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
    protected BackHandlerInterface backHandlerInterface;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectComponent(((SpotlightApplication) getActivity().getApplication()).getComponentContainer());

        if(!(getActivity() instanceof BackHandlerInterface))
            throw new ClassCastException(getActivity().getLocalClassName()+" must implement BackHandlerInterface");
        else
            backHandlerInterface = (BackHandlerInterface)getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();

        backHandlerInterface.setSelectedFragment(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        backHandlerInterface.removeSelectedFragment(this);
    }

    public Scheduler getUiScheduler() {
        return AndroidSchedulers.mainThread();
    }

    public interface BackHandlerInterface {
        void setSelectedFragment(BaseFragment backHandledFragment);

        void removeSelectedFragment(BaseFragment backHandledFragment);
    }

    protected abstract void injectComponent(ComponentContainer componentContainer);
}
