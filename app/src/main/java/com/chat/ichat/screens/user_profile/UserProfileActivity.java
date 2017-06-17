package com.chat.ichat.screens.user_profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.chat.ichat.api.ApiError;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.user.UserResponse;
import com.chat.ichat.core.GsonProvider;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.db.MessageStore;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.models.Message;
import com.chat.ichat.models.MessageResult;
import com.chat.ichat.screens.home.HomeActivity;
import com.chat.ichat.screens.image_viewer.ImageViewerActivity;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.chat.ichat.MessageController;
import com.chat.ichat.R;
import com.chat.ichat.config.AnalyticsContants;
import com.chat.ichat.core.BaseActivity;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.lib.AndroidUtils;
import com.chat.ichat.core.lib.ImageUtils;
import com.chat.ichat.screens.shared_media.SharedMediaActivity;
import org.jivesoftware.smack.packet.Presence;
import org.joda.time.DateTime;
import java.util.List;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.chat.ichat.MessageController.LAST_SEEN_PREFS_FILE;

public class UserProfileActivity extends BaseActivity {
    @Bind(R.id.iv_userprofile_dp)
    ImageView profileDP;

    @Bind(R.id.tb_user_profile)
    Toolbar toolbar;

    @Bind(R.id.user_profile_message)
    FloatingActionButton fab;

    @Bind(R.id.tv_contact_name)
    TextView contactNameView;

    @Bind(R.id.tv_user_profile_id)
    TextView userIdView;

    @Bind(R.id.profile_presence)
    TextView presenceView;

    @Bind(R.id.tv_shared_media_count)
    TextView sharedMediaCount;

    private Menu menu;
    final ProgressDialog[] progressDialog = new ProgressDialog[1];
    private static int RESULT_LOAD_IMAGE = 1;
    private String username;
    private String contactName;
    private String userId;
    private String contactProfileDP;
    private boolean isBlocked;

    private static String KEY_USER_NAME = "UserProfileActivity.USER_NAME";
    private static String KEY_CONTACT_NAME = "UserProfileActivity.CONTACT_NAME";
    private static String KEY_CONTACT_USER_ID = "UserProfileActivity.USER_ID";
    private static String KEY_CONTACT_BLOCKED = "UserProfileActivity.BLOCKED";
    private static String KEY_PROFILE_DP = "UserProfileActivity.KEY_PROFILE_DP";

    private FirebaseAnalytics firebaseAnalytics;
    private final String SCREEN_NAME = "user_profile";
    public static Intent callingIntent(Context context, String username, String userid, String contactName, boolean isBlocked, String contactDP) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(KEY_USER_NAME, username);
        intent.putExtra(KEY_CONTACT_NAME, contactName);
        intent.putExtra(KEY_CONTACT_USER_ID, userid);
        intent.putExtra(KEY_CONTACT_BLOCKED, isBlocked);
        intent.putExtra(KEY_PROFILE_DP, contactDP);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);
        Intent receivedIntent = getIntent();
        if(!receivedIntent.hasExtra(KEY_USER_NAME))
            return;

        username = receivedIntent.getStringExtra(KEY_USER_NAME);
        contactName = receivedIntent.getStringExtra(KEY_CONTACT_NAME);
        userId = receivedIntent.getStringExtra(KEY_CONTACT_USER_ID);
        contactProfileDP = receivedIntent.getStringExtra(KEY_PROFILE_DP);
        isBlocked = receivedIntent.getBooleanExtra(KEY_CONTACT_BLOCKED, false);

        contactNameView.setText(contactName);
        userIdView.setText(userId);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences sharedPreferences = this.getSharedPreferences(LAST_SEEN_PREFS_FILE, Context.MODE_PRIVATE);
        long millis = sharedPreferences.getLong(username, 0);
        if (millis == 0) {
            if (username.startsWith("o_")) {
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

        if(contactProfileDP!=null && !contactProfileDP.isEmpty()) {
            Logger.d(this, "Setting DP: "+contactProfileDP);
            Context context = this;
            Glide.with(this)
                    .load(contactProfileDP.replace("https://", "http://"))
                    .asBitmap().centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(ImageUtils.getDefaultProfileImage(contactName, username, 25.5))
                    .into(new BitmapImageViewTarget(profileDP) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            profileDP.setImageDrawable(circularBitmapDrawable);
                        }
                    });

            profileDP.setOnClickListener(v -> startActivity(ImageViewerActivity.callingIntent(this, contactProfileDP)));
        } else {
            profileDP.setImageDrawable(ImageUtils.getDefaultProfileImage(contactName, username, 25.5));
        }
        MessageController messageController = MessageController.getInstance();
        messageController.getLastActivity(this.username)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {}
            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(String time) {
                presenceView.setText(time);
            }
        });

        MessageStore.getInstance().getMessages(receivedIntent.getStringExtra(KEY_USER_NAME))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<MessageResult>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(List<MessageResult> messageResults) {
                        int count = 0;
                        for (MessageResult messageResult : messageResults) {
                            if(GsonProvider.getGson().fromJson(messageResult.getMessage(), Message.class).getMessageType() == Message.MessageType.image) {
                                count++;
                            }
                        }
                        sharedMediaCount.setText(count+"");
                    }
                });


        this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*              Analytics           */
        firebaseAnalytics.setCurrentScreen(this, SCREEN_NAME, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.user_profile_toolbar, menu);
        menu.findItem(R.id.action_block_contact);
        if(isBlocked) {
            menu.findItem(R.id.action_block_contact).setTitle("Unblock");
        } else {
            menu.findItem(R.id.action_block_contact).setTitle("Block");
        }
        return true;
    }

    public void blockUnblockContact(boolean shouldBlock) {
        ContactResult contactResult = new ContactResult();
        contactResult.setUserId(userId);
        contactResult.setBlocked(shouldBlock);
        Activity activity = UserProfileActivity.this;
        ApiManager.getContactApi().blockContact(userId)
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
                            a = ApiManager.getContactApi().blockContact(userId);
                        } else {
                            a = ApiManager.getContactApi().unblockContact(userId);
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
                                                        isBlocked = !isBlocked;

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
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if(id == R.id.action_block_contact) {
            LinearLayout parent = new LinearLayout(this);

            parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
            parent.setOrientation(LinearLayout.VERTICAL);
            parent.setPadding((int)AndroidUtils.px(24), (int)AndroidUtils.px(8), (int)AndroidUtils.px(24), 0);

            TextView textView1 = new TextView(this);
            textView1.setText("Are you sure want to block this contact?");
            textView1.setTextColor(ContextCompat.getColor(this, R.color.textColor));
            textView1.setTextSize(16);

            parent.addView(textView1);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.app_name));
            builder.setPositiveButton("OK", ((dialog, which) -> {
                blockUnblockContact(!isBlocked);
                progressDialog[0] = ProgressDialog.show(UserProfileActivity.this, "", "Please wait a moment", true);
            }));
            builder.setNegativeButton("CANCEL", ((dialog, which) -> {}));
            builder.setView(parent);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            /*              Analytics           */
            Bundle bundle = new Bundle();
            bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, this.username);
            firebaseAnalytics.logEvent(AnalyticsContants.Event.BLOCK_USER, bundle);
        } else if(id == R.id.action_delete_contact) {
            LinearLayout parent = new LinearLayout(this);

            parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
            parent.setOrientation(LinearLayout.VERTICAL);
            parent.setPadding((int)AndroidUtils.px(24), (int)AndroidUtils.px(8), (int)AndroidUtils.px(24), 0);

            TextView textView1 = new TextView(this);
            textView1.setText("Are you sure want to delete this contact?");
            textView1.setTextColor(ContextCompat.getColor(this, R.color.textColor));
            textView1.setTextSize(16);

            parent.addView(textView1);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.app_name));
            builder.setPositiveButton("OK", ((dialog, which) -> {
                delete(username);
            }));
            builder.setNegativeButton("CANCEL", ((dialog, which) -> {}));
            builder.setView(parent);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            /*              Analytics           */
            Bundle bundle = new Bundle();
            bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, this.username);
            firebaseAnalytics.logEvent(AnalyticsContants.Event.DELETE_USER, bundle);
        } else if(id == R.id.action_add_shortcut) {
            /*              Analytics           */
            Bundle bundle = new Bundle();
            bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, this.username);
            firebaseAnalytics.logEvent(AnalyticsContants.Event.ADD_SHORTCUT, bundle);
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.user_profile_message)
    public void onMessageClicked() {
        onBackPressed();

        /*              Analytics           */
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsContants.Param.OTHER_USER_NAME, this.username);
        firebaseAnalytics.logEvent(AnalyticsContants.Event.PROFILE_MESSAGE_USER, bundle);
    }

    @OnClick(R.id.profile_first_line)
    public void onUserIDClicked() {
        LinearLayout parent = new LinearLayout(this);

        parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setPadding((int) AndroidUtils.px(16),(int)AndroidUtils.px(8), 0, (int)AndroidUtils.px(8));

        TextView textView1 = new TextView(this);
        textView1.setText("Copy");
        textView1.setTextColor(ContextCompat.getColor(this, R.color.textColor));
        textView1.setTextSize(16);
        textView1.setGravity(Gravity.CENTER_VERTICAL);
        textView1.setHeight((int)AndroidUtils.px(48));

        parent.addView(textView1);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(parent);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        textView1.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("userId", userId);
            clipboard.setPrimaryClip(clip);
            alertDialog.dismiss();
        });
    }

    @OnClick(R.id.user_profile_shared_media)
    public void onSharedMediaClicked() {
        startActivity(SharedMediaActivity.callingIntent(this, username));
    }

    @OnClick(R.id.user_profile_notifications)
    public void onNotificationsClicked() {
        TextView t1, t2, t3, t4, t5;
        LinearLayout parent = new LinearLayout(this);

        parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setPadding((int)AndroidUtils.px(24), (int)AndroidUtils.px(8), 0, (int)AndroidUtils.px(8));

        t1 = new TextView(this);
        t1.setText("Turn On");
        t1.setTextColor(ContextCompat.getColor(this, R.color.textColor));
        t1.setTextSize(16);
        t1.setHeight((int)AndroidUtils.px(48));
        t1.setGravity(Gravity.CENTER_VERTICAL);
        t1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_volume_up_dark, 0, 0, 0);
        t1.setCompoundDrawablePadding((int)AndroidUtils.px(24));

        t5 = new TextView(this);
        t5.setText("Turn Off");
        t5.setTextColor(ContextCompat.getColor(this, R.color.textColor));
        t5.setTextSize(16);
        t5.setHeight((int)AndroidUtils.px(48));
        t5.setGravity(Gravity.CENTER_VERTICAL);
        t5.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_volume_off_dark, 0, 0, 0);
        t5.setCompoundDrawablePadding((int)AndroidUtils.px(24));

        parent.addView(t1);
        parent.addView(t5);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notifications");
        builder.setView(parent);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        t1.setOnClickListener(v -> {
            alertDialog.dismiss();
        });
        t5.setOnClickListener(v -> {
            alertDialog.dismiss();
        });
    }

    @Override
    public void onPresenceChanged(String username, Presence.Type type) {
        if(this.username.equals(username)) {
            Resources res = getResources();
            if(type == Presence.Type.available) {
                presenceView.setVisibility(View.VISIBLE);
                presenceView.setText(res.getString(R.string.chat_presence_online));
            } else if(type == Presence.Type.unavailable) {
                DateTime timeNow = DateTime.now();
                presenceView.setVisibility(View.VISIBLE);
                presenceView.setText(getResources().getString(R.string.chat_presence_away, AndroidUtils.lastActivityAt(timeNow)));
            }
        }
    }

    public void delete(String username) {
        ContactStore.getInstance().deleteContact(username);
        startActivity(HomeActivity.callingIntent(this, 0, null));
    }
}