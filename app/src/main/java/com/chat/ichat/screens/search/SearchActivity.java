package com.chat.ichat.screens.search;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.chat.ichat.MessageController;
import com.chat.ichat.R;
import com.chat.ichat.config.AnalyticsConstants;
import com.chat.ichat.core.BaseActivity;
import com.chat.ichat.core.Logger;
import com.chat.ichat.screens.home.ChatItem;
import com.chat.ichat.screens.message.MessageActivity;
import com.google.firebase.analytics.FirebaseAnalytics;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.Subscriber;

public class SearchActivity extends BaseActivity implements SearchContract.View, SearchAdapter.ContactClickListener {

    @Bind(R.id.rv_contact_list)
    RecyclerView contactsSearchList;
    @Bind(R.id.tb_search)
    Toolbar toolbar;
    @Bind(R.id.ib_search_clear)
    ImageButton clearSearchView;
    @Bind(R.id.et_search)
    EditText search;

    SearchPresenter searchPresenter;

    private SearchAdapter searchAdapter;

    private FirebaseAnalytics firebaseAnalytics;

    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(this, "SearchActivity");
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        search.requestFocus();

        contactsSearchList.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.ItemAnimator animator = contactsSearchList.getItemAnimator();
        if (animator instanceof SimpleItemAnimator)
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);

        SearchActivity searchActivity = SearchActivity.this;
        MessageController.getInstance().getChatList()
                .subscribe(new Subscriber<List<ChatItem>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(List<ChatItem> chatItems) {
                        searchAdapter = new SearchAdapter(searchActivity, searchActivity, chatItems);
                        contactsSearchList.setAdapter(searchAdapter);
                        searchAdapter.initSearch("",new ArrayList<>(), new ArrayList<>(), null);
                    }
                });
        searchPresenter = new SearchPresenter();

        searchPresenter.attachView(this);
        searchPresenter.init();

        this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*              Analytics           */
        firebaseAnalytics.setCurrentScreen(this, AnalyticsConstants.Event.SEARCH_SCREEN, null);
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
        firebaseAnalytics.logEvent(AnalyticsConstants.Event.SEARCH_BACK, null);
        super.onBackPressed();
    }

    @OnClick(R.id.ib_search_clear)
    public void onSearchClear() {
        firebaseAnalytics.logEvent(AnalyticsConstants.Event.SEARCH_CLEAR, null);
        search.setText("");
    }

    @OnTextChanged(R.id.et_search)
    public void onQueryChanged() {
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsConstants.Param.TEXT, search.getText().toString());
        firebaseAnalytics.logEvent(AnalyticsConstants.Event.SEARCH_QUERY, null);
        if(search.getText().length()>0) {
            clearSearchView.setVisibility(View.VISIBLE);
            clearSearchView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_close_active));
            searchPresenter.searchContacts(search.getText().toString());
        } else {
            clearSearchView.setVisibility(View.GONE);
            searchPresenter.init();
        }
    }

    @Override
    public void showError(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage("\n"+message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    @Override
    public void displaySearch(SearchModel searchModel) {
        Logger.d(this, "displaySearch");
        if(searchAdapter!=null)
            searchAdapter.displaySearch(searchModel.getSearchTerm(), searchModel.getContactsModelList(), searchModel.getSuggestedModelList(), searchModel.getSearchUser());
    }

    @Override
    public void initSearch(SearchModel searchModel) {
        Logger.d(this, "initSearch");
        if(search.getText().length()==0 && searchAdapter!=null)
            searchAdapter.initSearch(searchModel.getSearchTerm(), searchModel.getContactsModelList(), searchModel.getSuggestedModelList(), searchModel.getSearchUser());
    }

    @Override
    public void onContactItemClicked(String userName, int from) {
        Logger.d(this, "onContactItemClicked: "+userName);
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_NAME, userName);
        if(from == 0) {
            firebaseAnalytics.logEvent(AnalyticsConstants.Event.SEARCH_CONTACT_CHAT_OPEN, bundle);
        } else if(from == 1) {
            firebaseAnalytics.logEvent(AnalyticsConstants.Event.SEARCH_USERNAME_CHAT_OPEN, bundle);
        } else if(from == 2) {
            firebaseAnalytics.logEvent(AnalyticsConstants.Event.SEARCH_SUGGESTED_CHAT_OPEN, bundle);
        }
        startActivity(MessageActivity.callingIntent(this, userName));
    }

    @OnClick(R.id.iv_back)
    public void onBackClicked() {
        super.onBackPressed();
    }
}