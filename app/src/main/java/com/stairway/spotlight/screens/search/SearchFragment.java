package com.stairway.spotlight.screens.search;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseFragment;
import com.stairway.spotlight.core.di.component.ComponentContainer;

import butterknife.ButterKnife;

/**
 * Created by vidhun on 16/12/16.
 */

public class SearchFragment extends BaseFragment {
    public static SearchFragment getInstance() {
        SearchFragment searchFragment = new SearchFragment();
        return searchFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {

    }
}
