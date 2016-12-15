package com.stairway.spotlight.screens.contacts;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.stairway.data.config.Logger;
import com.stairway.data.source.contacts.ContactResult;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.screens.contacts.di.ContactsViewModule;
import com.stairway.spotlight.screens.message.MessageActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ContactsActivity extends BaseActivity implements ContactsContract.View,
        ContactsAdapter.ContactClickListener, ContactsAdapter.ContactAddClickListener{
    private ContactsAdapter contactsAdapter;
    private final String TITLE_CONTACTS = "Contacts";

    @Inject
    ContactsPresenter contactsPresenter;

    @Bind(R.id.rv_contact_list)
    RecyclerView contactList;

    public static Intent callingIntent(Context context) {
        return new Intent(context, ContactsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        ButterKnife.bind(this);
        contactsPresenter.attachView(this);
        contactsAdapter = new ContactsAdapter(this, this);
        contactList.setLayoutManager(new LinearLayoutManager(this));
        contactList.setAdapter(contactsAdapter);
        contactsPresenter.loadContacts();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(TITLE_CONTACTS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        contactsPresenter.attachView(this);
    }

    @Override
    public void contactAdded(String userName) {
        contactsAdapter.onContactAdded(userName);
    }

    @Override
    public void showContacts(List<ContactItemModel> contactResultList) {
        contactsAdapter.setContacts(contactResultList);
    }

    @Override
    public void onContactItemClicked(String userName) {
        startActivity(MessageActivity.callingIntent(this, userName));
    }

    @Override
    public void onContactAddClicked(String userName) {
        Logger.d("Add Clicked: "+userName);
        contactsPresenter.addContact(userName);
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        componentContainer.userSessionComponent().plus(new ContactsViewModule()).inject(this);
    }
}
