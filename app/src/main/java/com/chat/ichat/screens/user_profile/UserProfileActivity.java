package com.chat.ichat.screens.user_profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
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
import com.chat.ichat.config.AnalyticsConstants;
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

    @Bind(R.id.tb)
    Toolbar toolbar;

    @Bind(R.id.tv_contact_name)
    TextView contactNameView;

    @Bind(R.id.tv_user_profile_id)
    TextView userIdView;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

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

        this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAnalytics.setCurrentScreen(this, AnalyticsConstants.Event.USER_PROFILE_SCREEN, null);
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
        firebaseAnalytics.logEvent(AnalyticsConstants.Event.USER_PROFILE_BACK, null);
        super.onBackPressed();
    }

    @OnClick(R.id.iv_back)
    public void onBackClick() {
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

            Bundle bundle = new Bundle();
            bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_NAME, this.username);
            firebaseAnalytics.logEvent(AnalyticsConstants.Event.USER_PROFILE_BLOCK, bundle);
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

            Bundle bundle = new Bundle();
            bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_NAME, this.username);
            firebaseAnalytics.logEvent(AnalyticsConstants.Event.USER_PROFILE_DELETE, bundle);
        } else if(id == R.id.action_add_shortcut) {
            Bundle bundle = new Bundle();
            bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_NAME, this.username);
            firebaseAnalytics.logEvent(AnalyticsConstants.Event.USER_PROFILE_ADD_SHORTCUT, bundle);
        }
        return super.onOptionsItemSelected(item);
    }

    public void delete(String username) {
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
        startActivity(HomeActivity.callingIntent(this, 0, null));
    }
}