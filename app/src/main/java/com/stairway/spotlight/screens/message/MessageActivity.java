package com.stairway.spotlight.screens.message;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stairway.spotlight.AccessTokenManager;
import com.stairway.spotlight.MessageController;
import com.stairway.spotlight.R;

import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.db.ContactStore;
import com.stairway.spotlight.db.MessageStore;
import com.stairway.spotlight.models.ContactResult;
import com.stairway.spotlight.models.MessageResult;
import com.stairway.spotlight.screens.user_profile.UserProfileActivity;
import com.stairway.spotlight.screens.web_view.WebViewActivity;

import org.jivesoftware.smackx.chatstates.ChatState;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MessageActivity extends BaseActivity
        implements MessageContract.View, MessagesAdapter.PostbackClickListener, MessagesAdapter.UrlClickListener, QuickRepliesAdapter.QuickReplyClickListener{
    private MessagePresenter messagePresenter;

    @Bind(R.id.rv_messageitem)
    RecyclerView messageList;

    @Bind(R.id.et_sendmessage_message)
    EditText messageBox;

    @Bind(R.id.btn_sendMessage_send)
    ImageButton sendImageButton;

    @Bind(R.id.tb_message)
    Toolbar toolbar;

    @Bind(R.id.tb_message_title)
    TextView title;

    @Bind(R.id.tb_message_presence)
    TextView presenceView;

    private WrapContentLinearLayoutManager linearLayoutManager;

    private ChatState currentChatState;

    public static int index = -1;
    public static int top = -1;

    private static final String BUNDLE_RECYCLER_LAYOUT = "MessageActivity.recyclerlayout";

    private static final String KEY_CHAT_USER_NAME = "MessageActivity.CHAT_USERNAME";
    private static final String KEY_CHAT_CONTACT_NAME = "MessageActivity.CHAT_CONTACT_NAME";
    private String chatUserName; // contact user
    private String chatContactName;
    private String currentUser; // this user
    private MessagesAdapter messagesAdapter;

    // userName: id for ejabberd xmpp. userId: id set by user:
    public static Intent callingIntent(Context context, String chatUserName, String chatContactName) {
        Intent intent = new Intent(context, MessageActivity.class);
        intent.putExtra(KEY_CHAT_USER_NAME, chatUserName);
        intent.putExtra(KEY_CHAT_CONTACT_NAME, chatContactName);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        ButterKnife.bind(this);
        messagePresenter = new MessagePresenter(MessageStore.getInstance(), MessageController.getInstance());

        Intent receivedIntent = getIntent();
        if(!receivedIntent.hasExtra(KEY_CHAT_USER_NAME))
            return;
        this.chatUserName = receivedIntent.getStringExtra(KEY_CHAT_USER_NAME);
        this.chatContactName = receivedIntent.getStringExtra(KEY_CHAT_CONTACT_NAME);
        title.setText(chatContactName);

        currentChatState = ChatState.inactive;
        currentUser = AccessTokenManager.getInstance().load().getUserName();

        linearLayoutManager = new WrapContentLinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messagesAdapter = new MessagesAdapter(this, chatUserName, chatContactName, this, this, this);
        messageList.setLayoutManager(linearLayoutManager);

        RecyclerView.ItemAnimator animator = messageList.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        messageList.setAdapter(messagesAdapter);
//        OverScrollDecoratorHelper.setUpOverScroll(messageItem, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void onPause() {
        super.onPause();

        index = linearLayoutManager.findFirstVisibleItemPosition();
        View v = messageList.getChildAt(0);
        top = (v == null) ? 0 : (v.getTop() - messageList.getPaddingTop());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d(this, "ChatUserName: "+chatUserName);
        Logger.d(this, "ChatContactName: "+chatContactName);
        messagePresenter.loadMessages(chatUserName);
        if(index != -1) {
            linearLayoutManager.scrollToPositionWithOffset( index, top);
        }
        messagePresenter.getPresence(chatUserName);
        messagePresenter.sendReadReceipt(chatUserName);
    }

    @Override
    protected void onStart() {
        super.onStart();
        messagePresenter.attachView(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        messagePresenter.detachView();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
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
            this.onBackPressed();
        }
        else if(id == R.id.view_contact) {
            startActivity(UserProfileActivity.callingIntent(this, chatUserName));
            this.overridePendingTransition(0, 0);
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btn_sendMessage_send)
    public void onSendClicked() {
        String message = messageBox.getText().toString().trim();

        if(message.length()>=1) {
            messageBox.setText("");
            messagePresenter.sendTextMessage(chatUserName, currentUser, message);
        }
    }

    @OnTextChanged(R.id.et_sendmessage_message)
    public void onMessageChanged() {
        String message = messageBox.getText().toString().trim();

        if(message.length()>=1) {
            sendImageButton.setVisibility(View.VISIBLE);
            sendImageButton.setImageResource(R.drawable.ic_keyboard_send);
            if(currentChatState != ChatState.composing) {
                messagePresenter.sendChatState(chatUserName, ChatState.composing);
                currentChatState = ChatState.composing;
            }
        } else {
            sendImageButton.setVisibility(View.GONE);
            if(currentChatState != ChatState.gone) {
                messagePresenter.sendChatState(chatUserName, ChatState.gone);
                currentChatState = ChatState.gone;
            }
        }
    }

    @OnClick(R.id.message_menu)
    public void onMessageMenuClicked() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = this.getLayoutInflater().inflate(R.layout.bottomsheet_menu_message, null);

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();

        LinearLayout item1 = (LinearLayout) sheetView.findViewById(R.id.ll_bottomsheet_item1);
        LinearLayout item2 = (LinearLayout) sheetView.findViewById(R.id.ll_bottomsheet_item2);
        LinearLayout item3 = (LinearLayout) sheetView.findViewById(R.id.ll_bottomsheet_item3);
        LinearLayout item4 = (LinearLayout) sheetView.findViewById(R.id.ll_bottomsheet_item4);
        LinearLayout item5 = (LinearLayout) sheetView.findViewById(R.id.ll_bottomsheet_item5);
        LinearLayout enterText = (LinearLayout) sheetView.findViewById(R.id.ll_bottomsheet_entertext);

        item1.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            TextView item1Text = (TextView) sheetView.findViewById(R.id.tv_bottomsheet_item1);
            messagePresenter.sendTextMessage(chatUserName, currentUser, item1Text.getText().toString());
        });

        item2.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            TextView item2Text = (TextView) sheetView.findViewById(R.id.tv_bottomsheet_item2);
            messagePresenter.sendTextMessage(chatUserName, currentUser, item2Text.getText().toString());
        });

        item3.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            TextView item3Text = (TextView) sheetView.findViewById(R.id.tv_bottomsheet_item3);
            messagePresenter.sendTextMessage(chatUserName, currentUser, item3Text.getText().toString());
        });

        item4.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            TextView item4Text = (TextView) sheetView.findViewById(R.id.tv_bottomsheet_item4);
            messagePresenter.sendTextMessage(chatUserName, currentUser, item4Text.getText().toString());
        });

        item5.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            TextView item5Text = (TextView) sheetView.findViewById(R.id.tv_bottomsheet_item5);
            messagePresenter.sendTextMessage(chatUserName, currentUser, item5Text.getText().toString());
        });

        enterText.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            messageBox.requestFocus();
        });
    }

    @Override
    public void displayMessages(List<MessageResult> messages) {
        messagesAdapter.setMessages(messages);
    }

    @Override
    public void addMessageToList(MessageResult message) {
        Logger.i(this, "Add message:"+message.toString());
        messageList.scrollToPosition(messagesAdapter.getItemCount());
        messagesAdapter.addMessage(message);
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
        presenceView.setText(presence);
    }


    @Override
    public void sendPostbackMessage(String message) {
        messagePresenter.sendTextMessage(chatUserName, currentUser, message);
    }

    @Override
    public void urlButtonClicked(String url) {
        startActivity(WebViewActivity.callingIntent(this, url));
    }

    @Override
    public void onQuickReplyClicked(String text) {
        messagePresenter.sendTextMessage(chatUserName, currentUser, text);
    }

    @Override
    public void onMessageReceived(MessageResult messageResult) {
        super.onMessageReceived(messageResult);
        if(messageResult.getChatId().equals(chatUserName)) {
            messagesAdapter.addMessage(messageResult);
            messageList.scrollToPosition(messagesAdapter.getItemCount() - 1);
            messagePresenter.updateMessageRead(messageResult);
        }
    }

    @Override
    public void onChatStateReceived(String from, ChatState chatState) {
        super.onChatStateReceived(from, chatState);
        if(from.equals(chatUserName)) {
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
        if(this.chatUserName.equals(chatId)) {
            updateDeliveryStatus(deliveryReceiptId, messageStatus);

        }
    }

    // woraround for samsung devices, IndexOutOfBoundsException: Inconsistency detected. Invalid view holder adapter positionViewHolder
    private class WrapContentLinearLayoutManager extends LinearLayoutManager {
        public WrapContentLinearLayoutManager(Context context) {
            super(context);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                Logger.e(this, "meet a IOOBE in RecyclerView");
            }
        }
    }
}