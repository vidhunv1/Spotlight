package com.stairway.spotlight.screens.message;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stairway.spotlight.AccessTokenManager;
import com.stairway.spotlight.MessageController;
import com.stairway.spotlight.R;

import com.stairway.spotlight.api.bot.PersistentMenu;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.core.lib.AndroidUtils;
import com.stairway.spotlight.db.BotDetailsStore;
import com.stairway.spotlight.db.MessageStore;
import com.stairway.spotlight.models.MessageResult;
import com.stairway.spotlight.screens.message.emoji.EmojiViewHelper;
import com.stairway.spotlight.screens.user_profile.UserProfileActivity;
import com.stairway.spotlight.screens.web_view.WebViewActivity;

import org.jivesoftware.smackx.chatstates.ChatState;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MessageActivity extends BaseActivity
        implements MessageContract.View, MessagesAdapter.PostbackClickListener, MessagesAdapter.UrlClickListener, QuickRepliesAdapter.QuickReplyClickListener{
    private MessagePresenter messagePresenter;

    @Bind(R.id.rv_messageitem)
    RecyclerView messageList;

    EditText messageBox;

    ImageButton sendImageButton;

    @Bind(R.id.tb_message)
    Toolbar toolbar;

    @Bind(R.id.tb_message_title)
    TextView title;

    @Bind(R.id.tb_message_presence)
    TextView presenceView;

    View menuItemsView;
    BottomSheetDialog bottomSheetDialog;
    private EmojiViewHelper emojiPicker;

    private WrapContentLinearLayoutManager linearLayoutManager;

    private ChatState currentChatState;
    private boolean shouldHandleBack = true;

    public static int index = -1;
    public static int top = -1;

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
        messagePresenter = new MessagePresenter(MessageStore.getInstance(), MessageController.getInstance(), BotDetailsStore.getInstance());

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

        index = -1;
        top = -1;
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
        messagePresenter.loadMessages(chatUserName);
        if(index != -1) {
            linearLayoutManager.scrollToPositionWithOffset( index, top);
        }
        messagePresenter.getPresence(chatUserName);
        messagePresenter.sendReadReceipt(chatUserName);
//        if(emojiPicker!=null) {
//            emojiPicker.reset();
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        messagePresenter.attachView(this);
        messagePresenter.loadKeyboard(chatUserName);
    }

    @Override
    protected void onStop() {
        super.onStop();
        messagePresenter.detachView();
    }

    @Override
    public void onBackPressed() {
        Logger.d(this, "activityBackPressed");
        if(shouldHandleBack) {
            super.onBackPressed();
        } else {
            AndroidUtils.hideSoftInput(this);
            shouldHandleBack = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.messages_toolbar, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if((id == android.R.id.home)) {
            AndroidUtils.hideSoftInput(this);
            this.finish();
        }
        else if(id == R.id.view_contact) {
            AndroidUtils.hideSoftInput(this);
            startActivity(UserProfileActivity.callingIntent(this, chatUserName, chatContactName));
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSendClicked() {
        String message = messageBox.getText().toString().trim();

        if(message.length()>=1) {
            messageBox.setText("");
            messagePresenter.sendTextMessage(chatUserName, currentUser, message);
        }
    }

    public void onMessageChanged() {
        String message = messageBox.getText().toString().trim();

        if(message.length()>=1) {
            if(currentChatState != ChatState.composing) {
                messagePresenter.sendChatState(chatUserName, ChatState.composing);
                currentChatState = ChatState.composing;
            }
        } else {
            if(currentChatState != ChatState.gone) {
                messagePresenter.sendChatState(chatUserName, ChatState.gone);
                currentChatState = ChatState.gone;
            }
        }
    }

    public void onMessageMenuClicked() {
        if(bottomSheetDialog!=null) {
            bottomSheetDialog.show();
        }
    }

    @Override
    public void initBotMenu(List<PersistentMenu> persistentMenus) {
        bottomSheetDialog = new BottomSheetDialog(this);
        menuItemsView = this.getLayoutInflater().inflate(R.layout.bottomsheet_menu_message, null);

        bottomSheetDialog.setContentView(menuItemsView);

        List<TextView> itemsTV = new ArrayList<>(5);
        List<LinearLayout> itemsLL = new ArrayList<>(5);
        itemsLL.add((LinearLayout) menuItemsView.findViewById(R.id.ll_bottomsheet_item1));
        itemsLL.add((LinearLayout) menuItemsView.findViewById(R.id.ll_bottomsheet_item2));
        itemsLL.add((LinearLayout) menuItemsView.findViewById(R.id.ll_bottomsheet_item3));
        itemsLL.add((LinearLayout) menuItemsView.findViewById(R.id.ll_bottomsheet_item4));
        itemsLL.add((LinearLayout) menuItemsView.findViewById(R.id.ll_bottomsheet_item5));

        for (LinearLayout linearLayout : itemsLL) {
            linearLayout.setVisibility(View.GONE);
        }

        itemsTV.add((TextView) menuItemsView.findViewById(R.id.tv_bottomsheet_item1));
        itemsTV.add((TextView) menuItemsView.findViewById(R.id.tv_bottomsheet_item2));
        itemsTV.add((TextView) menuItemsView.findViewById(R.id.tv_bottomsheet_item3));
        itemsTV.add((TextView) menuItemsView.findViewById(R.id.tv_bottomsheet_item4));
        itemsTV.add((TextView) menuItemsView.findViewById(R.id.tv_bottomsheet_item5));

        for (int i = 0; i < persistentMenus.size(); i++) {
            itemsLL.get(i).setVisibility(View.VISIBLE);
            itemsTV.get(i).setText(persistentMenus.get(i).getTitle());
            PersistentMenu menu = persistentMenus.get(i);
            itemsLL.get(i).setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                if(menu.getType() == PersistentMenu.Type.postback) {
                    messagePresenter.sendTextMessage(chatUserName, currentUser, menu.getTitle());
                } else if(menu.getType() == PersistentMenu.Type.web_url) {
                    startActivity(WebViewActivity.callingIntent(this, menu.getUrl()));
                }
            });
        }

        LinearLayout enterText = (LinearLayout) menuItemsView.findViewById(R.id.ll_bottomsheet_entertext);
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
    public void setKeyboardType(boolean isBotKeyboard) {
        LinearLayout rootLayout = (LinearLayout) findViewById(R.id.ll_keyboard);
        rootLayout.removeViewAt(rootLayout.getChildCount()-1);
        if(isBotKeyboard) {
            View botKeyboardView = View.inflate(this, R.layout.layout_bot_keyboard, rootLayout);
            sendImageButton = (ImageButton) botKeyboardView.findViewById(R.id.btn_sendMessage_send);
            messageBox = (EditText) botKeyboardView.findViewById(R.id.et_sendmessage_message);
            botKeyboardView.findViewById(R.id.message_menu).setOnClickListener(v -> onMessageMenuClicked());
        } else {
            // regular keyboard
            View regularKeyboardView = View.inflate(this, R.layout.layout_regular_keyboard, rootLayout);
            sendImageButton = (ImageButton) regularKeyboardView.findViewById(R.id.btn_sendMessage_send);
            ImageButton emojiButton = (ImageButton) regularKeyboardView.findViewById(R.id.btn_message_smiley);

            MessageEditText messageEditText = (MessageEditText) regularKeyboardView.findViewById(R.id.et_sendmessage_message);
            FrameLayout smileyLayout = (FrameLayout) regularKeyboardView.findViewById(R.id.smiley_layout);
            messageBox = messageEditText;
            emojiPicker = new EmojiViewHelper(this, smileyLayout, getWindow());

            messageEditText.setOnEditTextImeBackListener(new MessageEditText.EditTextImeBackListener() {
                @Override
                public void onImeBack() {
                    Logger.d(this, "backPressed: "+emojiPicker.isEmojiState());

                    if(!emojiPicker.isEmojiState()) {
                        shouldHandleBack = false;
                        emojiPicker.reset();
                        emojiButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_smiley));

                        messageEditText.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(messageEditText, InputMethodManager.SHOW_IMPLICIT);

                    } else {
                        shouldHandleBack = true;
                    }
                    emojiPicker.removeEmojiPickerView();
                }
            });

            messageEditText.setOnTouchListener((v, event) -> {
                shouldHandleBack = false;
                emojiButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_smiley));
                if(!emojiPicker.isEmojiState()) {
                    emojiPicker.emojiButtonToggle();
                }
                return false;
            });

            emojiButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shouldHandleBack = true;
                    emojiPicker.emojiButtonToggle();
                    if(!emojiPicker.isEmojiState()) {
                        emojiButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_keyboard));
                    } else {
                        emojiButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_smiley));
                        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        mgr.showSoftInput(messageEditText, InputMethodManager.RESULT_SHOWN);
                    }
                }
            });

            emojiPicker.setOnEmojiconBackspaceClickedListener(v -> {
                messageEditText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            });

            emojiPicker.setOnEmojiconClickedListener(v -> {
                String text = messageEditText.getText() + v.getEmoji();
                messageEditText.setText(text);
                messageEditText.setSelection(text.length());
            });

        }

        sendImageButton.setOnClickListener(v -> onSendClicked());
        messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(isBotKeyboard) {
                    if (s.length() >= 1) {
                        sendImageButton.setVisibility(View.VISIBLE);
                        sendImageButton.setImageResource(R.drawable.ic_keyboard_send);
                    } else {
                        sendImageButton.setVisibility(View.GONE);
                    }
                } else {
                    if (s.length() >= 1) {
                        sendImageButton.setImageResource(R.drawable.ic_keyboard_send);
                    } else {
                        sendImageButton.setImageResource(R.drawable.ic_mic);
                    }
                }
                onMessageChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
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