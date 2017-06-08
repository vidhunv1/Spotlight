package com.chat.ichat.screens.new_chat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chat.ichat.db.ContactsContent;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.config.AnalyticsContants;
import com.chat.ichat.db.BotDetailsStore;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.R;
import com.chat.ichat.core.BaseActivity;
import com.chat.ichat.core.lib.AndroidUtils;
import com.chat.ichat.screens.message.MessageActivity;

import org.jivesoftware.smack.packet.Presence;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import xyz.danoz.recyclerviewfastscroller.sectionindicator.SectionIndicator;
import xyz.danoz.recyclerviewfastscroller.sectionindicator.title.SectionTitleIndicator;
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

/**
 * Created by vidhun on 08/01/17.
 */
public class NewChatActivity extends BaseActivity implements NewChatContract.View, NewChatAdapter.ContactClickListener{
    @Bind(R.id.rv_contact_list) RecyclerView contactList;
    @Bind(R.id.tb_new_chat) Toolbar toolbar;
    @Bind(R.id.et_new_chat_search1) EditText toolbarSearch;
    @Bind(R.id.tv_new_chat_title) TextView toolbarTitle;
    @Bind(R.id.ll_new_chat) RelativeLayout newChatLayout;
    @Bind(R.id.fast_scroller) VerticalRecyclerViewFastScroller fastScroller;
//    @Bind(R.id.fast_scroller_section_title_indicator) SectionTitleIndicator sectionTitleIndicator;

    NewChatAdapter newChatAdapter;

    final ProgressDialog[] progressDialog = new ProgressDialog[1];

    NewChatPresenter newChatPresenter;

    private static final String KEY_IS_NEW_CHAT = "KEY_IS_NEW_CHAT";

    private FirebaseAnalytics firebaseAnalytics;
    private final String SCREEN_NAME = "new_chat";
    private boolean isNewChat;

    public static Intent callingIntent(Context context, boolean isNewChat) {
        Intent intent = new Intent(context, NewChatActivity.class);
        intent.putExtra(KEY_IS_NEW_CHAT, isNewChat);
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
        fastScroller.setRecyclerView(contactList);
        contactList.setOnScrollListener(fastScroller.getOnScrollListener());
//        fastScroller.setSectionIndicator(sectionTitleIndicator);
        contactList.setLayoutManager(new LinearLayoutManager(this));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        contactList.setLayoutManager(linearLayoutManager);

        RecyclerView.ItemAnimator animator = contactList.getItemAnimator();
        if (animator instanceof SimpleItemAnimator)
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        newChatAdapter = new NewChatAdapter(this, this);
        contactList.setAdapter(newChatAdapter);

        ContactsContent contactsContent = new ContactsContent(this);
        newChatPresenter = new NewChatPresenter(ContactStore.getInstance(), contactsContent, ApiManager.getUserApi(), BotDetailsStore.getInstance(), ApiManager.getBotApi());

        Intent receivedIntent = getIntent();
        if(!receivedIntent.hasExtra(KEY_IS_NEW_CHAT))
            return;

        isNewChat = receivedIntent.getBooleanExtra(KEY_IS_NEW_CHAT, true);
        toolbarSearch.setVisibility(View.GONE);
        toolbarTitle.setVisibility(View.VISIBLE);

        if(isNewChat) {
            toolbarTitle.setText("New Message");
        } else {
            toolbarTitle.setText("Contacts");
        }

        newChatPresenter.attachView(this);
        newChatPresenter.initContactList(!isNewChat);

        this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*              Analytics           */
        firebaseAnalytics.setCurrentScreen(this, SCREEN_NAME, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if((id == android.R.id.home)) {
            finish();
            return true;
        } else if(id == R.id.action_add_contact) {
            final Handler handler = new Handler();
            AndroidUtils.hideSoftInput(this);
            handler.postDelayed(() -> showAddContactPopup(), 250);

            /*              Analytics           */
            firebaseAnalytics.logEvent(AnalyticsContants.Event.MAIN_ADD_CONTACT, null);
        } else if(id == R.id.action_search) {
            toolbarSearch.setVisibility(View.VISIBLE);
            AndroidUtils.showSoftInput(this,toolbarSearch);
            toolbarTitle.setVisibility(View.GONE);

            /*              Analytics           */
            firebaseAnalytics.logEvent(AnalyticsContants.Event.SEARCH, null);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        if(isNewChat)
//            getMenuInflater().inflate(R.menu.new_chat_toolbar, menu);
//        else
            getMenuInflater().inflate(R.menu.contacts_toolbar, menu);
        return true;
    }

    @Override
    public void showError(String title, String message) {
        if(progressDialog[0]!=null && progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage("\n"+message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    public void showContactAddedSuccess(String name, String username, boolean isExistingContact) {
        toolbarSearch.clearFocus();
        AndroidUtils.hideSoftInput(this);

        if(progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }

        String message;
        if(isExistingContact) {
            message = "<b>" + name + "</b> is already in your contacts on iChat.";
        } else {
            message = "<b>" + name + "</b> is added to your contacts on iChat.";

            /*              Analytics           */
            Bundle bundle = new Bundle();
            bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, username);
            firebaseAnalytics.logEvent(AnalyticsContants.Event.ADD_CONTACT_SUCCESS, bundle);
        }

        AlertDialog alertDialog = new AlertDialog.Builder(NewChatActivity.this).create();
        alertDialog.setMessage(Html.fromHtml(message));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();
        newChatPresenter.initContactList(!isNewChat);
    }

    @Override
    public void showInvalidIDError() {
        toolbarSearch.clearFocus();
        AndroidUtils.hideSoftInput(this);

        if(progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }
//        showMessageAlertDialog("Please enter a valid iChat ID.");

        AlertDialog alertDialog = new AlertDialog.Builder(NewChatActivity.this).create();
        alertDialog.setMessage("Please enter a valid iChat ID.");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();

        /*              Analytics           */
        firebaseAnalytics.logEvent(AnalyticsContants.Event.ADD_CONTACT_FAILURE, null);
    }

    private void showAddContactPopup() {
        LinearLayout parent = new LinearLayout(this);

        parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setPadding((int)AndroidUtils.px(24),(int)AndroidUtils.px(8), (int)AndroidUtils.px(24), 0);

        EditText editText = new EditText(this);
        ViewGroup.LayoutParams lparams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(lparams);
        editText.setHint(getResources().getString(R.string.add_contact_hint));
        editText.setHintTextColor(Color.parseColor("#9E9E9E"));
        parent.addView(editText);

        TextView tv = new TextView(this);
        tv.setText(getResources().getString(R.string.add_contact_subtitle));
        tv.setTextColor(ContextCompat.getColor(this, R.color.textColor));
        tv.setTextSize(12);
        parent.addView(tv);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add a contact");

        builder.setPositiveButton("ADD", ((dialog, which) -> {
//            dialog.dismiss();
            if(editText.getText().length()>=1) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(toolbarSearch.getWindowToken(), 0);

                progressDialog[0] = ProgressDialog.show(NewChatActivity.this, "", "Loading. Please wait...", true);
                newChatPresenter.addContact(editText.getText().toString());

                /*              Analytics           */
                Bundle bundle = new Bundle();
                bundle.putString(AnalyticsContants.Param.OTHER_USER_ID, editText.getText().toString());
                firebaseAnalytics.logEvent(AnalyticsContants.Event.ADD_CONTACT_POPUP, bundle);
            }
        }));
        builder.setView(parent);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.setOnDismissListener(dialog -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(toolbarSearch.getWindowToken(), 0);
        });

        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    @Override
    public void onContactItemClicked(String userId) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(toolbarSearch.getWindowToken(), 0);

        this.navigateToMessageActivity(userId);

        /*              Analytics           */
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsContants.Param.OTHER_USER_ID, userId);
        firebaseAnalytics.logEvent(AnalyticsContants.Event.SELECT_CONTACT, bundle);
    }

    @Override
    public void displayContacts(List<NewChatItemModel> newChatItemModel) {
        newChatAdapter.setContactList(newChatItemModel);
    }

    @OnTextChanged(R.id.et_new_chat_search1)
    public void onSearchChanged() {
        newChatAdapter.filterList(toolbarSearch.getText().toString());

        /*              Analytics           */
        Bundle bundle = new Bundle();
        firebaseAnalytics.logEvent(AnalyticsContants.Event.SEARCH, bundle);
    }

    private void navigateToMessageActivity(String username) {
        startActivity(MessageActivity.callingIntent(this, username));
    }

    @Override
    public void onPresenceChanged(String username, Presence.Type type) {
        newChatAdapter.onPresenceChanged(username, type);
    }
}