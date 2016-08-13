package com.stairway.spotlight.screens.message;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;

import com.stairway.data.manager.Logger;
import com.stairway.data.source.message.MessageResult;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.data.manager.XMPPManager;
import com.stairway.spotlight.screens.message.di.MessageModule;

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
        setContentView(R.layout.activity_message);
        ButterKnife.bind(this);
        messagesAdapter = new MessagesAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageItem.setLayoutManager(linearLayoutManager);

        messageItem.setAdapter(messagesAdapter);


        Intent receivedIntent = getIntent();
        if(!receivedIntent.hasExtra(USER_ID))
            return;

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        chatId = receivedIntent.getStringExtra(USER_ID);
        ab.setTitle(chatId);

        messagePresenter.attachView(this);
        messagePresenter.loadMessages(chatId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        messagePresenter.attachView(this);
        messagePresenter.receiveMessages();
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
        String message = messageBox.getText().toString();
        messageBox.setText("");
        messagePresenter.sendMessage(new MessageResult(chatId, currentUser, message, MessageResult.DeliveryStatus.NOT_SENT));
    }

    @Override
    public void displayMessages(List<MessageResult> messages) {
        messagesAdapter.setMessages(messages);
        Logger.d("Init messages list");
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
        currentUser = componentContainer.userSessionComponent().getUserSession().getUserId();
    }
}
