package com.stairway.spotlight.screens.message;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.stairway.data.manager.Logger;
import com.stairway.data.source.message.MessageResult;
import com.stairway.spotlight.R;
import com.stairway.spotlight.application.SpotlightApplication;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.data.manager.XMPPManager;
import com.stairway.spotlight.screens.message.di.MessageModule;
import com.stairway.spotlight.screens.user_profile.UserProfileActivity;

import org.jivesoftware.smackx.chatstates.ChatState;
import org.w3c.dom.Text;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class MessageActivity extends BaseActivity implements MessageContract.View {
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

    TextView presenceTextView;

    private ChatState currentChatState;

    private static String KEY_USER_ID = "USERID";
    private String chatId; // contact user, mobile
    private String currentUser; // this user, mobile
    private MessagesAdapter messagesAdapter;

    public static Intent callingIntent(Context context, String userId) {
        Intent intent = new Intent(context, MessageActivity.class);
        intent.putExtra(KEY_USER_ID, userId);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentChatState = ChatState.inactive;
        Intent receivedIntent = getIntent();
        if(!receivedIntent.hasExtra(KEY_USER_ID))
            return;

        chatId = receivedIntent.getStringExtra(KEY_USER_ID);
        setContentView(R.layout.activity_message);
        ButterKnife.bind(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messagesAdapter = new MessagesAdapter(this);
        messageItem.setLayoutManager(linearLayoutManager);
        messageItem.setAdapter(messagesAdapter);

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
        ab.setCustomView(R.layout.actionbar_message_name);

        View v =getSupportActionBar().getCustomView();
        presenceTextView = (TextView) v.findViewById(R.id.tv_message_presence);
        ImageButton profileDP = (ImageButton) v.findViewById(R.id.actionbar_message_profile);
        profileDP.setOnClickListener(v1 -> startActivity(UserProfileActivity.callingIntent(getBaseContext(), "12")));
        messagePresenter.attachView(this);
        Logger.d("ChatId: "+chatId+", CurrentUser:"+currentUser);
        messagePresenter.loadMessages(chatId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        messagePresenter.attachView(this);
        messagePresenter.getPresence(chatId);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.messages_action_bar, menu);
        return true;
    }

    @OnClick(R.id.btn_sendMessage_send)
    public void onSendClicked() {
        String message = messageBox.getText().toString().trim();

        if(message.length()>=1) {
            messageBox.setText("");
            messagePresenter.sendMessage(new MessageResult(chatId, currentUser, message, MessageResult.MessageStatus.NOT_SENT));
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
            sendImageButton.setImageResource(R.drawable.ic_keyboard_plus);
            if(currentChatState != ChatState.paused) {
                messagePresenter.sendChatState(chatId, SendChatStateUseCase.CHAT_PAUSED);
                currentChatState = ChatState.paused;
            }
        }
    }

    @Override
    public void displayMessages(List<MessageResult> messages) {
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
    public void updateDeliveryStatus(String deliveryReceiptId, MessageResult.MessageStatus messageStatus) {
        messagesAdapter.updateDeliveryStatus(deliveryReceiptId, messageStatus);
    }

    @Override
    public void updatePresence(String presence) {
        Logger.d("Presence: "+presence);
        presenceTextView.setText(presence);
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        componentContainer.userSessionComponent().plus(new MessageModule()).inject(this);
        currentUser = componentContainer.userSessionComponent().getUserSession().getChatId();
    }

    @Override
    public void onMessageReceived(MessageResult messageResult) {
        super.onMessageReceived(messageResult);
        if(messageResult.getChatId().equals(chatId)) {
            messagesAdapter.addMessage(messageResult);
            messageItem.scrollToPosition(messagesAdapter.getItemCount() - 1);
            messagePresenter.updateMessageSeen(messageResult);
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
        if(this.chatId.equals(chatId)) {
            updateDeliveryStatus(deliveryReceiptId, messageStatus);
        }
    }
}