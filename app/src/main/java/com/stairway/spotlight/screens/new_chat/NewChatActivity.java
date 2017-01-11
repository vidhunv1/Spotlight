package com.stairway.spotlight.screens.new_chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.screens.home.HomeActivity;
import com.stairway.spotlight.screens.message.MessageActivity;
import com.stairway.spotlight.screens.new_chat.di.NewChatViewModule;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

/**
 * Created by vidhun on 08/01/17.
 */

public class NewChatActivity extends BaseActivity implements NewChatContract.View, NewChatAdapter.ContactClickListener{
    @Bind(R.id.rv_contact_list)
    RecyclerView contactList;

    @Bind(R.id.tb_new_chat)
    Toolbar toolbar;

    @Bind(R.id.et_new_chat_search)
    EditText search;

    @Inject
    NewChatPresenter newChatPresenter;

    @Bind(R.id.tv_new_chat_title)
    TextView title;

    NewChatAdapter newChatAdapter;
    public static Intent callingIntent(Context context) {
        return new Intent(context, NewChatActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        newChatPresenter.attachView(this);
        newChatPresenter.initContactList();

        contactList.setLayoutManager(new LinearLayoutManager(this));
        OverScrollDecoratorHelper.setUpOverScroll(contactList, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        newChatAdapter = new NewChatAdapter(this, new ArrayList<>());
        contactList.setAdapter(newChatAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if((id == android.R.id.home)) {
            super.onBackPressed();
            return true;
        } else if(id == R.id.action_search) {
            title.setVisibility(View.GONE);
            search.setVisibility(View.VISIBLE);
            search.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_chat_activity, menu);
        return true;
    }

    @Override
    public void onContactItemClicked(String userId) {
        startActivity(MessageActivity.callingIntent(this, userId));
    }

    @Override
    public void displayContact(NewChatItemModel newChatItemModel) {
        newChatAdapter.addContact(newChatItemModel);
    }

    @Override
    public void displayContacts(List<NewChatItemModel> newChatItemModel) {
        newChatAdapter.addContacts(newChatItemModel);
    }

    @OnTextChanged(R.id.et_new_chat_search)
    public void onSearchChanged() {
        newChatAdapter.filterList(search.getText().toString());
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        componentContainer.userSessionComponent().plus(new NewChatViewModule()).inject(this);
    }
}
