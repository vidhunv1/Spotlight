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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.core.lib.AndroidUtils;
import com.stairway.spotlight.screens.message.MessageActivity;
import com.stairway.spotlight.screens.new_chat.di.NewChatViewModule;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * Created by vidhun on 08/01/17.
 */

public class NewChatActivity extends BaseActivity implements NewChatContract.View, NewChatAdapter.ContactClickListener{
    @Bind(R.id.rv_contact_list)
    RecyclerView contactList;

    @Bind(R.id.tb_new_chat)
    Toolbar toolbar;

    @Bind(R.id.et_new_chat_search1)
    EditText search;

    @Inject
    NewChatPresenter newChatPresenter;

    NewChatAdapter newChatAdapter;
    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, NewChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        return intent;
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
//        OverScrollDecoratorHelper.setUpOverScroll(contactList, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if((id == android.R.id.home)) {
            AndroidUtils.hideSoftInput(this);
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_chat_toolbar, menu);
        return true;
    }

    @Override
    public void onContactItemClicked(String userId) {
        startActivity(MessageActivity.callingIntent(this, userId));
    }

    @Override
    public void displayContacts(List<NewChatItemModel> newChatItemModel) {
        newChatAdapter = new NewChatAdapter(this, this, newChatItemModel);
        contactList.setAdapter(newChatAdapter);
    }

    @OnTextChanged(R.id.et_new_chat_search1)
    public void onSearchChanged() {
        newChatAdapter.filterList(search.getText().toString());
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        componentContainer.userSessionComponent().plus(new NewChatViewModule()).inject(this);
    }
}
