package com.stairway.spotlight.screens.message;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.stairway.data.config.Logger;
import com.stairway.data.source.message.MessageResult;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.data.config.XMPPManager;
import com.stairway.spotlight.screens.home.HomeActivity;
import com.stairway.spotlight.screens.message.di.MessageModule;
import com.stairway.spotlight.screens.message.view_models.TextMessage;
import com.stairway.spotlight.screens.user_profile.UserProfileActivity;
import com.stairway.spotlight.screens.web_view.WebViewActivity;

import org.jivesoftware.smackx.chatstates.ChatState;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class MessageActivity extends BaseActivity
        implements MessageContract.View, MessagesAdapter.PostbackClickListener, MessagesAdapter.UrlClickListener, QuickRepliesAdapter.QuickReplyClickListener{
    @Inject
    public XMPPManager connection;

    @Inject
    MessagePresenter messagePresenter;

    @Bind(R.id.rv_messageitem)
    RecyclerView messageItem;

    @Bind(R.id.et_sendmessage_message)
    EditText messageBox;

    @Bind(R.id.btn_sendMessage_send)
    ImageButton sendImageButton;

    @Bind(R.id.tb_message)
    Toolbar toolbar;

    @Bind(R.id.tb_message_title)
    TextView title;

    private ChatState currentChatState;

    private static final String KEY_USER_NAME = "USERNAME";
    private String chatId; // contact user, mobile
    private String currentUser; // this user, mobile
    private MessagesAdapter messagesAdapter;

    // userName: id for ejabberd xmpp. userId: id set by user:
    public static Intent callingIntent(Context context, String userName) {
        Intent intent = new Intent(context, MessageActivity.class);
        intent.putExtra(KEY_USER_NAME, userName);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentChatState = ChatState.inactive;
        Intent receivedIntent = getIntent();
        if(!receivedIntent.hasExtra(KEY_USER_NAME))
            return;

        chatId = receivedIntent.getStringExtra(KEY_USER_NAME);
        setContentView(R.layout.activity_message);
        ButterKnife.bind(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messagesAdapter = new MessagesAdapter(this, this, this, this);
        messageItem.setLayoutManager(linearLayoutManager);
        messageItem.setAdapter(messagesAdapter);
        OverScrollDecoratorHelper.setUpOverScroll(messageItem, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        messagePresenter.attachView(this);
        Logger.d(this, "ChatId: "+chatId+", CurrentUser:"+currentUser);
        messagePresenter.getName(chatId);
        messagePresenter.loadMessages(chatId);
    }

    @Override
        protected void onResume() {
        super.onResume();
        messagePresenter.attachView(this);
        messagePresenter.getPresence(chatId);
        messagePresenter.sendReadReceipt(chatId);
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

//    @Override
//    public void onBackPressed() {
//        if(this.isTaskRoot())
//            super.onBackPressed();
//        else
//            startActivity(HomeActivity.callingIntent(this));
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.messages_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if((item.getItemId() == android.R.id.home)) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    // send text message
    @OnClick(R.id.btn_sendMessage_send)
    public void onSendClicked() {
        String message = messageBox.getText().toString().trim();

        if(message.length()>=1) {
            messageBox.setText("");
            TextMessage textMessage = new TextMessage(message);
            messagePresenter.sendTextMessage(chatId, currentUser, textMessage);
        }
    }

    @OnTextChanged(R.id.et_sendmessage_message)
    public void onMessageChanged() {
        String message = messageBox.getText().toString().trim();
        if(message.length()>=1) {
            sendImageButton.setImageResource(R.drawable.ic_keyboard_send);
            if(currentChatState != ChatState.composing) {
                messagePresenter.sendChatState(chatId, SendChatStateUseCase.CHAT_TYPING);
                currentChatState = ChatState.composing;
            }
        } else {
            sendImageButton.setImageResource(R.drawable.ic_keyboard_audio);
            if(currentChatState != ChatState.paused) {
                messagePresenter.sendChatState(chatId, SendChatStateUseCase.CHAT_PAUSED);
                currentChatState = ChatState.paused;
            }
        }
    }

    @Override
    public void displayMessages(List<MessageResult> messages) {
        messagesAdapter.setMessages(messages);
        messageItem.scrollToPosition(messages.size()-1);
    }

    @Override
    public void addMessageToList(MessageResult message) {
        Logger.i(this, "Add message:"+message.toString());
        messagesAdapter.addMessage(message);
        messageItem.scrollToPosition(messagesAdapter.getItemCount()-1);
    }

    @Override
    public void setName(String name) {
        Logger.d(this, "Setting name:");
        title.setText(name);
    }

    @Override
    public void updateDeliveryStatus(MessageResult messageResult) {
        messagesAdapter.updateMessage(messageResult);
    }

    @Override
    public void updateDeliveryStatus(String deliveryReceiptId, MessageResult.MessageStatus messageStatus) {
        messagesAdapter.updateDeliveryStatus(deliveryReceiptId, messageStatus);
    }

    @Override
    public void updatePresence(String presence) {
        Logger.d(this, "Presence: "+presence);
//        presenceTextView.setText(presence);
    }


    @Override
    public void sendPostbackMessage(String message) {
        TextMessage textMessage = new TextMessage(message);
        messagePresenter.sendTextMessage(chatId, currentUser, textMessage);
    }

    @Override
    public void urlButtonClicked(String url) {
        startActivity(WebViewActivity.callingIntent(this, url));
    }

    @Override
    public void onQuickReplyClicked(String text) {
        TextMessage textMessage = new TextMessage(text);
        messagePresenter.sendTextMessage(chatId, currentUser, textMessage);
    }

    @Override
    public void onMessageReceived(MessageResult messageResult) {
        super.onMessageReceived(messageResult);
        if(messageResult.getChatId().equals(chatId)) {
            messagesAdapter.addMessage(messageResult);
            messageItem.scrollToPosition(messagesAdapter.getItemCount() - 1);
            messagePresenter.updateMessageRead(messageResult);
        }
    }

    @Override
    public void onChatStateReceived(String from, ChatState chatState) {
        super.onChatStateReceived(from, chatState);
        if(from.equals(chatId)) {
            if(chatState == ChatState.composing)
                updatePresence("Typing...");
            else
                messagePresenter.getPresence(from);
        }
    }

    @Override
    public void onMessageStatusReceived(String chatId, String deliveryReceiptId, MessageResult.MessageStatus messageStatus) {
        super.onMessageStatusReceived(chatId, deliveryReceiptId, messageStatus);
        Logger.d(this, "Message Status:"+messageStatus.name());
        if(this.chatId.equals(chatId)) {
            updateDeliveryStatus(deliveryReceiptId, messageStatus);
        }
    }


    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        componentContainer.userSessionComponent().plus(new MessageModule()).inject(this);
        currentUser = componentContainer.userSessionComponent().getUserSession().getUserName();
    }
}