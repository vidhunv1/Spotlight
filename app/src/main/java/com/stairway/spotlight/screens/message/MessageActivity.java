package com.stairway.spotlight.screens.message;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.stairway.spotlight.AccessTokenManager;
import com.stairway.spotlight.MessageController;
import com.stairway.spotlight.R;

import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.db.ContactStore;
import com.stairway.spotlight.db.MessageStore;
import com.stairway.spotlight.models.MessageResult;
import com.stairway.spotlight.screens.home.HomeActivity;
import com.stairway.spotlight.screens.message.view_models.TextMessage;
import com.stairway.spotlight.screens.user_profile.UserProfileActivity;
import com.stairway.spotlight.screens.web_view.WebViewActivity;

import org.jivesoftware.smackx.chatstates.ChatState;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class MessageActivity extends BaseActivity
        implements MessageContract.View, MessagesAdapter.PostbackClickListener, MessagesAdapter.UrlClickListener, QuickRepliesAdapter.QuickReplyClickListener{
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
    private String chatId; // contact user
    private String currentUser; // this user
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
        MessageController messageController = MessageController.getInstance();
        MessageStore messageStore = new MessageStore();
        ContactStore contactStore = new ContactStore();
        LoadMessagesUseCase loadMessagesUseCase = new LoadMessagesUseCase(messageController, messageStore);
        StoreMessageUseCase storeMessageUseCase = new StoreMessageUseCase(messageController, messageStore);
        SendMessageUseCase sendMessageUseCase = new SendMessageUseCase(messageController, messageStore);
        GetPresenceUseCase getPresenceUseCase = new GetPresenceUseCase(messageController);
        UpdateMessageUseCase updateMessageUseCase = new UpdateMessageUseCase(messageStore);
        SendChatStateUseCase sendChatStateUseCase = new SendChatStateUseCase(messageController);
        SendReadReceiptUseCase sendReadReceiptUseCase = new SendReadReceiptUseCase(messageController, messageStore);
        GetNameUseCase getNameUseCase = new GetNameUseCase(contactStore);
        messagePresenter = new MessagePresenter(loadMessagesUseCase, storeMessageUseCase, sendMessageUseCase, getPresenceUseCase, updateMessageUseCase, sendChatStateUseCase, sendReadReceiptUseCase, getNameUseCase);

        currentChatState = ChatState.inactive;
        currentUser = AccessTokenManager.getInstance().load().getUserName();
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
        RecyclerView.ItemAnimator animator = messageItem.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        messageItem.setAdapter(messagesAdapter);
//        OverScrollDecoratorHelper.setUpOverScroll(messageItem, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

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

    @Override
    public void onBackPressed() {
        startActivity(HomeActivity.callingIntent(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.messages_settings, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if((id == android.R.id.home)) {
            super.onBackPressed();
        }
        else if(id == R.id.view_contact) {
            startActivity(UserProfileActivity.callingIntent(this, chatId));
            this.overridePendingTransition(0, 0);
        }
//        else if(id == R.id.action_profile) {
//            startActivity(UserProfileActivity.callingIntent(this, chatId));
//            this.overridePendingTransition(0, 0);
//        }
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
            sendImageButton.setVisibility(View.VISIBLE);
            sendImageButton.setImageResource(R.drawable.ic_keyboard_send);
            if(currentChatState != ChatState.composing) {
                messagePresenter.sendChatState(chatId, ChatState.composing);
                currentChatState = ChatState.composing;
            }
        } else {
            sendImageButton.setVisibility(View.GONE);
            if(currentChatState != ChatState.gone) {
                messagePresenter.sendChatState(chatId, ChatState.gone);
                currentChatState = ChatState.gone;
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
}