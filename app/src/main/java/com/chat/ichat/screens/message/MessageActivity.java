package com.chat.ichat.screens.message;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chat.ichat.MessageController;
import com.chat.ichat.MessageService;
import com.chat.ichat.R;
import com.chat.ichat.UserSessionManager;
import com.chat.ichat.api.ApiError;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.bot.PersistentMenu;
import com.chat.ichat.api.payment.MerchantHashRequest;
import com.chat.ichat.api.payment.MerchantHashResponse;
import com.chat.ichat.api.user.UserResponse;
import com.chat.ichat.config.AnalyticsConstants;
import com.chat.ichat.core.BaseActivity;
import com.chat.ichat.core.GsonProvider;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.NotificationController;
import com.chat.ichat.core.lib.AndroidUtils;
import com.chat.ichat.core.lib.ImageUtils;
import com.chat.ichat.db.BotDetailsStore;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.db.GenericCache;
import com.chat.ichat.db.MessageStore;
import com.chat.ichat.db.SavedCardsStore;
import com.chat.ichat.models.AudioMessage;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.models.ImageMessage;
import com.chat.ichat.models.LocationMessage;
import com.chat.ichat.models.Message;
import com.chat.ichat.models.MessageResult;
import com.chat.ichat.models.SavedCardModel;
import com.chat.ichat.screens.home.HomeTabActivity;
import com.chat.ichat.screens.message.audio.AudioRecord;
import com.chat.ichat.screens.message.audio.AudioViewHelper;
import com.chat.ichat.screens.message.emoji.EmojiViewHelper;
import com.chat.ichat.screens.message.gallery.GalleryViewHelper;
import com.chat.ichat.screens.message.gif.GifViewHelper;
import com.chat.ichat.screens.message.persistent_menu.PersistentMenuViewHelper;
import com.chat.ichat.screens.new_chat.AddContactUseCase;
import com.chat.ichat.screens.payments.PayuPaymentsHelper;
import com.chat.ichat.screens.user_profile.UserProfileActivity;
import com.chat.ichat.screens.web_view.WebViewActivity;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.payu.india.Payu.PayuConstants;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.chat.ichat.MessageController.LAST_SEEN_PREFS_FILE;

public class MessageActivity extends BaseActivity
        implements  MessageContract.View,
                    MessagesAdapter.PostbackClickListener,
                    MessagesAdapter.UrlClickListener,
                    MessagesAdapter.ContactActionListener,
                    MessagesAdapter.QuickReplyActionListener,
                    MessagesAdapter.PaymentsListener{
    private MessagePresenter messagePresenter;

    @Bind(R.id.rv_messageitem) RecyclerView messageList;
    @Bind(R.id.tb_message) Toolbar toolbar;
    @Bind(R.id.tb_message_title) TextView title;
    @Bind(R.id.container) RelativeLayout rootLayout;
    TextView presenceView;

    EditText messageBox;

    final ProgressDialog[] progressDialog = new ProgressDialog[1];
    private EmojiViewHelper emojiViewHelper;
    private AudioViewHelper audioViewHelper;
    private GalleryViewHelper galleryViewHelper;
    private GifViewHelper gifViewHelper;
    private PersistentMenuViewHelper persistentMenuViewHelper;

    private List<PersistentMenu> persistentMenus;
    private WrapContentLinearLayoutManager linearLayoutManager;
    private ArrayList<String> selectedImages = new ArrayList<>();
    private ChatState currentChatState;
    private boolean shouldHandleBack = true;
    private final int REQUEST_CAMERA = 1;
    private final int REQUEST_PLACE_PICKER_SEND = 2;
    private static final int REQUEST_GALLERY = 3;
    private String currentPhotoPath;
    public static int index = -1;
    public static int top = -1;
    private String chatUserName; // contact user
    private String currentUser; // this user
    private String botCoverPicture, botDescription, botCategory;
    private MessagesAdapter messagesAdapter;
    private ContactResult contactDetails;
    //send
    private ImageButton galleryButton;
    private View gallerySelector;
    private FloatingActionButton sendView;
    private TextView sendFABBadge;

    private FirebaseAnalytics firebaseAnalytics;
    private Menu menu;
    // userName: id for ejabberd xmpp. userId: id set by user

    //composer
    View smileySelector, audioSelector, gifSelector;
    ImageButton emojiButton, audioButton, gifButton;
    View persistentMenuButton, botLayout;
    GalleryViewHelper.Listener openGalleryClickListener;
    PersistentMenuViewHelper.Listener persistentMenuListener;
    PayuPaymentsHelper payuPaymentsHelper;

    private Intent messageServiceIntent;
    private static final String KEY_CHAT_USER_NAME = "MessageActivity.CHAT_USERNAME";
    private static final String KEY_BOT_COVER = "MessageActivity.BOT_COVER";
    private static final String KEY_BOT_DESCRIPTION = "MessageActivity.BOT_DESCRIPTION";
    private static final String KEY_BOT_CATEGORY = "MessageActivity.BOT_CATEGORY";

    public static Intent callingIntent(Context context, String chatUserName) {
        Logger.d("[MessageActivity] "+chatUserName);
        Intent intent = new Intent(context, MessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_CHAT_USER_NAME, chatUserName);
        return intent;
    }

    public static Intent callingIntent(Context context, String chatUserName, String botCover, String description, String botCategory) {
        Logger.d("[MessageActivity] "+chatUserName);
        Intent intent = new Intent(context, MessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_CHAT_USER_NAME, chatUserName);
        intent.putExtra(KEY_BOT_COVER, botCover);
        intent.putExtra(KEY_BOT_DESCRIPTION, description);
        intent.putExtra(KEY_BOT_CATEGORY, botCategory);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        messageServiceIntent = new Intent(this, MessageService.class);
        messageServiceIntent.putExtra(MessageService.TAG_ACTIVITY_NAME, this.getClass().getName());

        setContentView(R.layout.activity_message);
        ButterKnife.bind(this);
        messagePresenter = new MessagePresenter(MessageStore.getInstance(), MessageController.getInstance(), BotDetailsStore.getInstance(), ContactStore.getInstance(), ApiManager.getUserApi(), ApiManager.getBotApi(), ApiManager.getMessageApi());

        Intent receivedIntent = getIntent();
        if(!receivedIntent.hasExtra(KEY_CHAT_USER_NAME))
            return;
        this.chatUserName = receivedIntent.getStringExtra(KEY_CHAT_USER_NAME);
        if(receivedIntent.hasExtra(KEY_BOT_COVER)) {
            this.botCoverPicture = receivedIntent.getStringExtra(KEY_BOT_COVER);
        } else {
            this.botCoverPicture = "";
        }
        if(receivedIntent.hasExtra(KEY_BOT_DESCRIPTION)) {
            this.botDescription = receivedIntent.getStringExtra(KEY_BOT_DESCRIPTION);
        } else {
            this.botDescription = "";
        }
        if(receivedIntent.hasExtra(KEY_BOT_CATEGORY)) {
            this.botCategory = receivedIntent.getStringExtra(KEY_BOT_CATEGORY);
        } else {
            this.botCategory = "";
        }

        currentChatState = ChatState.inactive;
        currentUser = UserSessionManager.getInstance().load().getUserName();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        index = -1;
        top = -1;
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        NotificationController.getInstance().clearNotification();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(linearLayoutManager!=null) {
            index = linearLayoutManager.findFirstVisibleItemPosition();
            View v = messageList.getChildAt(0);
            top = (v == null) ? 0 : (v.getTop() - messageList.getPaddingTop());
        }

        GenericCache.getInstance().put("draft_"+chatUserName, messageBox.getText().toString());
    }

    @Override
    protected void onResume() {
        if(progressDialog[0]!=null && progressDialog[0].isShowing())
            progressDialog[0].dismiss();
        Logger.d(this, "onResume");
        super.onResume();
        messagePresenter.loadMessages(chatUserName);

        /*              Analytics           */
        firebaseAnalytics.setCurrentScreen(this, AnalyticsConstants.Event.MESSAGE_SCREEN, null);
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
        Logger.d(this, "onBackPressed: "+shouldHandleBack);
        if(shouldHandleBack) {
            firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_BACK, null);
            super.onBackPressed();
        } else {
            AndroidUtils.hideSoftInput(this);
            shouldHandleBack = true;
        }
    }

    @Override
    public void showError(String title, String message) {
        if(progressDialog[0]!=null && progressDialog[0].isShowing())
            progressDialog[0].dismiss();
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
        } else if(id == R.id.info) {
            startActivity(UserProfileActivity.callingIntent(this, chatUserName, contactDetails.getUserId(), contactDetails.getContactName(), contactDetails.isBlocked(), contactDetails.getProfileDP()));
        } else if(id == R.id.favorite) {
            Bundle bundle = new Bundle();
            bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_NAME, chatUserName);
            firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_CLICK_FAVORITE, bundle);
            if(!contactDetails.isAdded()) {
                new AddContactUseCase(ApiManager.getUserApi(), ContactStore.getInstance(), ApiManager.getBotApi(), BotDetailsStore.getInstance())
                        .execute(contactDetails.getUserId(), true)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<ContactResult>() {
                            @Override
                            public void onCompleted() {}

                            @Override
                            public void onError(Throwable e) {}

                            @Override
                            public void onNext(ContactResult contactResult) {}
                        });
                item.setIcon(R.drawable.ic_favorite_filled);
                contactDetails.setAdded(true);
            } else {
                contactDetails.setAdded(false);
                item.setIcon(R.drawable.ic_favorite_border);
                ContactStore.getInstance().update(contactDetails)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<ContactResult>() {
                            @Override
                            public void onCompleted() {}
                            @Override
                            public void onError(Throwable e) {}

                            @Override
                            public void onNext(ContactResult contactResult) {

                            }
                        });
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.messages_toolbar, menu);
        if(contactDetails!=null && contactDetails.isAdded()) {
            Logger.d(this, "ADDED");
            menu.findItem(R.id.favorite).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_favorite_filled));
        } else
            menu.findItem(R.id.favorite).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_favorite_border));
        return true;
    }

    @OnClick(R.id.tb_message_title)
    public void onTBClicked() {
        AndroidUtils.hideSoftInput(this);
        startActivity(UserProfileActivity.callingIntent(this, chatUserName, contactDetails.getUserId(), contactDetails.getContactName(), contactDetails.isBlocked(), contactDetails.getProfileDP()));

        /*              Analytics           */
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_NAME, chatUserName);
        firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_CLICK_DP, bundle);
    }

    public void onSendClicked() {
        String message = messageBox.getText().toString().trim();

        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_ID, contactDetails.getUserId());
        bundle.putString(AnalyticsConstants.Param.MESSAGE, message);
        firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_CLICK_SEND, null);

        if(message.length()>=1) {
            messageBox.setText("");
            Message m = new Message();
            m.setText(message);
            if(contactDetails.isBlocked()) {
                AlertDialog alertDialog = new AlertDialog.Builder(MessageActivity.this).create();
                alertDialog.setMessage(Html.fromHtml("Unblock contact to send message."));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Unblock", (dialog, which) -> {
                    messagePresenter.blockContact(contactDetails.getUserId(), false);
                    progressDialog[0] = ProgressDialog.show(MessageActivity.this, "", "Please wait a moment", true);
                    dialog.dismiss();
                });
                alertDialog.show();
            } else {
                messagePresenter.sendTextMessage(chatUserName, currentUser, GsonProvider.getGson().toJson(m));
            }
        }

        if(selectedImages.size()>0) {
            galleryViewHelper.removeGalleryPickerView();
            galleryButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_gallery));
            gallerySelector.setVisibility(View.GONE);
            sendFABBadge.setVisibility(View.GONE);
            sendView.hide();
            new Handler().postDelayed(() -> {
                sendView.setBackgroundTintList(ColorStateList.valueOf(0xffeeeeee));
                sendView.setImageDrawable(getResources().getDrawable(R.drawable.ic_send_inactive));
                sendView.show();
            }, 125);
            for (String selectedImage : selectedImages) {
                Message m = new Message();
                ImageMessage imageMessage = new ImageMessage();
                imageMessage.setFileUri(selectedImage);
                m.setImageMessage(imageMessage);
                messagePresenter.sendTextMessage(chatUserName, currentUser, GsonProvider.getGson().toJson(m));
            }
            selectedImages.clear();
            galleryViewHelper.removeSelections();
        }
    }

    public void onMessageChanged() {
        String message = messageBox.getText().toString().trim();

        if(message.length()>=1) {
            if(currentChatState != ChatState.composing ) {
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

    @Override
    public void setContactDetails(ContactResult contact) {
        Logger.d(this, "ContactResult: "+contact);
        this.contactDetails = contact;

        title.setText(AndroidUtils.displayNameStyle(contactDetails.getContactName()));

        linearLayoutManager = new WrapContentLinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messagesAdapter = new MessagesAdapter(this, chatUserName, AndroidUtils.displayNameStyle(contactDetails.getContactName()), contact.getProfileDP(), contact.isAdded(),this, this, this, this, this);
        messagesAdapter.setBotDetails(botCoverPicture, botDescription, botCategory);
        messageList.setLayoutManager(linearLayoutManager);

        RecyclerView.ItemAnimator animator = messageList.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        messageList.setAdapter(messagesAdapter);
//        showAddBlock(!contact.isAdded());

        linearLayoutManager.scrollToPositionWithOffset(-1,-1);

        SharedPreferences sharedPreferences = this.getSharedPreferences(LAST_SEEN_PREFS_FILE, Context.MODE_PRIVATE);
        long millis = sharedPreferences.getLong(contactDetails.getUsername(), 0);
        if (millis == 0) {
            if (contactDetails.getUsername().startsWith("o_")) {
                presenceView.setText("Online");
            } else {
                presenceView.setText("Last seen recently");
            }
        } else if ((new DateTime(millis).plusSeconds(5).getMillis() >= DateTime.now().getMillis())) {
            presenceView.setText("Online");
        } else {
            String lastSeen = AndroidUtils.lastActivityAt(new DateTime(millis));
            presenceView.setText(this.getResources().getString(R.string.chat_presence_away, lastSeen));
        }
    }

    @Override
    public void showContactBlockedSuccess(boolean isBlocked) {
        if(progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }
        String message;
        contactDetails.setBlocked(isBlocked);
        if(isBlocked) {
            message = "This contact has been blocked.";
        } else {
            message = "This contact has been unblocked.";
        }

        AlertDialog alertDialog = new AlertDialog.Builder(MessageActivity.this).create();
        alertDialog.setMessage(Html.fromHtml(message));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    @Override
    public void initBotMenu(List<PersistentMenu> persistentMenus) {
        this.persistentMenus = persistentMenus;
    }

    @Override
    public void showHidePersistentMenu(boolean shouldShow) {
        Logger.d(this, "showHidePersistentMenu: "+shouldShow);
        if(shouldShow) {
            persistentMenuButton.setVisibility(View.GONE);
            shouldHandleBack = true;
            persistentMenuViewHelper.addPMView();
            messageBox.requestFocus();
        } else {
            Logger.d(this, "shouldHandleBack1: false");
            shouldHandleBack = false;
            persistentMenuButton.setVisibility(View.VISIBLE);
            persistentMenuViewHelper.removePMPickerView();
        }
    }

    @Override
    public void displayMessages(List<MessageResult> messages) {
        if(messagesAdapter == null) {
            Logger.d(this, "ContactResult: "+contactDetails);
            messagesAdapter = new MessagesAdapter(this, chatUserName, AndroidUtils.displayNameStyle(contactDetails.getContactName()), contactDetails.getProfileDP(), this.contactDetails.isAdded(), this, this, this, this, this);
            messagesAdapter.setBotDetails(botCoverPicture, botDescription, botCategory);
        }
        messagesAdapter.setMessages(messages);
        messagePresenter.sendReadReceipt(chatUserName);

        messagePresenter.getLastActivity(chatUserName);
        if(messages.size()==0) {
            showHideComposer(false);
        } else {
            showHideComposer(true);
        }
    }

    @Override
    public void setKeyboardType(boolean isBotKeyboard) {
        Logger.d(this, "KeyboardType: "+isBotKeyboard);
        LinearLayout rootLayout = (LinearLayout) findViewById(R.id.ll_keyboard);
        rootLayout.removeViewAt(rootLayout.getChildCount()-1);
        if(isBotKeyboard) {
            View botKeyboardView = View.inflate(this, R.layout.layout_bot_keyboard, rootLayout);
            botLayout = botKeyboardView.findViewById(R.id.bot_layout);
            persistentMenuButton = botKeyboardView.findViewById(R.id.message_menu);
            RelativeLayout sendView = (RelativeLayout) botKeyboardView.findViewById(R.id.btn_sendMessage_send);
            FrameLayout pmLayout = (FrameLayout) botKeyboardView.findViewById(R.id.pm_layout);
            MessageEditText messageEditText = (MessageEditText) botKeyboardView.findViewById(R.id.et_sendmessage_message);
            Context context = this;
            messageBox = messageEditText;

            messageEditText.setCursorVisible(false);
            persistentMenuViewHelper = new PersistentMenuViewHelper(this, persistentMenus, pmLayout, getWindow(), new PersistentMenuViewHelper.Listener() {
                @Override
                public void onPostbackClicked(String title, String payload) {
                    Bundle bundle = new Bundle();
                    bundle.putString(AnalyticsConstants.Param.MESSAGE, title);
                    bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_ID, contactDetails.getUserId());
                    firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_PERSISTENT_MENU_CLICK_ITEM, bundle);

                    sendPostbackMessage(title, payload);
                    new Handler().postDelayed(() -> {
                        persistentMenuViewHelper.reset();
                        persistentMenuButton.setVisibility(View.VISIBLE);
                    }, 500);
                }

                @Override
                public void onUrlClicked(String url) {
                    Bundle bundle = new Bundle();
                    bundle.putString(AnalyticsConstants.Param.MESSAGE, url);
                    bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_ID, contactDetails.getUserId());
                    firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_PERSISTENT_MENU_CLICK_ITEM, bundle);

                    startActivity(WebViewActivity.callingIntent(context, url));
                }
            });

            // remove later after regular keyboard change. -------------------------------------------------------
            messageEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() >= 1) {
                        persistentMenuButton.setVisibility(View.GONE);
                        sendView.setVisibility(View.VISIBLE);
                    } else {
                        persistentMenuButton.setVisibility(View.VISIBLE);
                        sendView.setVisibility(View.GONE);
                    }
                    onMessageChanged();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
            // remove later after regular keyboard change. -------------------------------------------------------

            sendView.setOnClickListener(v -> {
                onSendClicked();
            });

            persistentMenuButton.setOnClickListener(v -> {
//                onMessageMenuClicked();
                messageEditText.setCursorVisible(false);
                persistentMenuButton.setVisibility(View.GONE);
                messageEditText.requestFocus();
                Logger.d(this, "shouldHandleBack2: false");
                shouldHandleBack = false;
                persistentMenuViewHelper.addPMView();
                firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_CLICK_PERSISTENT_MENU, null);
            });
            messageEditText.setOnEditTextImeBackListener(() -> {
                persistentMenuButton.setVisibility(View.VISIBLE);
                shouldHandleBack = persistentMenuViewHelper.isNotPMState();
                Logger.d(this, "shouldHandleBack11:"+shouldHandleBack);
                persistentMenuViewHelper.removePMPickerView();
                messageEditText.requestFocus();
            });
            messageEditText.setOnClickListener(v -> {
                messageEditText.setCursorVisible(true);
                persistentMenuButton.setVisibility(View.VISIBLE);
            });

        } else {
            // regular keyboard
            View regularKeyboardView = View.inflate(this, R.layout.layout_user_keyboard, rootLayout);
            sendView = (FloatingActionButton) regularKeyboardView.findViewById(R.id.fab_sendMessage_send);
            sendFABBadge = (TextView) regularKeyboardView.findViewById(R.id.send_fab_badge);
            galleryButton = (ImageButton) regularKeyboardView.findViewById(R.id.btn_sendMessage_gallery);
            emojiButton = (ImageButton) regularKeyboardView.findViewById(R.id.btn_message_smiley);
            audioButton = (ImageButton) regularKeyboardView.findViewById(R.id.btn_sendMessage_audio);
            ImageButton locationButton = (ImageButton) regularKeyboardView.findViewById(R.id.btn_sendMessage_location);
            ImageButton cameraButton = (ImageButton) regularKeyboardView.findViewById(R.id.btn_sendMessage_camera);
            gifButton = (ImageButton) regularKeyboardView.findViewById(R.id.btn_sendMessage_gif);
            smileySelector = regularKeyboardView.findViewById(R.id.smiley_selector);
            audioSelector = regularKeyboardView.findViewById(R.id.audio_selector);
            gallerySelector = regularKeyboardView.findViewById(R.id.gallery_selector);
            gifSelector = regularKeyboardView.findViewById(R.id.gif_selector);
            Context context = this;
            Activity activity = this;
            openGalleryClickListener = new GalleryViewHelper.Listener() {
                @Override
                public void onOpenGalleryClicked() {
                    firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_OPEN_GALLERY, null);

                    int perm1 = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
                    int perm2 = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    int permission = PackageManager.PERMISSION_GRANTED;
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                        if (!(perm1 == permission && perm2 == permission)) {
                            firebaseAnalytics.logEvent(AnalyticsConstants.Event.PERMISSION_STORAGE_SHOW, null);
                            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                        } else {
                            Intent loadIntent = new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(loadIntent, REQUEST_GALLERY);
                        }
                    }
                }

                @Override
                public void onImagesClicked(ArrayList<String> uris) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(AnalyticsConstants.Param.COUNT, uris.size());
                    firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_GALLERY_CLICK_IMAGE, bundle);

                    selectedImages.clear();
                    selectedImages.addAll(uris);
                    if(uris.size()>0) {
                        sendView.hide();
                        new Handler().postDelayed(() -> {
                            sendView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                            sendView.setImageDrawable(getResources().getDrawable(R.drawable.ic_send_white));
                            sendView.show();
                        }, 125);

                        sendFABBadge.setVisibility(View.VISIBLE);
                        sendFABBadge.setText(uris.size()+"");
                    } else {
                        sendFABBadge.setVisibility(View.GONE);
                        sendView.hide();
                        new Handler().postDelayed(() -> {
                            sendView.setBackgroundTintList(ColorStateList.valueOf(0xffeeeeee));
                            sendView.setImageDrawable(getResources().getDrawable(R.drawable.ic_send_inactive));
                            sendView.show();
                        }, 125);
                    }
                }
            };

            MessageEditText messageEditText = (MessageEditText) regularKeyboardView.findViewById(R.id.et_sendmessage_message);
            FrameLayout smileyLayout = (FrameLayout) regularKeyboardView.findViewById(R.id.smiley_layout);
            messageBox = messageEditText;
            emojiViewHelper = new EmojiViewHelper(this, smileyLayout, getWindow());
            audioViewHelper = new AudioViewHelper(this, smileyLayout, getWindow());
            galleryViewHelper = new GalleryViewHelper(this, smileyLayout, getWindow());

            gifViewHelper = new GifViewHelper(this, smileyLayout, getWindow(), new GifViewHelper.GifViewListener() {
                @Override
                public void onSendGif(String url, int w, int h) {
                    gifViewHelper.hide();
                    setComposerSelected(0);

                    messageEditText.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(messageEditText, InputMethodManager.SHOW_IMPLICIT);

                    Logger.d(this, "Gif Width/Height"+w+", "+h);
                    Message m = new Message();
                    ImageMessage imageMessage = new ImageMessage();
                    imageMessage.setImageUrl(url);
                    imageMessage.setWidth(w);
                    imageMessage.setHeight(h);
                    imageMessage.setDataType(ImageMessage.ImageType.gif);
                    m.setImageMessage(imageMessage);

                    messagePresenter.sendTextMessage(chatUserName, currentUser, GsonProvider.getGson().toJson(m));
                }

                @Override
                public void blockBackPress(boolean shouldBlock) {
                    Logger.d(this, "shouldHandleBack10:"+shouldBlock);
                    shouldHandleBack = !shouldBlock;
                }

                @Override
                public void onFocusChanged(boolean hasFocus) {
                    if(!hasFocus) {
                        messageEditText.requestFocus();
                    }
                }
            });

            messageEditText.setOnEditTextImeBackListener(() -> {
                if(!emojiViewHelper.isEmojiState() || !audioViewHelper.isAudioState() || !galleryViewHelper.isGalleryState() || !gifViewHelper.isGifState()) {
                    if(!gifViewHelper.isGifState()) {
                        gifViewHelper.refreshView();
                    }
                    messageEditText.requestFocus();
                    ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                            .showSoftInput(messageEditText, InputMethodManager.SHOW_FORCED);
                    // TODO: hack for showing soft input.
                    new Handler().postDelayed(() -> ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                            .showSoftInput(messageEditText, InputMethodManager.SHOW_FORCED), 500);
                    Logger.d(this, "shouldHandleBack3: false");
                    shouldHandleBack = false;

                    emojiViewHelper.hide();
                    audioViewHelper.hide();
                    galleryViewHelper.hide();
                    gifViewHelper.hide();
                } else {
                    emojiViewHelper.removeEmojiPickerView();
                    audioViewHelper.removeAudioPickerView();
                    galleryViewHelper.removeGalleryPickerView();
                    gifViewHelper.removeGifPickerView();
                    shouldHandleBack = true;
                }

                setComposerSelected(0);
            });

            messageEditText.setOnTouchListener((v, event) -> {
                firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_CLICK_TEXTBOX, null);
                Logger.d(this, "shouldHandleBack4: false");
                shouldHandleBack = false;
                setComposerSelected(0);

                if(!emojiViewHelper.isEmojiState()) {
                    emojiViewHelper.emojiButtonToggle();
                }
                if(!audioViewHelper.isAudioState()) {
                    audioViewHelper.audioButtonToggle();
                }
                if(!galleryViewHelper.isGalleryState()) {
                    galleryViewHelper.galleryButtonToggle();
                }
                if(!gifViewHelper.isGifState()) {
                    gifViewHelper.GifButtonToggle();
                }
                return false;
            });

            emojiButton.setOnClickListener(v -> {
                shouldHandleBack = true;
                audioViewHelper.reset();
                galleryViewHelper.reset();
                gifViewHelper.reset();
                emojiViewHelper.emojiButtonToggle();
                if(!emojiViewHelper.isEmojiState()) {
                    setComposerSelected(1);
                }

                Bundle bundle = new Bundle();
                bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_NAME, this.chatUserName);
                firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_CLICK_SMILEYS, bundle);
            });

            audioButton.setOnClickListener(v -> {
                showAudioLayout();
            });

            galleryButton.setOnClickListener(v -> {
                showGalleryLayout();
            });

            gifButton.setOnClickListener(v -> {
                firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_CLICK_GIF, null);
                shouldHandleBack = true;
                audioViewHelper.reset();
                emojiViewHelper.reset();
                galleryViewHelper.reset();
                gifViewHelper.GifButtonToggle();
                if(!gifViewHelper.isGifState()) {
                    setComposerSelected(6);
                    Logger.d(this, "shouldHandleBack5: false");
                    shouldHandleBack = false;
                }
            });

            emojiViewHelper.setOnEmojiconBackspaceClickedListener(v -> {
                firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_SMILEYS_CLICK_BACKSPACE, null);
                messageEditText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            });
            emojiViewHelper.setOnEmojiconClickedListener(v -> {
                String text = messageEditText.getText() + v.getEmoji();
                messageEditText.setText(text);
                messageEditText.setSelection(text.length());

                /*              Analytics           */
                Bundle bundle = new Bundle();
                bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_ID, this.chatUserName);
                firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_SMILEYS_CLICK_SMILEY, bundle);
            });

            audioViewHelper.setAudioRecordListener(new AudioRecord.AudioRecordListener() {
                @Override
                public void onRecordStart() {}

                @Override
                public void onRecordStop(String fileName) {
                    Message m = new Message();
                    AudioMessage audioMessage = new AudioMessage();
                    audioMessage.setFileUri(fileName);
                    m.setAudioMessage(audioMessage);
                    messagePresenter.sendTextMessage(chatUserName, currentUser, GsonProvider.getGson().toJson(m));
                }

                @Override
                public void onRecordCancel() {}
            });

            cameraButton.setOnClickListener(v -> {
                firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_CLICK_CAMERA, null);
                audioViewHelper.reset();
                emojiViewHelper.reset();
                galleryViewHelper.reset();
                setComposerSelected(0);

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
                        startActivityForResult(cameraIntent, REQUEST_CAMERA);
                    }
                }
            });

            locationButton.setOnClickListener(v -> {
                firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_CLICK_LOCATION, null);
                audioViewHelper.reset();
                emojiViewHelper.reset();
                galleryViewHelper.reset();
                setComposerSelected(0);

                navigateToGetLocation();
            });

            messageBox.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (before == 0 && (s.length() == 1 || (Pattern.compile("(^[\\u20a0-\\u32ff\\ud83c\\udc00-\\ud83d\\udeff\\udbb9\\udce5-\\udbb9\\udcee ]+$)").matcher(s).find()))) {
                        sendView.hide();
                        new Handler().postDelayed(() -> {
                            sendView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                            sendView.setImageDrawable(getResources().getDrawable(R.drawable.ic_send_white));
                            sendView.show();
                        }, 125);
                    } else if(s.length() == 0){
                        sendView.hide();
                        new Handler().postDelayed(() -> {
                            sendView.setBackgroundTintList(ColorStateList.valueOf(0xffeeeeee));
                            sendView.setImageDrawable(getResources().getDrawable(R.drawable.ic_send_inactive));
                            sendView.show();
                        }, 125);
                    } else if(s.length() > 0) {
                        sendView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                        sendView.setImageDrawable(getResources().getDrawable(R.drawable.ic_send_white));
                    }
                    onMessageChanged();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            sendView.setOnClickListener(v -> {
                onSendClicked();
            });
            String draft = GenericCache.getInstance().get("draft_"+chatUserName);
            if(draft!=null) {
                messageBox.setText("");
                messageBox.append(draft);
                sendView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                sendView.setImageDrawable(getResources().getDrawable(R.drawable.ic_send_white));
            }
        }
    }

    public void showHideComposer(boolean shoudShow) {
        if(botLayout==null)
            return;
        if (shoudShow) {
            botLayout.setVisibility(View.VISIBLE);
        } else {
            botLayout.setVisibility(View.GONE);
        }
    }

    public void showAudioLayout() {
        firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_CLICK_AUDIO, null);
        int perm1 = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int perm2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission = PackageManager.PERMISSION_GRANTED;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!(perm1 == permission && perm2 == permission)) {
                firebaseAnalytics.logEvent(AnalyticsConstants.Event.PERMISSION_RECORD_AUDIO_SHOW, null);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 103);
            } else {
                shouldHandleBack = true;
                emojiViewHelper.reset();
                galleryViewHelper.reset();
                gifViewHelper.reset();
                audioViewHelper.audioButtonToggle();
                if(!audioViewHelper.isAudioState()) {
                    setComposerSelected(4);
                }
            }
        } else {
            shouldHandleBack = true;
            emojiViewHelper.reset();
            galleryViewHelper.reset();
            gifViewHelper.reset();
            audioViewHelper.audioButtonToggle();
            if(!audioViewHelper.isAudioState()) {
                setComposerSelected(4);
            }
        }
    }

    public void showGalleryLayout() {
        firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_CLICK_GALLERY, null);
        int perm1 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int perm2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission = PackageManager.PERMISSION_GRANTED;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!(perm1 == permission && perm2 == permission)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 102);
            } else {
                shouldHandleBack = true;
                audioViewHelper.reset();
                emojiViewHelper.reset();
                gifViewHelper.reset();
                galleryViewHelper.galleryButtonToggle();
                if(!galleryViewHelper.isGalleryState()) {
                    setComposerSelected(2);
                    galleryViewHelper.setListener(openGalleryClickListener);
                } else {
                    galleryViewHelper.removeListener();
                }
            }
        } else {
            shouldHandleBack = true;
            audioViewHelper.reset();
            emojiViewHelper.reset();
            gifViewHelper.reset();
            galleryViewHelper.galleryButtonToggle();
            if(!galleryViewHelper.isGalleryState()) {
                setComposerSelected(2);
                galleryViewHelper.setListener(openGalleryClickListener);
            } else {
                galleryViewHelper.removeListener();
            }
        }
    }

    public void setComposerSelected(int pos) { // 0 - none, 1 - smiley, 2 - gallery, 3 - camera, 4 - audio, 5 - location, 6 - gif
        if(pos == 1) {
            smileySelector.setVisibility(View.VISIBLE);
            audioSelector.setVisibility(View.GONE);
            gallerySelector.setVisibility(View.GONE);
            gifSelector.setVisibility(View.GONE);
            gifButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_gif));
            audioButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_mic));
            emojiButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_insert_emoticon_selected));
            galleryButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_gallery));
        } else if(pos == 2) {
            audioSelector.setVisibility(View.GONE);
            smileySelector.setVisibility(View.GONE);
            gallerySelector.setVisibility(View.VISIBLE);
            gifSelector.setVisibility(View.GONE);
            gifButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_gif));
            audioButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_mic));
            emojiButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_insert_emoticon));
            galleryButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_gallery_selected));
        } else if(pos == 4) {
            audioSelector.setVisibility(View.VISIBLE);
            smileySelector.setVisibility(View.GONE);
            gallerySelector.setVisibility(View.GONE);
            gifSelector.setVisibility(View.GONE);
            gifButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_gif));
            audioButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_mic_selected));
            emojiButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_insert_emoticon));
            galleryButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_gallery));
        } else if(pos == 6) {
            audioSelector.setVisibility(View.GONE);
            smileySelector.setVisibility(View.GONE);
            gallerySelector.setVisibility(View.GONE);
            gifSelector.setVisibility(View.VISIBLE);
            gifButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_gif_selected));
            audioButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_mic));
            emojiButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_insert_emoticon));
            galleryButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_gallery));
        } else {
            audioSelector.setVisibility(View.GONE);
            smileySelector.setVisibility(View.GONE);
            gallerySelector.setVisibility(View.GONE);
            gifSelector.setVisibility(View.GONE);
            gifButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_gif));
            audioButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_mic));
            emojiButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_insert_emoticon));
            galleryButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_gallery));
        }
    }

    @Override
    public void addMessageToList(MessageResult message) {
        if(messagesAdapter!=null) {
            messagesAdapter.addMessage(message);
            messageList.scrollToPosition(messagesAdapter.getItemCount()-1);
            startService(messageServiceIntent);
        }
    }

    @Override
    public void updateDeliveryStatus(MessageResult messageResult) {
        if(messagesAdapter!=null)
            messagesAdapter.updateMessage(messageResult);
    }

    @Override
    public void updateDeliveryStatus(String messageId, String deliveryReceiptId, MessageResult.MessageStatus messageStatus) {
        if(messagesAdapter!=null)
            messagesAdapter.updateDeliveryStatus(messageId, deliveryReceiptId, messageStatus);
    }

    @Override
    public void updateLastActivity(String time) {
        if(time != null && !time.isEmpty()) {
            presenceView.setVisibility(View.VISIBLE);
            presenceView.setText(time);
        } else {}
    }

    @Override
    public void sendPostbackMessage(String message, String payload) {
        Message m = new Message();
        m.setText(message);
        if(payload!=null && !payload.isEmpty()) {
            m.setPayload(payload);
        }
        if(contactDetails.isBlocked()) {
            AlertDialog alertDialog = new AlertDialog.Builder(MessageActivity.this).create();
            alertDialog.setMessage(Html.fromHtml("Unblock contact to send message."));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Unblock", (dialog, which) -> {
                messagePresenter.blockContact(contactDetails.getUserId(), false);
                progressDialog[0] = ProgressDialog.show(MessageActivity.this, "", "Please wait a moment", true);
                dialog.dismiss();
            });
            alertDialog.show();
        } else {
            messagePresenter.sendTextMessage(chatUserName, currentUser, GsonProvider.getGson().toJson(m));
        }
        showHideComposer(true);
    }

    @Override
    public void navigateToGetLocation() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), REQUEST_PLACE_PICKER_SEND);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
            showError("Error", "Google play services is not available");
        }
    }

    @Override
    public void urlButtonClicked(String url) {
        startActivity(WebViewActivity.callingIntent(this, url));
    }

    @Override
    public void onPaymentButtonClicked(String amount, String txnid, String productInfo, String udf1, String udf2, String udf3, String udf4, String udf5, String sUrl, String fUrl) {
        MessageActivity messageActivity = MessageActivity.this;
        payuPaymentsHelper = new PayuPaymentsHelper(this, amount, txnid, productInfo, sUrl, fUrl, new PayuPaymentsHelper.PaymentsInterface() {
            @Override
            public void startPaymentsActivity(Intent intent, int requestCode) {
                Logger.d(this, "startActivity: ");
                startActivityForResult(intent, requestCode);
            }
        });
        payuPaymentsHelper.setUdf1(udf1);
        payuPaymentsHelper.setUdf2(udf2);
        payuPaymentsHelper.setUdf3(udf3);
        payuPaymentsHelper.setUdf4(udf4);
        payuPaymentsHelper.setUdf5(udf5);

        SavedCardsStore.getInstance().getCards()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<SavedCardModel>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(List<SavedCardModel> savedCardModels) {
                        if(savedCardModels!=null && savedCardModels.size() > 0) {
                            BottomSheetDialog bottomSheerDialog = new BottomSheetDialog(MessageActivity.this);
                            View parentView = getLayoutInflater().inflate(R.layout.bottomsheet_checkout,null);
                            bottomSheerDialog.setContentView(parentView);
                            BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from((View) parentView.getParent());
                            bottomSheetBehavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
                            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,200,getResources().getDisplayMetrics());
                            bottomSheerDialog.show();

                            LinearLayout linearLayout = (LinearLayout) parentView.findViewById(R.id.ll_cards);
                            LinearLayout addCard = (LinearLayout) parentView.findViewById(R.id.ll_add_card);
                            LinearLayout addNB = (LinearLayout) parentView.findViewById(R.id.ll_add_netbanking);

                            addCard.setOnClickListener(v -> {
                                bottomSheerDialog.dismiss();
                                messageActivity.showAddCardDialog();
                            });
                            addNB.setOnClickListener(v -> {});

                            for (SavedCardModel savedCardModel : savedCardModels) {
                                ImageView cardImage = new ImageView(messageActivity);
                                cardImage.setPadding(0, (int)AndroidUtils.px(6), 0, (int)AndroidUtils.px(6));
                                switch (savedCardModel.getCardType()) {
                                    case "MAST":
                                        cardImage.setImageResource(R.drawable.visa);
                                        break;
                                    case "VISA":
                                        cardImage.setImageResource(R.drawable.master);
                                        break;
                                    case "AMEX":
                                        cardImage.setImageResource(R.drawable.amex);
                                        break;
                                }

                                FrameLayout.LayoutParams cardParams = new FrameLayout.LayoutParams((int)AndroidUtils.px(84), (int)AndroidUtils.px(48), Gravity.CENTER);
                                cardImage.setLayoutParams(cardParams);

                                TextView textView = new TextView(messageActivity);
                                FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, (int)AndroidUtils.px(48), Gravity.CENTER);
                                textView.setGravity(Gravity.CENTER_VERTICAL);
                                textView.setLayoutParams(textParams);
                                textView.setTextSize(16);
                                textView.setTextColor(ContextCompat.getColor(messageActivity, R.color.textColor));
                                textView.setText(savedCardModel.getCardNumber());

                                LinearLayout LL = new LinearLayout(messageActivity);
                                LL.setOrientation(LinearLayout.HORIZONTAL);

                                LinearLayoutCompat.LayoutParams LLParams = new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
                                LL.setLayoutParams(LLParams);
                                LL.addView(cardImage);
                                LL.addView(textView);

                                LL.setOnClickListener(v -> {
                                    progressDialog[0] = ProgressDialog.show(MessageActivity.this, "", "Loading. Please wait...", true);
                                    bottomSheerDialog.dismiss();
                                    ApiManager.getPaymentApi().getPaymentCardHashById(savedCardModel.getServerId())
                                            .subscribeOn(Schedulers.newThread())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Subscriber<MerchantHashResponse>() {
                                                @Override
                                                public void onCompleted() {

                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    e.printStackTrace();
                                                }

                                                @Override
                                                public void onNext(MerchantHashResponse merchantHashResponse) {
                                                    MerchantHashResponse.Hash hash = merchantHashResponse.getMerchantHashes().get(0);
                                                    payuPaymentsHelper.makePaymentByStoredCard(hash.getCardToken(), hash.getMerchantHash());
                                                }
                                            });
                                });

                                linearLayout.addView(LL);
                            }
                        } else {
                            messageActivity.showAddCardDialog();
                        }
                    }
                });
    }

    public void showAddCardDialog() {

        LayoutInflater inflater = this.getLayoutInflater();
        View v = inflater.inflate(R.layout.popup_enter_card, null);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(v);

        Button proceed = (Button)v.findViewById(R.id.btn_add_contact);
        EditText cardNumber = (EditText) v.findViewById(R.id.et_card_number);
        EditText expiry = (EditText) v.findViewById(R.id.et_expiry);
        EditText cvv = (EditText) v.findViewById(R.id.et_cvv);

        expiry.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {


                if (editable.length() > 0 && (editable.length() % 3) == 0) {
                    final char c = editable.charAt(editable.length() - 1);
                    if ('/' == c) {
                        editable.delete(editable.length() - 1, editable.length());
                    }
                }
                if (editable.length() > 0 && (editable.length() % 3) == 0) {
                    char c = editable.charAt(editable.length() - 1);
                    if (Character.isDigit(c) && TextUtils.split(editable.toString(), String.valueOf("/")).length <= 2) {
                        editable.insert(editable.length() - 1, String.valueOf("/"));
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getWindow().setLayout((int)AndroidUtils.px(328), ViewGroup.LayoutParams.WRAP_CONTENT);
        cardNumber.requestFocus();
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        alertDialog.setOnDismissListener(dialog -> {
            ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(cardNumber.getWindowToken(), 0);
        });

        proceed.setOnClickListener(v1 -> {
            String exp[] = expiry.getText().toString().split("/");
            String month = exp[0];
            String year = "20"+exp[1];
            alertDialog.dismiss();
            payuPaymentsHelper.makePaymentByCreditCard(cardNumber.getText().toString(), month, year, cvv.getText().toString());
        });
    }

    @Override
    public void onMessageReceived(MessageResult messageResult, ContactResult contactResult) {
        if(messageResult.getChatId().equals(chatUserName) && messagesAdapter!=null) {
            messagesAdapter.addMessage(messageResult);
            messageList.scrollToPosition(messagesAdapter.getItemCount() - 1);
            messagePresenter.updateMessageRead(messageResult);
            final MediaPlayer mp = MediaPlayer.create(this, R.raw.conversation_tone);
            mp.start();
            NotificationController.getInstance().clearNotification();
        } else {
            NotificationController.getInstance().showNotificationAndAlert(true);
        }
    }

    @Override
    public void onChatStateReceived(String from, ChatState chatState) {
        super.onChatStateReceived(from, chatState);
        if(from.equals(chatUserName)) {
            if(chatState == ChatState.composing) {
                presenceView.setText(getResources().getString(R.string.chat_state_typing));
                messagesAdapter.setTyping(true);
                messageList.scrollToPosition(messagesAdapter.getItemCount() - 1);
                final Handler handler = new Handler();
                handler.postDelayed(() -> messagesAdapter.setTyping(false), 10000);
            } else {
                messagePresenter.getLastActivity(this.chatUserName);
            }
        }
    }

    @Override
    public void onPresenceChanged(String username, Presence.Type type) {
        super.onPresenceChanged(username, type);
        if(username.equals(chatUserName)) {
            presenceView.setVisibility(View.VISIBLE);
            if(type == Presence.Type.available) {
                presenceView.setText(getResources().getString(R.string.chat_presence_online));
            } else if(type == Presence.Type.unavailable) {
                DateTime timeNow = DateTime.now();
                presenceView.setText(getResources().getString(R.string.chat_presence_away, AndroidUtils.lastActivityAt(timeNow)));
            }
        }
    }

    @Override
    public void onMessageStatusReceived(String messageId, String chatId, String deliveryReceiptId, MessageResult.MessageStatus messageStatus) {
        super.onMessageStatusReceived(messageId, chatId, deliveryReceiptId, messageStatus);
        if(this.chatUserName.equals(chatId)) {
            new Handler().postDelayed(() -> updateDeliveryStatus(messageId, deliveryReceiptId, messageStatus), 200);
        }
    }

    @Override
    public void onContactBlockedClicked() {
        LinearLayout parent = new LinearLayout(this);

        parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setPadding((int)AndroidUtils.px(24), (int)AndroidUtils.px(8), (int)AndroidUtils.px(24), 0);

        TextView textView1 = new TextView(this);
        textView1.setText("Are you sure want to block this contact?");
        textView1.setTextColor(ContextCompat.getColor(this, R.color.textColor));
        textView1.setTextSize(16);

        parent.addView(textView1);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.app_name));
        builder.setPositiveButton("OK", ((dialog, which) -> {
            blockUnblockContact(!contactDetails.isAdded());
            progressDialog[0] = ProgressDialog.show(MessageActivity.this, "", "Please wait a moment", true);
        }));
        builder.setNegativeButton("CANCEL", ((dialog, which) -> {}));
        builder.setView(parent);
        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_NAME, this.chatUserName);
        firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_CLICK_BLOCK_CONTACT, bundle);
    }

    @Override
    public void onContactReportSpamClicked() {
        LinearLayout parent = new LinearLayout(this);

        parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setPadding((int)AndroidUtils.px(24), (int)AndroidUtils.px(8), (int)AndroidUtils.px(24), 0);

        TextView textView1 = new TextView(this);
        textView1.setText("Are you sure want to report and block this contact?");
        textView1.setTextColor(ContextCompat.getColor(this, R.color.textColor));
        textView1.setTextSize(16);

        parent.addView(textView1);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.app_name));
        builder.setPositiveButton("REPORT AND BLOCK", ((dialog, which) -> {
            blockUnblockContact(true);
            delete(contactDetails.getUsername(), false);
            progressDialog[0] = ProgressDialog.show(MessageActivity.this, "", "Please wait a moment", true);
        }));
        builder.setNegativeButton("CANCEL", ((dialog, which) -> {}));
        builder.setView(parent);
        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK && data!=null) {
            if(currentPhotoPath!=null) {
                Logger.d(this, "Got image");
                Message m = new Message();
                ImageMessage imageMessage = new ImageMessage();
                imageMessage.setFileUri(currentPhotoPath);
                m.setImageMessage(imageMessage);

                messagePresenter.sendTextMessage(chatUserName, currentUser, GsonProvider.getGson().toJson(m));
            }
        } else if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK && data!=null) {
            Logger.d(this, "Gallery");
            Uri selectedImage = data.getData();
            Logger.d(this, selectedImage.toString());
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            if(cursor!=null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                Logger.d(this, "Got gallery pic: " + picturePath);
                Message m = new Message();
                ImageMessage imageMessage = new ImageMessage();
                imageMessage.setFileUri(picturePath);
                m.setImageMessage(imageMessage);

                messagePresenter.sendTextMessage(chatUserName, currentUser, GsonProvider.getGson().toJson(m));
                cursor.close();
            }
        } else if (requestCode == REQUEST_PLACE_PICKER_SEND) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);

                if(place!=null) {
                    Message m = new Message();
                    String placeName="", address="";
                    if(place.getName()!=null) {
                        placeName = place.getName().toString();
                    }
                    if(place.getAddress()!=null) {
                        address = place.getAddress().toString();
                    }
                    LocationMessage locationMessage = new LocationMessage(place.getLatLng().latitude, place.getLatLng().longitude, placeName, address);
                    m.setLocationMessage(locationMessage);

                    if (contactDetails.isBlocked()) {
                        AlertDialog alertDialog = new AlertDialog.Builder(MessageActivity.this).create();
                        alertDialog.setMessage(Html.fromHtml("Unblock contact to send message."));
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Unblock", (dialog, which) -> {
                            messagePresenter.blockContact(contactDetails.getUserId(), false);
                            progressDialog[0] = ProgressDialog.show(MessageActivity.this, "", "Please wait a moment", true);
                            dialog.dismiss();
                        });
                        alertDialog.show();
                    } else {
                        messagePresenter.sendTextMessage(chatUserName, currentUser, GsonProvider.getGson().toJson(m));
                    }
                }
            }
        } else if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            if (data != null) {

                /**
                 * Here, data.getStringExtra("payu_response") ---> Implicit response sent by PayU
                 * data.getStringExtra("result") ---> Response received from merchant's Surl/Furl
                 *
                 * PayU sends the same response to merchant server and in app. In response check the value of key "status"
                 * for identifying status of transaction. There are two possible status like, success or failure
                 * */
                new android.app.AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage("Payu's Data : " + data.getStringExtra("payu_response") + "\n\n\n Merchant's Data: " + data.getStringExtra("result"))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }).show();

                try {
                    JSONObject jsonObject = new JSONObject(data.getStringExtra("payu_response"));
                    String cardToken = jsonObject.getString("card_token");
                    String cardNumber = jsonObject.getString("card_no");
                    String cardType = jsonObject.getString("card_type");
                    String merchatnHash = jsonObject.getString("merchant_hash");
                    String merchantKey = jsonObject.getString("merchant_key");

                    MerchantHashRequest merchantHashRequest = new MerchantHashRequest();
                    merchantHashRequest.setMerchantKey(merchantKey);
                    merchantHashRequest.setCardToken(cardToken);
                    merchantHashRequest.setMerchantHash(merchatnHash);
                    merchantHashRequest.setCardNumberMasked(cardNumber);
                    merchantHashRequest.setCardType(cardType);

                    Logger.d(this, "Got Merchant Hash");
                    ApiManager.getPaymentApi().updarePaymentCardHash(merchantHashRequest)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<MerchantHashResponse>() {
                                @Override
                                public void onCompleted() {

                                }

                                @Override
                                public void onError(Throwable e) {
                                    Logger.d(this, e.toString());
                                    e.printStackTrace();
                                }

                                @Override
                                public void onNext(MerchantHashResponse merchantHashResponse) {
                                    MerchantHashResponse.Hash hash = merchantHashResponse.getMerchantHashes().get(0);
                                    SavedCardsStore.getInstance().putCard(hash.getCardNumberMasked(), hash.getCardType(), hash.getServerId())
                                            .subscribeOn(Schedulers.newThread())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Subscriber<Boolean>() {
                                                @Override
                                                public void onCompleted() {

                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    e.printStackTrace();
                                                }

                                                @Override
                                                public void onNext(Boolean aBoolean) {

                                                }
                                            });
                                }
                            });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(this, getString(R.string.could_not_receive_data), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    firebaseAnalytics.logEvent(AnalyticsConstants.Event.PERMISSION_STORAGE_ALLOW, null);
                    Intent loadIntent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(loadIntent, REQUEST_GALLERY);
                } else {
                    firebaseAnalytics.logEvent(AnalyticsConstants.Event.PERMISSION_STORAGE_DENY, null);
                }
                break;
            case 102:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    firebaseAnalytics.logEvent(AnalyticsConstants.Event.PERMISSION_STORAGE_ALLOW, null);
                    showGalleryLayout();
                } else {
                    firebaseAnalytics.logEvent(AnalyticsConstants.Event.PERMISSION_STORAGE_DENY, null);
                }
                break;
            case 103:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    firebaseAnalytics.logEvent(AnalyticsConstants.Event.PERMISSION_RECORD_AUDIO_ALLOW, null);
                    showAudioLayout();
                } else {
                    firebaseAnalytics.logEvent(AnalyticsConstants.Event.PERMISSION_RECORD_AUDIO_DENY, null);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // woraround for samsung devices, IndexOutOfBoundsException: Inconsistency detected. Invalid view holder adapter positionViewHolder
    private class WrapContentLinearLayoutManager extends LinearLayoutManager {
        WrapContentLinearLayoutManager(Context context) {
            super(context);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                Logger.e(this, "meet a IOOBE in RecyclerView");
            }
        }
    }

    @OnClick(R.id.iv_back)
    public void onBackClick() {
        super.onBackPressed();
    }

    public void blockUnblockContact(boolean shouldBlock) {
        ContactResult contactResult = new ContactResult();
        contactResult.setUserId(contactDetails.getUserId());
        contactResult.setBlocked(shouldBlock);
        Activity activity = MessageActivity.this;
        ApiManager.getContactApi().blockContact(contactDetails.getUserId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserResponse>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(UserResponse userResponse) {
                        Observable<UserResponse> a;
                        if(shouldBlock) {
                            a = ApiManager.getContactApi().blockContact(contactDetails.getUserId());
                        } else {
                            a = ApiManager.getContactApi().unblockContact(contactDetails.getUserId());
                        }
                        a.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<UserResponse>() {
                                    @Override
                                    public void onCompleted() {}

                                    @Override
                                    public void onError(Throwable e) {
                                        e.printStackTrace();
                                        ApiError error = new ApiError(e);
                                        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(activity).create();
                                        alertDialog.setTitle(error.getTitle());
                                        alertDialog.setMessage("\n"+error.getMessage());
                                        alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
                                        alertDialog.show();
                                    }

                                    @Override
                                    public void onNext(UserResponse userResponse) {
                                        contactResult.setBlocked(shouldBlock);
                                        ContactStore.getInstance().update(contactResult)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Subscriber<ContactResult>() {
                                                    @Override
                                                    public void onCompleted() {}

                                                    @Override
                                                    public void onError(Throwable e) {}

                                                    @Override
                                                    public void onNext(ContactResult contactResult) {
                                                        if(progressDialog[0].isShowing()) {
                                                            progressDialog[0].dismiss();
                                                        }
                                                        String message;
                                                        if(shouldBlock) {
                                                            message = "This contact has been blocked.";
                                                            menu.findItem(R.id.action_block_contact).setTitle("Unblock");
                                                        }
                                                        else {
                                                            message = "This contact has been unblocked.";
                                                            menu.findItem(R.id.action_block_contact).setTitle("Block");
                                                        }
                                                        contactDetails.setBlocked(!contactDetails.isBlocked());

                                                        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(activity).create();
                                                        alertDialog.setMessage(Html.fromHtml(message));
                                                        alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
                                                        alertDialog.show();
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    public void delete(String username, boolean closeActivity) {
        MessageStore.getInstance().deleteChat(username)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(Boolean aBoolean) {
                        ContactStore.getInstance().deleteContactUsername(username);
                    }
                });
        if(closeActivity)
            startActivity(HomeTabActivity.callingIntent(this, 0, null));
    }
}