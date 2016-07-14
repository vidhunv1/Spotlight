package com.stairway.spotlight.screens;

import android.app.Fragment;
import android.os.Bundle;

import com.stairway.spotlight.SpotlightApplication;
import com.stairway.spotlight.internal.di.component.ComponentContainer;

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

    public interface BackHandlerInterface {
        void setSelectedFragment(BaseFragment backHandledFragment);

        void removeSelectedFragment(BaseFragment backHandledFragment);
    }

    protected abstract void injectComponent(ComponentContainer componentContainer);
}
