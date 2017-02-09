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

import com.stairway.spotlight.AccessTokenManager;
import com.stairway.spotlight.api.ApiManager;
import com.stairway.spotlight.api.user.UserApi;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.db.ContactStore;
import com.stairway.spotlight.db.MessageStore;
import com.stairway.spotlight.models.AccessToken;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.models.ContactResult;
import com.stairway.spotlight.screens.home.ChatListAdapter;
import com.stairway.spotlight.screens.home.ChatItem;
import com.stairway.spotlight.screens.home.HomeActivity;

import java.io.Serializable;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchActivity extends BaseActivity implements SearchContract.View,
        SearchAdapter.ContactClickListener, SearchAdapter.MessageClickListener,
        ChatListAdapter.ChatClickListener {

    @Bind(R.id.rv_contact_list)
    RecyclerView contactsSearchList;
    @Bind(R.id.tb_search)
    Toolbar toolbar;
    @Bind(R.id.ib_search_clear)
    ImageButton clearSearchView;
    SearchPresenter searchPresenter;

    private EditText searchQuery;

    private AccessToken userSession;
    private SearchAdapter searchAdapter;
    private boolean isAdapterSet = false;
    private static final String KEY_CHATS = "CHATS";

    public static Intent callingIntent(Context context, List<ChatItem> chatListItemModel) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(KEY_CHATS, (Serializable) chatListItemModel);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(this, "SearchActivity");
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        userSession = AccessTokenManager.getInstance().load();
        ContactStore contactStore = new ContactStore();
        MessageStore messageStore = new MessageStore();
        UserApi userApi = ApiManager.getUserApi();
        searchPresenter = new SearchPresenter(new SearchUseCase(contactStore, messageStore));
        Intent i = getIntent();
        List<ChatItem> list = (List<ChatItem>) i.getSerializableExtra(KEY_CHATS);
        ChatListAdapter chatListAdapter = new ChatListAdapter(this, this);
        chatListAdapter.setChatList(list);
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
        searchAdapter = new SearchAdapter(this, this, this);
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
        super.onBackPressed();
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
}