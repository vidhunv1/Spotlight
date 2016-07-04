package com.stairway.spotlight.ui;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vidhun on 05/07/16.
 */
public class BaseActivity extends Activity implements BaseFragment.BackHandlerInterface {
    private List<BaseFragment> baseFragmentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}
