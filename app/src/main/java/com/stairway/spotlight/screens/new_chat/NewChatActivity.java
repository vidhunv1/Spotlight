package com.stairway.spotlight.screens.new_chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stairway.spotlight.AccessTokenManager;
import com.stairway.spotlight.api.ApiManager;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.core.lib.ImageUtils;
import com.stairway.spotlight.db.BotDetailsStore;
import com.stairway.spotlight.db.ContactStore;
import com.stairway.spotlight.models.AccessToken;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.lib.AndroidUtils;
import com.stairway.spotlight.models.ContactResult;
import com.stairway.spotlight.screens.message.MessageActivity;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vidhun on 08/01/17.
 */

public class NewChatActivity extends BaseActivity implements NewChatContract.View, NewChatAdapter.ContactClickListener{
    @Bind(R.id.rv_contact_list)
    RecyclerView contactList;

    @Bind(R.id.tb_new_chat)
    Toolbar toolbar;

    @Bind(R.id.et_new_chat_search1)
    EditText toolbarSearch;

    @Bind(R.id.tv_new_chat_title)
    TextView toolbarTitle;

    NewChatAdapter newChatAdapter;

    @Bind(R.id.ll_new_chat)
    LinearLayout newChatLayout;

    private PopupWindow addContactPopupWindow;
    private View addContactPopupView;
    private AccessToken userSession;
    private boolean showSoftInput;

    NewChatPresenter newChatPresenter;

    private static final String KEY_SHOW_SOFT_INPUT = "KEY_SHOW_SOFT_INPUT";

    public static Intent callingIntent(Context context, boolean showSoftInput) {
        Intent intent = new Intent(context, NewChatActivity.class);
        intent.putExtra(KEY_SHOW_SOFT_INPUT, showSoftInput);
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

        contactList.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.ItemAnimator animator = contactList.getItemAnimator();
        if (animator instanceof SimpleItemAnimator)
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        newChatAdapter = new NewChatAdapter(this, this);
        contactList.setAdapter(newChatAdapter);

        userSession = AccessTokenManager.getInstance().load();
        newChatPresenter = new NewChatPresenter(ContactStore.getInstance(), ApiManager.getUserApi(), BotDetailsStore.getInstance(), ApiManager.getBotApi());

        Intent receivedIntent = getIntent();
        if(!receivedIntent.hasExtra(KEY_SHOW_SOFT_INPUT))
            return;

        showSoftInput = receivedIntent.getBooleanExtra(KEY_SHOW_SOFT_INPUT, false);

        if(showSoftInput) {
            toolbarSearch.setVisibility(View.VISIBLE);
            toolbarTitle.setVisibility(View.GONE);
            AndroidUtils.showSoftInput(this, toolbarSearch);
        } else {
            AndroidUtils.hideSoftInput(this);
            toolbarSearch.setVisibility(View.GONE);
            toolbarTitle.setVisibility(View.VISIBLE);
        }

        newChatPresenter.attachView(this);
        newChatPresenter.initContactList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if((id == android.R.id.home)) {
            AndroidUtils.hideSoftInput(this);
            super.onBackPressed();
            return true;
        } else if(id == R.id.action_add_contact) {
            showAddContactPopup();
        } else if(id == R.id.action_search) {
            toolbarSearch.setVisibility(View.VISIBLE);
            AndroidUtils.showSoftInput(this,toolbarSearch);
            toolbarTitle.setVisibility(View.GONE);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(showSoftInput)
            getMenuInflater().inflate(R.menu.new_chat_toolbar, menu);
        else
            getMenuInflater().inflate(R.menu.contacts_toolbar, menu);
        return true;
    }

    public void showContactAddedSuccess(String name, String username, boolean isExistingContact) {
        Logger.d(this, "showContactAddedSuccess:"+addContactPopupWindow.isShowing());
        addContactPopupWindow.dismiss();
        AndroidUtils.hideSoftInput(this);

        String message;
        if(isExistingContact)
            message = name+" is already in your contacts on iChat.";
        else
            message = name+" is added to your contacts on iChat.";

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View addedContactView = inflater.inflate(R.layout.popup_contact_added, null);
        PopupWindow addedPopupWindow = new PopupWindow(
                addedContactView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
        if(Build.VERSION.SDK_INT>=21)
            addedPopupWindow.setElevation(5.0f);

        addedPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        addedPopupWindow.showAtLocation(newChatLayout, Gravity.CENTER,0,0);

        RelativeLayout out = (RelativeLayout) addedContactView.findViewById(R.id.fl_added_contact);
        out.setOnClickListener(view -> addedPopupWindow.dismiss());

        Button sendMessage = (Button) addedContactView.findViewById(R.id.btn_send_message);
        sendMessage.setOnClickListener(v1 -> {
            addedPopupWindow.dismiss();
            this.navigateToMessageActivity(username);
        });

        TextView resultMessage = (TextView) addedContactView.findViewById(R.id.tv_add_result_message);
        resultMessage.setText(message);
        ImageView profileImage = (ImageView) addedContactView.findViewById(R.id.iv_profileImage);
        profileImage.setImageDrawable(ImageUtils.getDefaultProfileImage(name, username, 18));
        newChatPresenter.initContactList();
    }

    @Override
    public void showInvalidIDError() {
        if(addContactPopupWindow.isShowing()) {
            ProgressBar pb = (ProgressBar) addContactPopupView.findViewById(R.id.pb_add_contact);
            pb.setVisibility(View.INVISIBLE);
            showAlertDialog("Please enter a valid iChat ID.", R.layout.alert);
        }
    }

    private void showAddContactPopup() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        addContactPopupView = inflater.inflate(R.layout.popup_add_contact,null);
        addContactPopupWindow = new PopupWindow(
                addContactPopupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
        if(Build.VERSION.SDK_INT>=21)
            addContactPopupWindow.setElevation(5.0f);

        addContactPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        addContactPopupWindow.showAtLocation(newChatLayout, Gravity.CENTER,0,0);

        FrameLayout outLayout = (FrameLayout) addContactPopupView.findViewById(R.id.fl_add_contact);
        outLayout.setOnClickListener(v -> {
            addContactPopupWindow.dismiss();
            AndroidUtils.hideSoftInput(this);
        });

        EditText enterId = (EditText) addContactPopupView.findViewById(R.id.et_add_contact);
        enterId.requestFocus();
        AndroidUtils.showSoftInput(this, enterId);

        Button addButton = (Button) addContactPopupView.findViewById(R.id.btn_add_contact);
        addButton.setOnClickListener(v -> {
            if(enterId.getText().length()>0) {
                ProgressBar pb = (ProgressBar) addContactPopupView.findViewById(R.id.pb_add_contact);
                pb.setVisibility(View.VISIBLE);
                newChatPresenter.addContact(enterId.getText().toString(), userSession.getAccessToken());
            }
        });

        // popup not working in older versions
//        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
//            newChatLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
//                public void onGlobalLayout(){
//                    int heightDiff = newChatLayout.getRootView().getHeight()- newChatLayout.getHeight();
//                    // IF height diff is more then 150, consider keyboard as visible.
//                    Logger.d(this, "DIFF: "+heightDiff);
//                    RelativeLayout content = (RelativeLayout) addContactPopupView.findViewById(R.id.rl_add_contact_content);
//                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)content.getLayoutParams();
//                    if(heightDiff>150) {
//                        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -110, getResources().getDisplayMetrics());
//                        params.setMargins(0, px, 0, 0);
//                        content.setLayoutParams(params);
//                    } else {
//                        params.setMargins(0, 0, 0, 0);
//                        content.setLayoutParams(params);
//                    }
//                }
//            });
    }

    public void showAlertDialog(String message, int layout) {
        //TODO: Something wrong. 16?
        final int WIDTH = 294, HEIGHT = 98;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(layout, null);

        dialogBuilder.setView(dialogView);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, WIDTH+16, getResources().getDisplayMetrics());
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, HEIGHT+16, getResources().getDisplayMetrics());
        alertDialog.getWindow().setLayout(width, height);

        TextView messageText = (TextView) dialogView.findViewById(R.id.tv_alert_message);
        messageText.setText(message);
        Button ok = (Button) dialogView.findViewById(R.id.btn_alert_ok);
        ok.setOnClickListener(v -> alertDialog.dismiss());
    }

    @Override
    public void onContactItemClicked(String userId) {
        AndroidUtils.hideSoftInput(this);
        this.navigateToMessageActivity(userId);
    }

    @Override
    public void displayContacts(List<NewChatItemModel> newChatItemModel) {
        newChatAdapter.setContactList(newChatItemModel);
    }

    @OnTextChanged(R.id.et_new_chat_search1)
    public void onSearchChanged() {
        newChatAdapter.filterList(toolbarSearch.getText().toString());
    }

    private void navigateToMessageActivity(String username) {
        Activity activity = this;
        ContactStore.getInstance().getContactByUserName(username)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ContactResult>() {
                    @Override
                    public void onCompleted() {}
                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(ContactResult contactResult) {
                        Logger.d(this, contactResult.toString());
                        String name = "";
                        if(!contactResult.getContactName().isEmpty())
                            name = contactResult.getContactName();
                        if(!contactResult.getDisplayName().isEmpty())
                            name = contactResult.getDisplayName();
                        startActivity(MessageActivity.callingIntent(activity, username, name));
                    }
                });
    }
}