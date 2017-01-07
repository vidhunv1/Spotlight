package com.stairway.spotlight.screens.search;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;

import com.stairway.data.config.Logger;
import com.stairway.data.source.contacts.ContactResult;
import com.stairway.data.source.user.UserSessionResult;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.screens.add_contact.AddUserActivity;
import com.stairway.spotlight.screens.search.di.SearchViewModule;

import java.util.ArrayList;

import javax.inject.Inject;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;

public class SearchActivity extends BaseActivity implements SearchContract.View,
        SearchAdapter.ContactClickListener, SearchAdapter.MessageClickListener, SearchAdapter.FindContactClickListener {

    @Bind(R.id.actionbar_search)
    EditText searchText;

    @Bind(R.id.rv_contact_list)
    RecyclerView contactsSearchList;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
        getSupportActionBar().setCustomView(R.layout.actionbar_search);
        getSupportActionBar().setElevation(0);

        ButterKnife.bind(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        searchPresenter.attachView(this);
        searchPresenter.attachView(this);
        searchAdapter = new SearchAdapter(this, this, this);
        contactsSearchList.setLayoutManager(new LinearLayoutManager(this));
        contactsSearchList.setNestedScrollingEnabled(false);
        contactsSearchList.setAdapter(searchAdapter);
//        displaySearch(new SearchModel("", new ArrayList<>(0), new ArrayList<>(0)));
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

    @OnTextChanged(R.id.actionbar_search)
    public void onSearchTextChanged() {
        searchPresenter.search(searchText.getText()+"");
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
    public void showFindContactError() {
//        Toast.makeText(this, "No user found", Toast.LENGTH_SHORT);
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getBaseContext());
        builder.setMessage("No User found");
        builder.setCancelable(true);

        builder.show();
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