package com.stairway.spotlight.screens.search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;

import com.stairway.data.config.Logger;
import com.stairway.data.source.contacts.ContactResult;
import com.stairway.spotlight.models.AccessToken;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.screens.home.ChatListAdapter;
import com.stairway.spotlight.screens.home.ChatListItemModel;
import com.stairway.spotlight.screens.home.HomeActivity;
import com.stairway.spotlight.screens.search.di.SearchViewModule;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchActivity extends BaseActivity implements SearchContract.View,
        SearchAdapter.ContactClickListener, SearchAdapter.MessageClickListener, SearchAdapter.FindContactClickListener,
        ChatListAdapter.ChatClickListener {

    @Bind(R.id.rv_contact_list)
    RecyclerView contactsSearchList;
    @Bind(R.id.tb_search)
    Toolbar toolbar;
    @Bind(R.id.ib_search_clear)
    ImageButton clearSearchView;
    @Inject
    SearchPresenter searchPresenter;

    private EditText searchQuery;

    private AccessToken userSession;
    private SearchAdapter searchAdapter;
    private boolean isAdapterSet = false;
    private static final String KEY_CHATS = "CHATS";

    public static Intent callingIntent(Context context, List<ChatListItemModel> chatListItemModelList) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(KEY_CHATS, (Serializable) chatListItemModelList);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(this, "SearchActivity");
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        Intent i = getIntent();
        List<ChatListItemModel> list = (List<ChatListItemModel>) i.getSerializableExtra(KEY_CHATS);
        ChatListAdapter chatListAdapter = new ChatListAdapter(this, list, this);
        contactsSearchList.setAdapter(chatListAdapter);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        searchQuery = (EditText) toolbar.findViewById(R.id.et_search);
        Activity searchActivity = this;
        searchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()==0) {
                    contactsSearchList.setAdapter(chatListAdapter);
                    clearSearchView.setImageDrawable(ContextCompat.getDrawable(searchActivity, R.drawable.ic_close_inactive));
                    isAdapterSet = false;
                } else {
                    if(!isAdapterSet)
                        contactsSearchList.setAdapter(searchAdapter);
                    searchPresenter.search(s.toString());
                    clearSearchView.setImageDrawable(ContextCompat.getDrawable(searchActivity, R.drawable.ic_close_active));
                    isAdapterSet = true;
                }
            }
        });

        searchPresenter.attachView(this);
        searchAdapter = new SearchAdapter(this, this, this, this);
        contactsSearchList.setLayoutManager(new LinearLayoutManager(this));
        contactsSearchList.setNestedScrollingEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        startActivity(HomeActivity.callingIntent(this));
        this.overridePendingTransition(0,0);
    }

    @OnClick(R.id.ib_search_clear)
    public void onSearchClear() {
        if(searchQuery.getText().length()>0)
            searchQuery.setText("");
        else
            onBackPressed();
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
    public void onChatItemClicked(String userId) {}

    @Override
    public void onFindContactItemClicked(String userName) {
        // get username details from searchAdapter. ie. call presenter.
        searchPresenter.findContact(userName, userSession.getAccessToken());
    }

    @Override
    public void navigateToAddContact(ContactResult contactResult) {
//        startActivity(AddUserActivity.callingIntent(this, contactResult));
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        componentContainer.userSessionComponent().plus(new SearchViewModule()).inject(this);
        userSession = componentContainer.userSessionComponent().getUserSession();
    }
}