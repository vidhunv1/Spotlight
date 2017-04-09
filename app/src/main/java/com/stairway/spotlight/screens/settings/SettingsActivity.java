package com.stairway.spotlight.screens.settings;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.stairway.spotlight.R;
import com.stairway.spotlight.UserSessionManager;
import com.stairway.spotlight.api.ApiError;
import com.stairway.spotlight.api.ApiManager;
import com.stairway.spotlight.application.SpotlightApplication;
import com.stairway.spotlight.components.CustomNumberPicker;
import com.stairway.spotlight.config.AnalyticsContants;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.core.lib.AndroidUtils;
import com.stairway.spotlight.core.lib.ImageUtils;
import com.stairway.spotlight.core.lib.RoundedCornerTransformation;
import com.stairway.spotlight.models.UserSession;
import com.stairway.spotlight.screens.web_view.WebViewActivity;
import com.stairway.spotlight.screens.welcome.WelcomeActivity;

import java.io.File;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Response;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class SettingsActivity extends BaseActivity implements SettingsContract.View{

    @Bind(R.id.tb_settings)
    Toolbar toolbar;

    @Bind(R.id.main_content)
    CoordinatorLayout rootLayout;

    @Bind(R.id.settings_dp)
    ImageView profileDp;

    @Bind(R.id.settings_vibrate_option)
    TextView vibrateOptionView;

    @Bind(R.id.settings_send_by_enter)
    Switch sendByEnterSwitch;

    @Bind(R.id.settings_alert)
    Switch alertSwitch;

    @Bind(R.id.settings_in_app_browser)
    Switch inAppBrowserSwitch;

    @Bind(R.id.tv_user_profile_id)
    TextView profileIdText;

    @Bind(R.id.title)
    TextView profileNameText;

    @Bind(R.id.android_version_name)
    TextView versionNameText;

    private static final int REQUEST_GALLERY = 1;
    private static final int REQUEST_CAMERA = 2;
    private String currentPhotoPath;

    final ProgressDialog[] progressDialog = new ProgressDialog[1];

    public static final String PREFS_FILE = "settings";
    public static final String KEY_ALERT = "alert";
    public static final String KEY_SOUND = "sound";
    public static final String KEY_VIBRATE = "vibrate";
    public static final String KEY_LED_COLOR = "led_color";
    public static final String KEY_IN_APP_BROWSER = "in_app_browser";
    public static final String KEY_SEND_BY_ENTER = "send_by_enter";
    public static final String KEY_TEXT_SIZE = "text_size";

    static enum VibrateOptions {DISABLED, DEFAULT, SHORT, LONG, ONLY_IF_SILENT};
    static enum LedOptions {RED, ORANGE, YELLOW, GREEN, CYAN, BLUE, VIOLET, PINK, WHITE};
    String vibrateOptionsNames[] = {"Disabled", "Default", "Short", "Long", "Only if silent"};

    private SharedPreferences sharedPreferences;

    UserSession userSession;

    SettingsPresenter settingsPresenter;

    AlertDialog alertDialogPic = null;

    private FirebaseAnalytics firebaseAnalytics;
    private final String SCREEN_NAME = "settings";
    public static Intent callingIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        userSession = UserSessionManager.getInstance().load();
        settingsPresenter = new SettingsPresenter(ApiManager.getUserApi(), UserSessionManager.getInstance(), PreferenceManager.getDefaultSharedPreferences(this));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        toolbar.setSubtitle("");

        profileNameText.setText(userSession.getName());
        profileIdText.setText(userSession.getUserId());
        profileDp.setImageDrawable(ImageUtils.getDefaultProfileImage(userSession.getName(), userSession.getUserId(), 18));

        this.sharedPreferences = SpotlightApplication.getContext().getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);

        vibrateOptionView.setText(vibrateOptionsNames[sharedPreferences.getInt(KEY_VIBRATE,1)]);
        sendByEnterSwitch.setChecked(sharedPreferences.getBoolean(KEY_SEND_BY_ENTER, false));
        alertSwitch.setChecked(sharedPreferences.getBoolean(KEY_ALERT, true));
        inAppBrowserSwitch.setChecked(sharedPreferences.getBoolean(KEY_IN_APP_BROWSER, true));

        alertSwitch.setOnClickListener(v -> sharedPreferences.edit().putBoolean(KEY_ALERT, alertSwitch.isChecked()).apply());
        sendByEnterSwitch.setOnClickListener(v -> sharedPreferences.edit().putBoolean(KEY_SEND_BY_ENTER, sendByEnterSwitch.isChecked()).apply());
        inAppBrowserSwitch.setOnClickListener(v -> sharedPreferences.edit().putBoolean(KEY_IN_APP_BROWSER, inAppBrowserSwitch.isChecked()).apply());
        String versionName;
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "v1.0.0";
        }
        versionNameText.setText(this.getResources().getString(R.string.settings_app_version, this.getResources().getString(R.string.app_name), versionName));
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        if(userSession.getProfilePicPath()!=null && !userSession.getProfilePicPath().isEmpty()) {
            Context context = this;
            Glide.with(this).load(userSession.getProfilePicPath().replace("https://", "http://")).asBitmap().centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(true)
                    .placeholder(ImageUtils.getDefaultProfileImage(userSession.getName(), userSession.getUserId(), 18))
                    .into(new BitmapImageViewTarget(profileDp) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            profileDp.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            super.onBackPressed();
        } else if(id == R.id.action_logout) {
            showLogoutPopup();

            /*              Analytics           */
            Bundle bundle = new Bundle();
            firebaseAnalytics.logEvent(AnalyticsContants.Event.LOGOUT, bundle);
        } else if(id == R.id.action_edit_name) {

            /*              Analytics           */
            Bundle bundle = new Bundle();
            firebaseAnalytics.logEvent(AnalyticsContants.Event.EDIT_NAME, bundle);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(alertDialogPic!=null) {
            alertDialogPic.dismiss();
        }
        settingsPresenter.attachView(this);

        /*              Analytics           */
        firebaseAnalytics.setCurrentScreen(this, SCREEN_NAME, null);
    }

    public void showError(String title, String message) {
        if(progressDialog[0]!=null && progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }
        
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage("\n"+message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    @Override
    public void onLogoutSuccess() {
        if(progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }
        startActivity(WelcomeActivity.callingIntent(this));
        finish();
    }


    @OnClick(R.id.settings_vibrate_row)
    public void onVibrateClicked() {
        showVibratePopup();
    }

    @OnClick(R.id.settings_ledcolor_row)
    public void onLedColorClicked() {
        showLedColorPopup();
    }

    @OnClick(R.id.settings_askquestion_row)
    public void onAskQuestionClicked() {
        showAskAQuestionPopup();
    }

    @OnClick(R.id.settings_faq_row)
    public void onFaqClicked() {
        startActivity(WebViewActivity.callingIntent(this, "http://google.com/faq"));
    }

    @OnClick(R.id.settings_privacy_policy_row)
    public void onPrivacyPolicyClicked() {
        startActivity(WebViewActivity.callingIntent(this, "http://google.com/privacy"));
    }

    @OnClick(R.id.settings_sound_row)
    public void onSoundClicked() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Ringtone");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
        this.startActivityForResult(intent, 5);
    }

    @OnClick(R.id.settings_take_pic)
    public void onCameraClicked() {
        LinearLayout parent = new LinearLayout(this);

        parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setPadding((int)AndroidUtils.px(16),(int)AndroidUtils.px(8), 0, (int)AndroidUtils.px(8));

        TextView textView1 = new TextView(this);
        textView1.setText("From camera");
        textView1.setTextColor(ContextCompat.getColor(this, R.color.textColor));
        textView1.setTextSize(16);
        textView1.setGravity(Gravity.CENTER_VERTICAL);
        textView1.setHeight((int)AndroidUtils.px(48));
        textView1.setOnClickListener(v -> {
            // ** Load image from camera **
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
                    Uri photoURI = FileProvider.getUriForFile(this, "com.stairway.spotlight.fileprovider", photoFile);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    currentPhotoPath = photoFile.getAbsolutePath();
                    startActivityForResult(cameraIntent, REQUEST_CAMERA);
                }
            }
        });

        TextView textView2 = new TextView(this);
        textView2.setText("From gallery");
        textView2.setHeight((int)AndroidUtils.px(48));
        textView2.setTextSize(16);
        textView2.setGravity(Gravity.CENTER_VERTICAL);
        textView2.setTextColor(ContextCompat.getColor(this, R.color.textColor));
        textView2.setOnClickListener(v -> {
            // ** Load image from gallery **
            // Permissions
            int perm1 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int perm2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int permission = PackageManager.PERMISSION_GRANTED;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (!(perm1 == permission && perm2 == permission)) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                } else {
                    Intent loadIntent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(loadIntent, REQUEST_GALLERY);
                }
            }
        });

        TextView textView3 = new TextView(this);
        textView3.setHeight((int)AndroidUtils.px(48));
        textView3.setText("Delete photo");
        textView3.setTextSize(16);
        textView3.setGravity(Gravity.CENTER_VERTICAL);
        textView3.setTextColor(ContextCompat.getColor(this, R.color.textColor));
        textView3.setOnClickListener(v -> {
            UserSession u = new UserSession();
            u.setProfilePicPath("");
            UserSessionManager.getInstance().save(u);
            profileDp.setImageDrawable(ImageUtils.getDefaultProfileImage(userSession.getName(), userSession.getUserId(), 18));
            Handler mainHandler = new Handler(this.getMainLooper());

            Context context = this;
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    alertDialogPic.dismiss();
                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                }
            };
            mainHandler.post(myRunnable);
        });

        parent.addView(textView1);
        parent.addView(textView2);
        parent.addView(textView3);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(parent);
        alertDialogPic = builder.create();
        alertDialogPic.show();
    }

    public void showAskAQuestionPopup() {
        LinearLayout parent = new LinearLayout(this);

        parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setPadding((int)AndroidUtils.px(24),(int)AndroidUtils.px(18), (int)AndroidUtils.px(24), 0);

        TextView textView1 = new TextView(this);
        textView1.setText("We try to respond as quickly as possible, but it may take a while.\n\nPlease take a look at\niChat FAQ: it has answers to most questions and important tips for troubleshooting.");
        textView1.setTextColor(ContextCompat.getColor(this, R.color.textColor));
        textView1.setPadding(0,0,0,(int) AndroidUtils.px(6));
        textView1.setTextSize(18);

        parent.addView(textView1);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ask a Question");
        builder.setPositiveButton("ASK", ((dialog, which) -> {}));
        builder.setNegativeButton("CANCEL", ((dialog, which) -> {}));
        builder.setView(parent);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void showLogoutPopup() {
        LinearLayout parent = new LinearLayout(this);

        parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setPadding((int)AndroidUtils.px(24),(int)AndroidUtils.px(18), (int)AndroidUtils.px(24), 0);

        TextView textView1 = new TextView(this);
        textView1.setText("Are you sure want to log out?");
        textView1.setTextColor(ContextCompat.getColor(this, R.color.textColor));
        textView1.setTextSize(16);

        parent.addView(textView1);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.app_name));
        builder.setPositiveButton("OK", ((dialog, which) -> {
            if(progressDialog[0]!=null && progressDialog[0].isShowing()) {
                progressDialog[0].dismiss();
            }
            progressDialog[0] = ProgressDialog.show(this, "", "Logging out. Please wait...", true);
            settingsPresenter.logoutUser();
        }));
        builder.setNegativeButton("CANCEL", ((dialog, which) -> {}));
        builder.setView(parent);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @SuppressWarnings("RestrictedApi")
    public void showVibratePopup() {
        int checkedPos = sharedPreferences.getInt(KEY_VIBRATE, 1);
        final AppCompatRadioButton[] rb = new AppCompatRadioButton[5];
        RadioGroup rg = new RadioGroup(this);
        rg.setOrientation(RadioGroup.VERTICAL);
        rg.setPadding((int)AndroidUtils.px(18), (int)AndroidUtils.px(8), 0, (int)AndroidUtils.px(8));
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{ new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked} },
                new int[]{Color.GRAY, ContextCompat.getColor(this, R.color.colorPrimary)});

        for(int i=0; i<5; i++) {
            rb[i]  = new AppCompatRadioButton(this);
            rb[i].setText(vibrateOptionsNames[i]);
            rb[i].setHeight((int)AndroidUtils.px(48));
            rb[i].setId(i + 100);
            rb[i].setTextSize(16);
            rb[i].setPadding((int)AndroidUtils.px(11),0,0,0);
            rb[i].setSupportButtonTintList(colorStateList);

            if(i == checkedPos) {
                rb[i].setChecked(true);
            }
            rg.addView(rb[i]);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Vibrate");
        builder.setView(rg);
        builder.setNegativeButton("CANCEL", ((dialog, which) -> {}));
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        rb[0].setOnClickListener(v -> {
            alertDialog.dismiss();
            sharedPreferences.edit().putInt(KEY_VIBRATE, 0).apply();
            vibrateOptionView.setText(vibrateOptionsNames[0]);
        });
        rb[1].setOnClickListener(v -> {
            alertDialog.dismiss();
            sharedPreferences.edit().putInt(KEY_VIBRATE, 1).apply();
            vibrateOptionView.setText(vibrateOptionsNames[1]);
        });
        rb[2].setOnClickListener(v -> {
            alertDialog.dismiss();
            sharedPreferences.edit().putInt(KEY_VIBRATE, 2).apply();
            vibrateOptionView.setText(vibrateOptionsNames[2]);
        });
        rb[3].setOnClickListener(v -> {
            alertDialog.dismiss();
            sharedPreferences.edit().putInt(KEY_VIBRATE, 3).apply();
            vibrateOptionView.setText(vibrateOptionsNames[3]);
        });
        rb[4].setOnClickListener(v -> {
            alertDialog.dismiss();
            sharedPreferences.edit().putInt(KEY_VIBRATE, 4).apply();
            vibrateOptionView.setText(vibrateOptionsNames[4]);
        });
    }

    @SuppressWarnings("RestrictedApi")
    public void showLedColorPopup() {
        int checkedPos = 4;
        final AppCompatRadioButton[] rb = new AppCompatRadioButton[9];
        RadioGroup rg = new RadioGroup(this);
        rg.setOrientation(RadioGroup.VERTICAL);
        rg.setPadding((int)AndroidUtils.px(18),(int)AndroidUtils.px(8),0,(int)AndroidUtils.px(8));

        int colorsInt[] = {Color.rgb(255,0,0), Color.rgb(255,165,0), Color.rgb(255,255,0), Color.rgb(0,255,0), Color.rgb(0,255,255), Color.rgb(0,0,255), Color.rgb(238,130,238), Color.rgb(255, 192, 203), Color.rgb(245, 245, 245)};
        String colorsText[] = {"Red", "Orange", "Yellow", "Green", "Cyan", "Blue", "Violet", "Pink", "White"};

        for(int i=0; i<9; i++) {
            rb[i]  = new AppCompatRadioButton(this);
            rb[i].setText(colorsText[i]);
            rb[i].setHeight((int)AndroidUtils.px(48));
            rb[i].setId(i + 100);
            rb[i].setTextSize(16);
            rb[i].setPadding((int)AndroidUtils.px(11),0,0,0);
            rb[i].setSupportButtonTintList(new ColorStateList(
                    new int[][]{ new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked} },
                    new int[]{colorsInt[i], colorsInt[i]}));

            if(i == checkedPos) {
                rb[i].setChecked(true);
            }
            rg.addView(rb[i]);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Led Color");
        builder.setView(rg);

        builder.setPositiveButton("SET", ((dialog, which) -> {}));
        // hack for positioning button left-right<-->
        builder.setNegativeButton(" ", ((dialog, which) -> {}));
        builder.setNeutralButton("DISABLED", ((dialog, which) -> {}));

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void updateProfileDP(String url) {
        Logger.d(this, "Setting profile DP: "+url);
        Context context = this;
        Glide.with(this).load(url.replace("https://", "http://")).asBitmap().centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true)
                .placeholder(ImageUtils.getDefaultProfileImage(userSession.getName(), userSession.getUserId(), 18))
                .into(new BitmapImageViewTarget(profileDp) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        profileDp.setImageDrawable(circularBitmapDrawable);
                    }
                });
        Toast.makeText(this, "Uploaded DP.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == Activity.RESULT_OK && requestCode == 5) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                //chosen ringtone
                Logger.d(this, "Chosen ringtone: "+uri.toString());
            } else {
                //chosen ringtone null
            }
        } else if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK && intent!=null) {
            if(alertDialogPic!=null)
                alertDialogPic.dismiss();
            Uri selectedImage = intent.getData();
            Logger.d(this, selectedImage.toString());
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            Logger.d(this, "Got gallery pic: "+picturePath);
            cursor.close();
            settingsPresenter.uploadProfileDP(new File(picturePath));

        } else if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            if(alertDialogPic!=null)
                alertDialogPic.dismiss();
            if(currentPhotoPath!=null) {
                settingsPresenter.uploadProfileDP(new File(currentPhotoPath));
                Logger.d(this, "got camClick:"+currentPhotoPath);
                galleryAddPic(currentPhotoPath);
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
                } else {
                    //not granted
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void galleryAddPic(String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
}