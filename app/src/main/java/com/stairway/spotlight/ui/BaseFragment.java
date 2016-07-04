package com.stairway.spotlight.ui;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by vidhun on 04/07/16.
 */
public abstract class BaseFragment extends Fragment {
    protected BackHandlerInterface backHandlerInterface;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
}
