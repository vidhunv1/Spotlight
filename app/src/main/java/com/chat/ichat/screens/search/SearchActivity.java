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
import android.widget.EditText;
import android.widget.ImageButton;
import com.chat.ichat.R;
import com.chat.ichat.core.BaseActivity;
import com.chat.ichat.core.Logger;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.db.MessageStore;
import com.chat.ichat.screens.message.MessageActivity;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

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
    private final String SCREEN_NAME = "search";

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        search.requestFocus();

        contactsSearchList.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.ItemAnimator animator = contactsSearchList.getItemAnimator();
        if (animator instanceof SimpleItemAnimator)
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        searchAdapter = new SearchAdapter(this, this);
        contactsSearchList.setAdapter(searchAdapter);
        searchPresenter = new SearchPresenter();

        searchPresenter.attachView(this);
        searchAdapter.initSearch("",new ArrayList<>(), new ArrayList<>(), null);
        searchPresenter.init();

        this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*              Analytics           */
        firebaseAnalytics.setCurrentScreen(this, SCREEN_NAME, null);
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
    }

    @OnClick(R.id.ib_search_clear)
    public void onSearchClear() {
        search.setText("");
    }

    @OnTextChanged(R.id.et_search)
    public void onQueryChanged() {
        if(search.getText().length()>0) {
            clearSearchView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_close_active));
            searchPresenter.searchContacts(search.getText().toString());
        }
        else {
            clearSearchView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_close_inactive));
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
        searchAdapter.displaySearch(searchModel.getSearchTerm(), searchModel.getContactsModelList(), searchModel.getSuggestedModelList(), searchModel.getSearchUser());
    }

    @Override
    public void initSearch(SearchModel searchModel) {
        Logger.d(this, "initSearch");
        if(search.getText().length()==0)
            searchAdapter.initSearch(searchModel.getSearchTerm(), searchModel.getContactsModelList(), searchModel.getSuggestedModelList(), searchModel.getSearchUser());
    }

    @Override
    public void onContactItemClicked(String userName) {
        startActivity(MessageActivity.callingIntent(this, userName));
    }
}