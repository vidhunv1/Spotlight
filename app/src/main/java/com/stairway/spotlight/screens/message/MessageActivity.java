package com.stairway.spotlight.screens.message;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.stairway.data.manager.Logger;
import com.stairway.data.source.message.MessageResult;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.data.manager.XMPPManager;
import com.stairway.spotlight.screens.message.di.MessageModule;
import com.stairway.spotlight.screens.user_profile.UserProfileActivity;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MessageActivity extends BaseActivity implements MessageContract.View {
    @Inject
    public XMPPManager connection;

    @Inject
    MessagePresenter messagePresenter;

    @Bind(R.id.rv_messageitem)
    RecyclerView messageItem;

    @Bind(R.id.et_sendmessage_message)
    EditText messageBox;

    private static String USER_ID = "USERID";
    private String chatId;
    private String currentUser;
    private MessagesAdapter messagesAdapter;


    public static Intent callingIntent(Context context, String userId) {
        Intent intent = new Intent(context, MessageActivity.class);
        intent.putExtra(USER_ID, userId);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent receivedIntent = getIntent();
        if(!receivedIntent.hasExtra(USER_ID))
            return;

        chatId = receivedIntent.getStringExtra(USER_ID);
        setContentView(R.layout.activity_message);
        ButterKnife.bind(this);

//        getWindow().setBackgroundDrawableResource(R.drawable.bg_chat);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messagesAdapter = new MessagesAdapter(this);
        messageItem.setLayoutManager(linearLayoutManager);
        messageItem.setAdapter(messagesAdapter);

        Logger.d("[MessagesActivity]Loading messages");

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
        ab.setCustomView(R.layout.actionbar_message_name);

//        ab.setTitle(Html.fromHtml("<font color='#686868'>  "+chatId+"</font>"));
//        ab.setSubtitle(Html.fromHtml("<font color='#cecece'> Last seen at 3:30 PM</font>"));

        ab.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        View v =getSupportActionBar().getCustomView();
        ImageButton profileDP = (ImageButton) v.findViewById(R.id.actionbar_message_profile);

        ((ImageButton)v.findViewById(R.id.actionbar_message_profile)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d("[MessageActivity]ImageProfile");
                startActivity(UserProfileActivity.callingIntent(getBaseContext(), "12"));
            }
        });

        messagePresenter.attachView(this);
        messagePresenter.loadMessages(chatId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        messagePresenter.attachView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        messagePresenter.detachView();
    }

    @Override
    protected void onStop() {
        super.onStop();
        messagePresenter.detachView();
    }

    @OnClick(R.id.btn_sendMessage_send)
    public void onSendClicked() {
        String message = messageBox.getText().toString().trim();

        if(message.length()>=1) {
            messageBox.setText("");
            Logger.d("chatId: "+chatId+", currentUser: "+currentUser);
            messagePresenter.sendMessage(new MessageResult(chatId, currentUser, message, MessageResult.DeliveryStatus.NOT_SENT));
        }
    }

    @Override
    public void displayMessages(List<MessageResult> messages) {
        Logger.d("Init messages list");
        messagesAdapter.setMessages(messages);
    }

    @Override
    public void addMessageToList(MessageResult message) {
        messagesAdapter.addMessage(message);
        messageItem.scrollToPosition(messagesAdapter.getItemCount()-1);
    }

    @Override
    public void updateDeliveryStatus(MessageResult messageResult) {
        messagesAdapter.updateMessage(messageResult);
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        componentContainer.userSessionComponent().plus(new MessageModule()).inject(this);
        currentUser = componentContainer.userSessionComponent().getUserSession().getChatId();
    }
}