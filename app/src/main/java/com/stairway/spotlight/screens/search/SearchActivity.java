package com.stairway.spotlight.screens.search;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import com.stairway.data.config.Logger;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.screens.search.di.SearchViewModule;
import java.util.List;
import javax.inject.Inject;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;

public class SearchActivity extends BaseActivity implements SearchContract.View, ContactSearchAdapter.ContactClickListener {
    @Bind(R.id.actionbar_search)
    EditText searchText;

    @Bind(R.id.rv_contact_list)
    RecyclerView contactsSearchList;

//    RecyclerView messagesSearchList;
//    @Bind(R.id.rv_chat_list)

    @Inject
    SearchPresenter searchPresenter;

    private ContactSearchAdapter contactSearchAdapter;

    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, SearchActivity.class);
        return intent;
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
//        messagesSearchList.setLayoutManager(new LinearLayoutManager(this));
        contactSearchAdapter = new ContactSearchAdapter(this);
        contactsSearchList.setLayoutManager(new LinearLayoutManager(this));
        contactsSearchList.setNestedScrollingEnabled(false);
        contactsSearchList.setAdapter(contactSearchAdapter);
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
    public void displayContacts(String searchQuery, List<ContactsModel> contactsModels) {
        Logger.d(this, "DisplayContacts: "+searchQuery+", "+contactsModels.size());
        contactSearchAdapter.setContacts(searchQuery, contactsModels);
    }

    @Override
    public void displayMessages(String searchQuery, List<MessagesModel> messagesModels) {}

    @OnTextChanged(R.id.actionbar_search)
    public void onSearchTextChanged() {
        Logger.d(this, "Seach: "+searchText.getText());
        searchPresenter.searchContacts(searchText.getText()+"");
    }

    @Override
    public void onContactItemClicked(String userId) {

    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        componentContainer.userSessionComponent().plus(new SearchViewModule()).inject(this);
    }
}