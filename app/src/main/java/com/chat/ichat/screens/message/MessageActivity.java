package com.chat.ichat.screens.message;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
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
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.chat.ichat.MessageController;
import com.chat.ichat.R;
import com.chat.ichat.UserSessionManager;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.bot.PersistentMenu;
import com.chat.ichat.config.AnalyticsContants;
import com.chat.ichat.core.BaseActivity;
import com.chat.ichat.core.GsonProvider;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.NotificationController;
import com.chat.ichat.core.lib.AndroidUtils;
import com.chat.ichat.core.lib.CircleTransformation;
import com.chat.ichat.core.lib.ImageUtils;
import com.chat.ichat.db.BotDetailsStore;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.db.GenericCache;
import com.chat.ichat.db.MessageStore;
import com.chat.ichat.models.AudioMessage;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.models.ImageMessage;
import com.chat.ichat.models.LocationMessage;
import com.chat.ichat.models.Message;
import com.chat.ichat.models.MessageResult;
import com.chat.ichat.screens.message.audio.AudioRecord;
import com.chat.ichat.screens.message.audio.AudioViewHelper;
import com.chat.ichat.screens.message.emoji.EmojiViewHelper;
import com.chat.ichat.screens.message.gallery.GalleryViewHelper;
import com.chat.ichat.screens.message.gif.GifViewHelper;
import com.chat.ichat.screens.user_profile.UserProfileActivity;
import com.chat.ichat.screens.web_view.WebViewActivity;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MessageActivity extends BaseActivity
        implements  MessageContract.View,
                    MessagesAdapter.PostbackClickListener,
                    MessagesAdapter.UrlClickListener,
                    MessagesAdapter.QuickReplyActionListener{
    private MessagePresenter messagePresenter;

    @Bind(R.id.rv_messageitem) RecyclerView messageList;
    @Bind(R.id.tb_message) Toolbar toolbar;
    @Bind(R.id.tb_message_title) TextView title;
    @Bind(R.id.container) RelativeLayout rootLayout;
    @Bind(R.id.message_add_block) LinearLayout addBlockView;
    @Bind(R.id.iv_profile_image) ImageView profileImage;
    @Bind(R.id.tb_message_presence)
    TextView presenceView;

    EditText messageBox;

    final ProgressDialog[] progressDialog = new ProgressDialog[1];
    private EmojiViewHelper emojiViewHelper;
    private AudioViewHelper audioViewHelper;
    private GalleryViewHelper galleryViewHelper;
    private GifViewHelper gifViewHelper;

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
    private static final String KEY_CHAT_USER_NAME = "MessageActivity.CHAT_USERNAME";
    private String chatUserName; // contact user
    private String currentUser; // this user
    private MessagesAdapter messagesAdapter;
    private ContactResult contactDetails;
    //send
    private ImageButton galleryButton;
    private View gallerySelector;
    private FloatingActionButton sendView;
    private TextView sendFABBadge;

    private FirebaseAnalytics firebaseAnalytics;
    private final String SCREEN_NAME = "message";
    // userName: id for ejabberd xmpp. userId: id set by user

    //composer
    View smileySelector, audioSelector, gifSelector;
    ImageButton emojiButton, audioButton, gifButton;

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
        messagePresenter = new MessagePresenter(MessageStore.getInstance(), MessageController.getInstance(), BotDetailsStore.getInstance(), ContactStore.getInstance(), ApiManager.getUserApi(), ApiManager.getBotApi(), ApiManager.getMessageApi());

        Intent receivedIntent = getIntent();
        if(!receivedIntent.hasExtra(KEY_CHAT_USER_NAME))
            return;
        this.chatUserName = receivedIntent.getStringExtra(KEY_CHAT_USER_NAME);

        currentChatState = ChatState.inactive;
        currentUser = UserSessionManager.getInstance().load().getUserName();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
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

        GenericCache.getInstance().put("draft_"+chatUserName, messageBox.getText().toString());
    }

    @Override
    protected void onResume() {
        Logger.d(this, "onResume");
        super.onResume();
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

    @Override
    public void showError(String title, String message) {
        if(progressDialog[0].isShowing())
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.messages_toolbar, menu);
        return true;
    }

    @OnClick(R.id.tb_message_title)
    public void onTBClicked() {
        AndroidUtils.hideSoftInput(this);
        startActivity(UserProfileActivity.callingIntent(this, chatUserName, contactDetails.getUserId(), contactDetails.getContactName(), contactDetails.isBlocked(), contactDetails.getProfileDP()));

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
        firebaseAnalytics.logEvent(AnalyticsContants.Event.MESSAGE_DP_CLICK, bundle);
    }

    public void onSendClicked() {
        String message = messageBox.getText().toString().trim();

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
                messagePresenter.sendImageMessage(chatUserName, currentUser, selectedImage);
            }
            selectedImages.clear();
            galleryViewHelper.removeSelections();
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
        Logger.d(this, "ContactDetails: "+contact);
        this.contactDetails = contact;

        title.setText(AndroidUtils.displayNameStyle(contactDetails.getContactName()));

        linearLayoutManager = new WrapContentLinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messagesAdapter = new MessagesAdapter(this, chatUserName, AndroidUtils.displayNameStyle(contactDetails.getContactName()), contact.getProfileDP(), this, this, this);
        messageList.setLayoutManager(linearLayoutManager);

        RecyclerView.ItemAnimator animator = messageList.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        messageList.setAdapter(messagesAdapter);
        showAddBlock(!contact.isAdded());

        if(contact.getProfileDP()!=null && !contact.getProfileDP().isEmpty()) {
            Glide.with(this)
                    .load(contact.getProfileDP().replace("https://", "http://"))
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(ImageUtils.getDefaultProfileImage(contact.getContactName(), contact.getUsername(), 18))
                    .bitmapTransform(new CenterCrop(this), new CircleTransformation(this))
                    .into(profileImage);
        } else {
            profileImage.setImageDrawable(ImageUtils.getDefaultProfileImage(contact.getContactName(), contact.getUsername(), 18));
        }
        linearLayoutManager.scrollToPositionWithOffset(-1,-1);
    }

    @Override
    public void showAddBlock(boolean shouldShow) {
        Logger.d(this, "showAddBlock: "+shouldShow);
        if(shouldShow) {
            TextView addView = (TextView) findViewById(R.id.message_add);
            TextView blockView = (TextView) findViewById(R.id.message_block);
            if(contactDetails.isBlocked()) {
                blockView.setText("UNBLOCK");
            } else {
                blockView.setText("BLOCK");
            }
            addBlockView.setVisibility(View.VISIBLE);
            addView.setOnClickListener(v -> {
                messagePresenter.addContact(contactDetails.getUserId());
                progressDialog[0] = ProgressDialog.show(MessageActivity.this, "", "Please wait a moment", true);

                /*              Analytics           */
                Bundle bundle = new Bundle();
                bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, this.chatUserName);
                firebaseAnalytics.logEvent(AnalyticsContants.Event.OTHER_ADD_CONTACT, bundle);
            });
            blockView.setOnClickListener(v -> {
                String message = "Are you sure you want to block <b>" + AndroidUtils.displayNameStyle(contactDetails.getContactName()) + "</b>?";;
                AlertDialog alertDialog = new AlertDialog.Builder(MessageActivity.this).create();
                alertDialog.setMessage(Html.fromHtml(message));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", (dialog, which) -> {
                    messagePresenter.blockContact(contactDetails.getUserId(), true);
                    progressDialog[0] = ProgressDialog.show(MessageActivity.this, "", "Please wait a moment", true);
                    dialog.dismiss();
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", (dialog, which) -> {
                    dialog.cancel();
                });
                if(contactDetails.isBlocked()) {
                    messagePresenter.blockContact(contactDetails.getUserId(), false);
                    progressDialog[0] = ProgressDialog.show(MessageActivity.this, "", "Please wait a moment", true);
                } else {
                    alertDialog.show();
                }

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
        if(progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }
        message = "<b>" + AndroidUtils.displayNameStyle(contactDetails.getContactName()) + "</b> is added to your contacts on iChat.";

        AlertDialog alertDialog = new AlertDialog.Builder(MessageActivity.this).create();
        alertDialog.setMessage(Html.fromHtml(message));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();

        alertDialog.setOnDismissListener(dialog -> {
            showAddBlock(false);
        });
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

        alertDialog.setOnDismissListener(dialog -> {
            showAddBlock(true);
        });
    }

    @Override
    public void initBotMenu(List<PersistentMenu> persistentMenus) {
        this.persistentMenus = persistentMenus;
    }

    @Override
    public void displayMessages(List<MessageResult> messages) {
        if(messagesAdapter == null) {
            messagesAdapter = new MessagesAdapter(this, chatUserName, AndroidUtils.displayNameStyle(contactDetails.getContactName()), contactDetails.getProfileDP(), this, this, this);
        }
        messagesAdapter.setMessages(messages);
        messagePresenter.sendReadReceipt(chatUserName);

        messagePresenter.getLastActivity(chatUserName);
    }

    @Override
    public void setKeyboardType(boolean isBotKeyboard) {
        Logger.d(this, "KeyboardType: "+isBotKeyboard);
        LinearLayout rootLayout = (LinearLayout) findViewById(R.id.ll_keyboard);
        rootLayout.removeViewAt(rootLayout.getChildCount()-1);
        if(isBotKeyboard) {
            View botKeyboardView = View.inflate(this, R.layout.layout_bot_keyboard, rootLayout);
            RelativeLayout sendView = (RelativeLayout) botKeyboardView.findViewById(R.id.btn_sendMessage_send);
            messageBox = (EditText) botKeyboardView.findViewById(R.id.et_sendmessage_message);
            botKeyboardView.findViewById(R.id.message_menu).setOnClickListener(v -> onMessageMenuClicked());

            // remove later after regular keyboard change. -------------------------------------------------------
            messageBox.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() >= 1) {
                        sendView.setVisibility(View.VISIBLE);
                        sendView.setBackgroundResource(R.drawable.bg_send_active);
                    } else {
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
            GalleryViewHelper.Listener openGalleryClickListener = new GalleryViewHelper.Listener() {
                @Override
                public void onOpenGalleryClicked() {
                    int perm1 = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
                    int perm2 = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    int permission = PackageManager.PERMISSION_GRANTED;
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                        if (!(perm1 == permission && perm2 == permission)) {
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
            gifViewHelper = new GifViewHelper(this, smileyLayout, getWindow(), (url, w,h) -> {
                Logger.d(this, "Gif Width/Height"+w+", "+h);
                Message m = new Message();
                ImageMessage imageMessage = new ImageMessage();
                imageMessage.setImageUrl(url);
                imageMessage.setWidth(w);
                imageMessage.setHeight(h);

                m.setImageMessage(imageMessage);

                messagePresenter.sendTextMessage(chatUserName, currentUser, GsonProvider.getGson().toJson(m));
            });

            messageEditText.setOnEditTextImeBackListener(() -> {
                if(!emojiViewHelper.isEmojiState() || !audioViewHelper.isAudioState() || !galleryViewHelper.isGalleryState() || !gifViewHelper.isGifState()) {
                    shouldHandleBack = false;
                    emojiViewHelper.reset();
                    audioViewHelper.reset();
                    galleryViewHelper.reset();
                    gifViewHelper.reset();

                    messageEditText.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(messageEditText, InputMethodManager.SHOW_IMPLICIT);

                } else {
                    shouldHandleBack = true;
                }

                emojiViewHelper.removeEmojiPickerView();
                audioViewHelper.removeAudioPickerView();
                galleryViewHelper.removeGalleryPickerView();
                gifViewHelper.removeGifPickerView();

                setComposerSelected(0);
            });

            messageEditText.setOnTouchListener((v, event) -> {
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

                /*              Analytics           */
                Bundle bundle = new Bundle();
                bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, this.chatUserName);
                firebaseAnalytics.logEvent(AnalyticsContants.Event.MESSAGE_SMILEY, bundle);
            });

            audioButton.setOnClickListener(v -> {
                shouldHandleBack = true;
                emojiViewHelper.reset();
                galleryViewHelper.reset();
                gifViewHelper.reset();
                audioViewHelper.audioButtonToggle();
                if(!audioViewHelper.isAudioState()) {
                    setComposerSelected(4);
                }
            });

            galleryButton.setOnClickListener(v -> {
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
            });

            gifButton.setOnClickListener(v -> {
                shouldHandleBack = true;
                audioViewHelper.reset();
                emojiViewHelper.reset();
                galleryViewHelper.reset();
                gifViewHelper.GifButtonToggle();
                if(!gifViewHelper.isGifState()) {
                    setComposerSelected(6);
                    shouldHandleBack = false;
                }
            });

            emojiViewHelper.setOnEmojiconBackspaceClickedListener(v -> messageEditText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)));
            emojiViewHelper.setOnEmojiconClickedListener(v -> {
                String text = messageEditText.getText() + v.getEmoji();
                messageEditText.setText(text);
                messageEditText.setSelection(text.length());

                /*              Analytics           */
                Bundle bundle = new Bundle();
                bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, this.chatUserName);
                firebaseAnalytics.logEvent(AnalyticsContants.Event.SMILEY_SELECTED, bundle);
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
                    messagePresenter.sendAudioMessage(chatUserName, currentUser, fileName);
                }

                @Override
                public void onRecordCancel() {}
            });

            cameraButton.setOnClickListener(v -> {
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

                /*              Analytics           */
                Bundle bundle = new Bundle();
                bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, this.chatUserName);
                firebaseAnalytics.logEvent(AnalyticsContants.Event.MESSAGE_CAMERA, bundle);
            });

            locationButton.setOnClickListener(v -> {
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

            sendView.setOnClickListener(v -> onSendClicked());
            String draft = GenericCache.getInstance().get("draft_"+chatUserName);
            if(draft!=null) {
                messageBox.setText("");
                messageBox.append(draft);
                sendView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                sendView.setImageDrawable(getResources().getDrawable(R.drawable.ic_send_white));
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
        } else {
            presenceView.setVisibility(View.GONE);
        }
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
        }
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
            updateDeliveryStatus(messageId, deliveryReceiptId, messageStatus);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK && data!=null) {
            if(currentPhotoPath!=null) {
                Logger.d(this, "Got image");
                messagePresenter.sendImageMessage(chatUserName, currentUser, currentPhotoPath);
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

                messagePresenter.sendImageMessage(chatUserName, currentUser, picturePath);
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
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    Intent loadIntent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(loadIntent, REQUEST_GALLERY);
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
}