package com.chat.ichat.screens.message;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.chat.ichat.UserSessionManager;
import com.chat.ichat.MessageController;
import com.chat.ichat.R;
import com.chat.ichat.api.bot.PersistentMenu;
import com.chat.ichat.config.AnalyticsContants;
import com.chat.ichat.core.BaseActivity;
import com.chat.ichat.core.GsonProvider;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.NotificationController;
import com.chat.ichat.core.lib.AndroidUtils;
import com.chat.ichat.core.lib.ImageUtils;
import com.chat.ichat.db.BotDetailsStore;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.db.MessageStore;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.models.Message;
import com.chat.ichat.models.MessageResult;
import com.chat.ichat.screens.message.emoji.EmojiViewHelper;
import com.chat.ichat.screens.user_profile.UserProfileActivity;
import com.chat.ichat.screens.web_view.WebViewActivity;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MessageActivity extends BaseActivity
        implements MessageContract.View, MessagesAdapter.PostbackClickListener, MessagesAdapter.UrlClickListener{
    private MessagePresenter messagePresenter;

    @Bind(R.id.rv_messageitem)
    RecyclerView messageList;

    EditText messageBox;

    LinearLayout sendButtonView;

    @Bind(R.id.tb_message)
    Toolbar toolbar;

    @Bind(R.id.tb_message_title)
    TextView title;

//    @Bind(R.id.tb_message_presence)
//    TextView presenceView;

    @Bind(R.id.container)
    RelativeLayout rootLayout;

    @Bind(R.id.message_add_block)
    LinearLayout addBlockView;

    @Bind(R.id.iv_profile_image)
    ImageView profileImage;

    private EmojiViewHelper emojiPicker;
    private List<PersistentMenu> persistentMenus;

    private WrapContentLinearLayoutManager linearLayoutManager;

    private ChatState currentChatState;
    private boolean shouldHandleBack = true;

    private final int RESULT_TAKE_PICTURE = 1;
    private String currentPhotoPath;
    private Uri imageUri;

    public static int index = -1;
    public static int top = -1;

    private static final String KEY_CHAT_USER_NAME = "MessageActivity.CHAT_USERNAME";
    private String chatUserName; // contact user
    private String currentUser; // this user
    private MessagesAdapter messagesAdapter;
    private String contactName;
    private String contactUserId;
    private String contactProfileDP;

    private FirebaseAnalytics firebaseAnalytics;
    private final String SCREEN_NAME = "message";
    // userName: id for ejabberd xmpp. userId: id set by user
    public static Intent callingIntent(Context context, String chatUserName) {
        Logger.d("[MessageActivity] "+chatUserName);
        Intent intent = new Intent(context, MessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_CHAT_USER_NAME, chatUserName);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        ButterKnife.bind(this);
        messagePresenter = new MessagePresenter(MessageStore.getInstance(), MessageController.getInstance(), BotDetailsStore.getInstance(), ContactStore.getInstance());

        Intent receivedIntent = getIntent();
        if(!receivedIntent.hasExtra(KEY_CHAT_USER_NAME))
            return;
        this.chatUserName = receivedIntent.getStringExtra(KEY_CHAT_USER_NAME);

        currentChatState = ChatState.inactive;
        currentUser = UserSessionManager.getInstance().load().getUserName();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        index = -1;
        top = -1;

        this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(linearLayoutManager!=null) {
            index = linearLayoutManager.findFirstVisibleItemPosition();
            View v = messageList.getChildAt(0);
            top = (v == null) ? 0 : (v.getTop() - messageList.getPaddingTop());
        }
    }

    @Override
    protected void onResume() {
        Logger.d(this, "onResume");
        super.onResume();
        if(index != -1) {
            linearLayoutManager.scrollToPositionWithOffset( index, top);
        }
        messagePresenter.loadMessages(chatUserName);

        /*              Analytics           */
        firebaseAnalytics.setCurrentScreen(this, SCREEN_NAME, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        messagePresenter.attachView(this);
        messagePresenter.loadContactDetails(chatUserName);
        messagePresenter.loadKeyboard(chatUserName);

    }

    @Override
    protected void onStop() {
        super.onStop();
        messagePresenter.detachView();
    }

    @Override
    public void onBackPressed() {
        if(shouldHandleBack) {
            super.onBackPressed();
        } else {
            AndroidUtils.hideSoftInput(this);
            shouldHandleBack = true;
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.messages_toolbar, menu);
//
//        return true;
//    }
//


    @Override
    public void showError(String title, String message) {
        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage("\n"+message);
        alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if((id == android.R.id.home)) {
            AndroidUtils.hideSoftInput(this);
            this.finish();
        }
//        else if(id == R.id.view_contact) {
//            AndroidUtils.hideSoftInput(this);
//            startActivity(UserProfileActivity.callingIntent(this, chatUserName, contactUserId, contactName));
//        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.message_head)
    public void onTBClicked() {
        AndroidUtils.hideSoftInput(this);
        startActivity(UserProfileActivity.callingIntent(this, chatUserName, contactUserId, contactName, contactProfileDP));

        /*              Analytics           */
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, chatUserName);
        firebaseAnalytics.logEvent(AnalyticsContants.Event.MESSAGE_TITLE_CLICK, bundle);
    }

    @OnClick(R.id.iv_profile_image)
    public void onProfileDPCLiked() {
        /*              Analytics           */
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, chatUserName);
        firebaseAnalytics.logEvent(AnalyticsContants.Event.MESSAGE_TITLE_CLICK, bundle);
    }

    public void onSendClicked() {
        String message = messageBox.getText().toString().trim();

        if(message.length()>=1) {
            messageBox.setText("");
            Message m = new Message();
            m.setText(message);
            messagePresenter.sendTextMessage(chatUserName, currentUser, GsonProvider.getGson().toJson(m));
        }

        /*              Analytics           */
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsContants.Param.MESSAGE, message);
        bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, chatUserName);
        firebaseAnalytics.logEvent(AnalyticsContants.Event.SEND_MESSAGE, bundle);
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
        if(persistentMenus!=null) {
            View menuItemsView;
            BottomSheetDialog bottomSheetDialog;
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
                        this.sendPostbackMessage(menu.getTitle(), menu.getPayload());
                    } else if(menu.getType() == PersistentMenu.Type.web_url) {
                        startActivity(WebViewActivity.callingIntent(this, menu.getUrl()));
                    }
                });
            }

            ImageView close = (ImageView) menuItemsView.findViewById(R.id.close);
            close.setOnClickListener(v -> bottomSheetDialog.dismiss());

            LinearLayout enterText = (LinearLayout) menuItemsView.findViewById(R.id.ll_bottomsheet_entertext);
            enterText.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                messageBox.requestFocus();
            });
            bottomSheetDialog.show();
        }

        /*              Analytics           */
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, this.chatUserName);
        firebaseAnalytics.logEvent(AnalyticsContants.Event.CLICK_BOT_MENU, bundle);
    }

    @Override
    public void setContactDetails(ContactResult contact) {
        contactName = AndroidUtils.displayNameStyle(contact.getContactName());
        this.contactName = AndroidUtils.displayNameStyle(contact.getContactName());
        this.contactUserId = contact.getUserId();
        this.contactProfileDP = contact.getProfileDP();

        title.setText(contactName);

        linearLayoutManager = new WrapContentLinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messagesAdapter = new MessagesAdapter(this, chatUserName, contactName, contact.getProfileDP(), this, this);
        messageList.setLayoutManager(linearLayoutManager);

        RecyclerView.ItemAnimator animator = messageList.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        messageList.setAdapter(messagesAdapter);
        showAddBlock(!contact.isAdded());

        if(contact.getProfileDP()!=null && !contact.getProfileDP().isEmpty()) {
            Context context = this;
            Glide.with(this)
                    .load(contact.getProfileDP().replace("https://", "http://"))
                    .asBitmap().centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new BitmapImageViewTarget(profileImage) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            profileImage.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        } else {
            profileImage.setImageDrawable(ImageUtils.getDefaultProfileImage(contact.getContactName(), contact.getUsername(), 18));
        }
    }

    @Override
    public void showAddBlock(boolean shouldShow) {
        Logger.d(this, "showAddBlock: "+shouldShow);
        if(shouldShow) {
            TextView addView = (TextView) findViewById(R.id.message_add);
            TextView blockView = (TextView) findViewById(R.id.message_block);
            addBlockView.setVisibility(View.VISIBLE);
            addView.setOnClickListener(v -> {
                messagePresenter.addContact(chatUserName);

                /*              Analytics           */
                Bundle bundle = new Bundle();
                bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, this.chatUserName);
                firebaseAnalytics.logEvent(AnalyticsContants.Event.OTHER_ADD_CONTACT, bundle);
            });
            blockView.setOnClickListener(v -> {
                String message = "Are you sure you want to block <b>" + contactName + "</b>?";;
                AlertDialog alertDialog = new AlertDialog.Builder(MessageActivity.this).create();
                alertDialog.setMessage(Html.fromHtml(message));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", (dialog, which) -> {
                    messagePresenter.blockContact(chatUserName);
                    dialog.dismiss();
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", (dialog, which) -> {
                    dialog.cancel();
                });
                alertDialog.show();

                /*              Analytics           */
                Bundle bundle = new Bundle();
                bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, this.chatUserName);
                firebaseAnalytics.logEvent(AnalyticsContants.Event.BLOCK_USER, bundle);
            });
        } else {
            addBlockView.setVisibility(View.GONE);
        }
    }

    @Override
    public void showContactAddedSuccess() {
        String message;
        message = "<b>" + contactName + "</b> is added to your contacts on iChat.";

        AlertDialog alertDialog = new AlertDialog.Builder(MessageActivity.this).create();
        alertDialog.setMessage(Html.fromHtml(message));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();

        alertDialog.setOnDismissListener(dialog -> {
            showAddBlock(false);
        });
    }

    @Override
    public void showContactBlockedSuccess() {
        String message;
        message = "This contact has been blocked.";

        AlertDialog alertDialog = new AlertDialog.Builder(MessageActivity.this).create();
        alertDialog.setMessage(Html.fromHtml(message));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();

        alertDialog.setOnDismissListener(dialog -> {
            showAddBlock(false);
        });
    }

    @Override
    public void initBotMenu(List<PersistentMenu> persistentMenus) {
        this.persistentMenus = persistentMenus;
    }

    @Override
    public void displayMessages(List<MessageResult> messages) {
        NotificationController.getInstance().updateNotification();
        messagesAdapter.setMessages(messages);
        messagePresenter.sendReadReceipt(chatUserName);

        messagePresenter.getLastActivity(chatUserName);
    }

    @Override
    public void setKeyboardType(boolean isBotKeyboard) {
        LinearLayout rootLayout = (LinearLayout) findViewById(R.id.ll_keyboard);
        rootLayout.removeViewAt(rootLayout.getChildCount()-1);
        if(isBotKeyboard) {
            View botKeyboardView = View.inflate(this, R.layout.layout_bot_keyboard, rootLayout);
            sendButtonView = (LinearLayout) botKeyboardView.findViewById(R.id.btn_sendMessage_send);
            messageBox = (EditText) botKeyboardView.findViewById(R.id.et_sendmessage_message);
            botKeyboardView.findViewById(R.id.message_menu).setOnClickListener(v -> onMessageMenuClicked());

            // remove later after regular keyboard change. -------------------------------------------------------
            messageBox.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() >= 1) {
                        sendButtonView.setVisibility(View.VISIBLE);
                        sendButtonView.setBackgroundResource(R.drawable.bg_send_active);
                    } else {
                        sendButtonView.setVisibility(View.GONE);
                    }
                    onMessageChanged();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
            // remove later after regular keyboard change. -------------------------------------------------------

            /*              Analytics           */
            Bundle bundle = new Bundle();
            bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, this.chatUserName);
            bundle.putString(AnalyticsContants.Param.KEYBOARD_TYPE, "Bot");
            firebaseAnalytics.logEvent(AnalyticsContants.Event.KEYBOARD_TYPE, bundle);
        } else {
            // regular keyboard
            View regularKeyboardView = View.inflate(this, R.layout.layout_regular_keyboard, rootLayout);
            sendButtonView = (LinearLayout) regularKeyboardView.findViewById(R.id.btn_sendMessage_send);
            ImageButton emojiButton = (ImageButton) regularKeyboardView.findViewById(R.id.btn_message_smiley);
            ImageButton cameraButton = (ImageButton) regularKeyboardView.findViewById(R.id.btn_sendMessage_camera);

            MessageEditText messageEditText = (MessageEditText) regularKeyboardView.findViewById(R.id.et_sendmessage_message);
            FrameLayout smileyLayout = (FrameLayout) regularKeyboardView.findViewById(R.id.smiley_layout);
            messageBox = messageEditText;
            emojiPicker = new EmojiViewHelper(this, smileyLayout, getWindow());

            messageEditText.setOnEditTextImeBackListener(new MessageEditText.EditTextImeBackListener() {
                @Override
                public void onImeBack() {
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

            emojiButton.setOnClickListener(v -> {
                shouldHandleBack = true;
                emojiPicker.emojiButtonToggle();
                if(!emojiPicker.isEmojiState()) {
                    emojiButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_keyboard));
                } else {
                    emojiButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_smiley));
                    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.showSoftInput(messageEditText, InputMethodManager.RESULT_SHOWN);
                }

                /*              Analytics           */
                Bundle bundle = new Bundle();
                bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, this.chatUserName);
                firebaseAnalytics.logEvent(AnalyticsContants.Event.MESSAGE_SMILEY, bundle);
            });

            emojiPicker.setOnEmojiconBackspaceClickedListener(v -> {
                messageEditText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            });

            emojiPicker.setOnEmojiconClickedListener(v -> {
                String text = messageEditText.getText() + v.getEmoji();
                messageEditText.setText(text);
                messageEditText.setSelection(text.length());

                /*              Analytics           */
                Bundle bundle = new Bundle();
                bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, this.chatUserName);
                firebaseAnalytics.logEvent(AnalyticsContants.Event.SMILEY_SELECTED, bundle);
            });

            cameraButton.setOnClickListener(v -> {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                PackageManager pm = this.getPackageManager();
                if (cameraIntent.resolveActivity(this.getPackageManager()) != null && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    File photoFile = null;
                    try {
                        photoFile = ImageUtils.createImageFile(this);
                    } catch (IOException ex) {
                        Logger.d(this, "Error creating image file.");
                    }
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(this, "com.chat.ichat.fileprovider", photoFile);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        currentPhotoPath = photoFile.getAbsolutePath();
                        startActivityForResult(cameraIntent, RESULT_TAKE_PICTURE);
                    }
                }

                /*              Analytics           */
                Bundle bundle = new Bundle();
                bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, this.chatUserName);
                firebaseAnalytics.logEvent(AnalyticsContants.Event.MESSAGE_CAMERA, bundle);
            });

            messageBox.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() >= 1) {
                        sendButtonView.setBackgroundResource(R.drawable.bg_send_active);
                        cameraButton.setVisibility(View.GONE);
                    } else {
                        sendButtonView.setBackgroundResource(R.drawable.bg_send_inactive);
                        cameraButton.setVisibility(View.VISIBLE);
                    }
                    onMessageChanged();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            /*              Analytics           */
            Bundle bundle = new Bundle();
            bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, this.chatUserName);
            bundle.putString(AnalyticsContants.Param.KEYBOARD_TYPE, "Regular");
            firebaseAnalytics.logEvent(AnalyticsContants.Event.KEYBOARD_TYPE, bundle);
        }

        sendButtonView.setOnClickListener(v -> {
            onSendClicked();
        });
    }

    @Override
    public void addMessageToList(MessageResult message) {
        messageList.scrollToPosition(messagesAdapter.getItemCount());
        if(messagesAdapter!=null)
            messagesAdapter.addMessage(message);
    }

    @Override
    public void updateDeliveryStatus(MessageResult messageResult) {
        if(messagesAdapter!=null)
            messagesAdapter.updateMessage(messageResult);
    }

    @Override
    public void updateDeliveryStatus(String deliveryReceiptId, MessageResult.MessageStatus messageStatus) {
        if(messagesAdapter!=null)
            messagesAdapter.updateDeliveryStatus(deliveryReceiptId, messageStatus);
    }

    @Override
    public void updateLastActivity(String time) {
//        if(time != null && !time.isEmpty()) {
//            presenceView.setVisibility(View.VISIBLE);
//            presenceView.setText(time);
//        } else {
//            presenceView.setVisibility(View.GONE);
//        }
    }

    @Override
    public void sendPostbackMessage(String message, String payload) {
        Message m = new Message();
        m.setText(message);
        if(payload!=null && !payload.isEmpty()) {
            m.setPayload(payload);
        }
        messagePresenter.sendTextMessage(chatUserName, currentUser, GsonProvider.getGson().toJson(m));
    }

    @Override
    public void urlButtonClicked(String url) {
        startActivity(WebViewActivity.callingIntent(this, url));
    }

    @Override
    public void onMessageReceived(MessageResult messageResult, ContactResult contactResult) {
        if(messageResult.getChatId().equals(chatUserName) && messagesAdapter!=null) {
            messagesAdapter.addMessage(messageResult);
            messageList.scrollToPosition(messagesAdapter.getItemCount() - 1);
            messagePresenter.updateMessageRead(messageResult);
            final MediaPlayer mp = MediaPlayer.create(this, R.raw.conversation_tone);
            mp.start();
        } else {
            NotificationController.getInstance().showNotificationAndAlert(true);
        }
    }

    @Override
    public void onChatStateReceived(String from, ChatState chatState) {
        super.onChatStateReceived(from, chatState);
        if(from.equals(chatUserName)) {
            if(chatState == ChatState.composing) {
//                presenceView.setText(getResources().getString(R.string.chat_state_typing));
            } else {
                messagePresenter.getLastActivity(this.chatUserName);
            }
        }
    }

    @Override
    public void onPresenceChanged(String username, Presence.Type type) {
        super.onPresenceChanged(username, type);
        if(username.equals(chatUserName)) {
//            presenceView.setVisibility(View.VISIBLE);
            if(type == Presence.Type.available) {
//                presenceView.setText(getResources().getString(R.string.chat_presence_online));
            } else if(type == Presence.Type.unavailable) {
                DateTime timeNow = DateTime.now();
//                presenceView.setText(getResources().getString(R.string.chat_presence_away, AndroidUtils.lastActivityAt(timeNow)));
            }
        }
    }

    @Override
    public void onMessageStatusReceived(String chatId, String deliveryReceiptId, MessageResult.MessageStatus messageStatus) {
        super.onMessageStatusReceived(chatId, deliveryReceiptId, messageStatus);

        if(this.chatUserName.equals(chatId)) {
            updateDeliveryStatus(deliveryReceiptId, messageStatus);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_TAKE_PICTURE && resultCode == RESULT_OK && data!=null) {
            if(currentPhotoPath!=null) {
                Logger.d(this, "Got image");
//                setProfileDP(new File(currentPhotoPath));
//                presenter.uploadProfileDP(new File(currentPhotoPath), userSession);
//                galleryAddPic(currentPhotoPath);
            }
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