package com.stairway.spotlight.screens.add_user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stairway.data.source.contacts.ContactResult;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.screens.add_user.di.AddContactModule;
import com.stairway.spotlight.screens.message.MessageActivity;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddUserActivity extends BaseActivity implements AddUserContract.View{
    @Inject
    AddUserPresenter addUserPresenter;

    @Bind(R.id.tv_contactItem_contactName)
    TextView contactName;

    @Bind(R.id.tv_contactItem_contactId)
    TextView contactId;

    @Bind(R.id.btn_add)
    Button addButton;

    @Bind(R.id.iv_contactItem_profileImage)
    ImageView profileImage;

    @Bind(R.id.tb_add_user)
    Toolbar toolbar;

    @Bind(R.id.ll_contact_content)
    LinearLayout contactContent;

    private ContactResult contact;

    private static String KEY_CONTACT = "CONTACT";

    public static Intent callingIntent(Context context, ContactResult contactResult) {
        Intent intent = new Intent(context, AddUserActivity.class);
        intent.putExtra(KEY_CONTACT, contactResult);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent receivedIntent = getIntent();
        if(!receivedIntent.hasExtra(KEY_CONTACT))
            return;
        contact = (ContactResult) receivedIntent.getSerializableExtra(KEY_CONTACT);
        setContentView(R.layout.activity_add_user);
        ButterKnife.bind(this);

        contactName.setText(contact.getContactName());
        contactId.setText("@"+contact.getUserId());
        profileImage.setImageResource(R.drawable.default_profile_image);

        if(contact.isAdded()) {
            addButton.setVisibility(View.GONE);
            contactContent.setOnClickListener(v -> {
                navigateToMessage(contact.getUsername());
            });
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if((item.getItemId() == android.R.id.home)) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addUserPresenter.attachView(this);
    }

    @Override
    public void navigateToMessage(String userName) {
        startActivity(MessageActivity.callingIntent(this, userName));
    }

    @OnClick(R.id.btn_add)
    public void onAddClicked() {
        addUserPresenter.addContact(contact);
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        componentContainer.userSessionComponent().plus(new AddContactModule()).inject(this);
    }
}