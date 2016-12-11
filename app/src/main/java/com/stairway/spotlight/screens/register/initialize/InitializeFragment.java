package com.stairway.spotlight.screens.register.initialize;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stairway.data.source.user.UserSessionResult;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseFragment;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.screens.home.HomeActivity;
import com.stairway.spotlight.screens.register.initialize.di.InitializeViewModule;

import javax.inject.Inject;
import butterknife.ButterKnife;

/**
 * Created by vidhun on 09/12/16.
 */
public class InitializeFragment extends BaseFragment implements InitializeContract.View{
    @Inject
    InitializePresenter initializePresenter;

    UserSessionResult userSession;

    public static InitializeFragment getInstance() {
        InitializeFragment initializeFragment = new InitializeFragment();
        return initializeFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_initialize, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        initializePresenter.attachView(this);
        initializePresenter.syncContacts(userSession.getAccessToken());
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void navigateToHome() {
        startActivity(HomeActivity.callingIntent(getActivity()));
        getActivity().finish();
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        componentContainer.userSessionComponent().plus(new InitializeViewModule()).inject(this);
        userSession = componentContainer.userSessionComponent().getUserSession();
    }
}