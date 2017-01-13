package com.stairway.spotlight.screens.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;

import com.stairway.data.config.Logger;
import com.stairway.data.source.contacts.ContactResult;
import com.stairway.data.source.user.UserSessionResult;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.screens.add_user.AddUserActivity;
import com.stairway.spotlight.screens.search.di.SearchViewModule;

import javax.inject.Inject;
import butterknife.Bind;
import butterknife.ButterKnife;

public class SearchActivity extends BaseActivity implements SearchContract.View,
        SearchAdapter.ContactClickListener, SearchAdapter.MessageClickListener, SearchAdapter.FindContactClickListener {

    @Bind(R.id.rv_contact_list)
    RecyclerView contactsSearchList;

    @Bind(R.id.tb_search)
    Toolbar toolbar;

    @Inject
    SearchPresenter searchPresenter;


    private UserSessionResult userSession;

    private SearchAdapter searchAdapter;

    public static Intent callingIntent(Context context) {
        return new Intent(context, SearchActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(this, "SearchActivity");
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        EditText searchQuery = (EditText) toolbar.findViewById(R.id.et_search);
        searchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchPresenter.search(s.toString());
            }

        });

        searchPresenter.attachView(this);
        searchPresenter.attachView(this);
        searchAdapter = new SearchAdapter(this, this, this);
        contactsSearchList.setLayoutManager(new LinearLayoutManager(this));
        contactsSearchList.setNestedScrollingEnabled(false);
        contactsSearchList.setAdapter(searchAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return true;
    }

    @Override
    public void displaySearch(SearchModel searchModel) {
        searchAdapter.displaySearch(searchModel);
    }

    @Override
    public void onContactItemClicked(String userId) {}

    @Override
    public void onMessageItemClicked(String userId) {}

    @Override
    public void onFindContactItemClicked(String userName) {
        // get username details from searchAdapter. ie. call presenter.
        searchPresenter.findContact(userName, userSession.getAccessToken());
    }

    @Override
    public void navigateToAddContact(ContactResult contactResult) {
        startActivity(AddUserActivity.callingIntent(this, contactResult));
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        componentContainer.userSessionComponent().plus(new SearchViewModule()).inject(this);
        userSession = componentContainer.userSessionComponent().getUserSession();
    }
}