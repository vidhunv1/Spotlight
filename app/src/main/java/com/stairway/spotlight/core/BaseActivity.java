package com.stairway.spotlight.core;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.stairway.spotlight.SpotlightApplication;
import com.stairway.spotlight.core.di.component.ComponentContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vidhun on 05/07/16.
 */
public abstract class BaseActivity extends AppCompatActivity implements BaseFragment.BackHandlerInterface {
    private List<BaseFragment> baseFragmentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectComponent(((SpotlightApplication) getApplication()).getComponentContainer());
    }

    @Override
    public void removeSelectedFragment(BaseFragment backHandledFragment) {
        baseFragmentList.remove(backHandledFragment);
    }

    @Override
    public void setSelectedFragment(BaseFragment backHandledFragment) {
        baseFragmentList.add(backHandledFragment);
    }

    private BaseFragment getCurrentFragment() {
        int size = baseFragmentList.size();
        if (size > 0)
            return baseFragmentList.get(size - 1);
        return null;
    }

    protected abstract void injectComponent(ComponentContainer componentContainer);
}
