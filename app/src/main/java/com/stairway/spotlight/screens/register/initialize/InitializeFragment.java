package com.stairway.spotlight.screens.register.initialize;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stairway.spotlight.AccessTokenManager;
import com.stairway.spotlight.R;
import com.stairway.spotlight.api.ApiManager;
import com.stairway.spotlight.api.contacts.ContactsApi;
import com.stairway.spotlight.core.BaseFragment;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.db.ContactStore;
import com.stairway.spotlight.db.ContactsContent;
import com.stairway.spotlight.screens.home.HomeActivity;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by vidhun on 09/12/16.
 */
public class InitializeFragment extends BaseFragment implements InitializeContract.View{

    InitializePresenter initializePresenter;

    @Bind(R.id.tv_fragment_initialize)
    TextView initializeText;

    private ContactsApi contactApi;
    private ContactsContent contactContent;
    private ContactStore contactStore;

    public static InitializeFragment getInstance() {
        InitializeFragment initializeFragment = new InitializeFragment();
        return initializeFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.contactApi = ApiManager.getContactsApi();
        this.contactStore = new ContactStore();
        this.contactContent = new ContactsContent(this.getContext());
        initializePresenter = new InitializePresenter(contactApi, contactContent, contactStore);
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
        initializeText.setText("Initializing...");
        initializePresenter.syncContacts();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void navigateToHome() {
        Logger.d(this, "Navigate to home");
        startActivity(HomeActivity.callingIntent(getActivity()));
        getActivity().finish();
    }
}