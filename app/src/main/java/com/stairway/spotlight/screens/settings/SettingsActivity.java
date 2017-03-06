package com.stairway.spotlight.screens.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
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

import com.stairway.spotlight.R;
import com.stairway.spotlight.application.SpotlightApplication;
import com.stairway.spotlight.components.CustomNumberPicker;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.core.lib.AndroidUtils;
import com.stairway.spotlight.core.lib.ImageUtils;
import com.stairway.spotlight.screens.web_view.WebViewActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends BaseActivity {

    @Bind(R.id.tb_settings)
    Toolbar toolbar;

    @Bind(R.id.main_content)
    CoordinatorLayout rootLayout;

    @Bind(R.id.settings_dp)
    ImageView profileDp;

    @Bind(R.id.settings_vibrate_option)
    TextView vibrateOptionView;

    @Bind(R.id.settings_text_size)
    TextView textSizeView;

    @Bind(R.id.settings_send_by_enter)
    Switch sendByEnterSwitch;

    @Bind(R.id.settings_alert)
    Switch alertSwitch;

    @Bind(R.id.settings_in_app_browser)
    Switch inAppBrowserSwitch;

    static final String PREFS_FILE = "settings";
    static final String KEY_ALERT = "alert";
    static final String KEY_SOUND = "sound";
    static final String KEY_VIBRATE = "vibrate";
    static final String KEY_LED_COLOR = "led_color";
    static final String KEY_IN_APP_BROWSER = "in_app_browser";
    static final String KEY_SEND_BY_ENTER = "send_by_enter";
    static final String KEY_TEXT_SIZE = "text_size";

    static enum VibrateOptions {DISABLED, DEFAULT, SHORT, LONG, ONLY_IF_SILENT};
    static enum LedOptions {RED, ORANGE, YELLOW, GREEN, CYAN, BLUE, VIOLET, PINK, WHITE};
    String vibrateOptionsNames[] = {"Disabled", "Default", "Short", "Long", "Only if silent"};

    private SharedPreferences sharedPreferences;

    public static Intent callingIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        toolbar.setSubtitle("");

        profileDp.setImageDrawable(ImageUtils.getDefaultProfileImage("Vidhun Vinod", "vidhun", 18));

        this.sharedPreferences = SpotlightApplication.getContext().getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);

        vibrateOptionView.setText(vibrateOptionsNames[sharedPreferences.getInt(KEY_VIBRATE,1)]);
        textSizeView.setText(sharedPreferences.getInt(KEY_TEXT_SIZE, 16)+"");
        sendByEnterSwitch.setChecked(sharedPreferences.getBoolean(KEY_SEND_BY_ENTER, false));
        alertSwitch.setChecked(sharedPreferences.getBoolean(KEY_ALERT, true));
        inAppBrowserSwitch.setChecked(sharedPreferences.getBoolean(KEY_IN_APP_BROWSER, true));

        alertSwitch.setOnClickListener(v -> sharedPreferences.edit().putBoolean(KEY_ALERT, alertSwitch.isChecked()).apply());
        sendByEnterSwitch.setOnClickListener(v -> sharedPreferences.edit().putBoolean(KEY_SEND_BY_ENTER, sendByEnterSwitch.isChecked()).apply());
        inAppBrowserSwitch.setOnClickListener(v -> sharedPreferences.edit().putBoolean(KEY_IN_APP_BROWSER, inAppBrowserSwitch.isChecked()).apply());
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
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.settings_vibrate_row)
    public void onVibrateClicked() {
        showVibratePopup();
    }

    @OnClick(R.id.settings_ledcolor_row)
    public void onLedColorClicked() {
        showLedColorPopup();
    }

    @OnClick(R.id.settings_texsize_row)
    public void onTextSizeClicked() {
        showTextSizePopup();
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

        TextView textView2 = new TextView(this);
        textView2.setText("From gallery");
        textView2.setHeight((int)AndroidUtils.px(48));
        textView2.setTextSize(16);
        textView2.setGravity(Gravity.CENTER_VERTICAL);
        textView2.setTextColor(ContextCompat.getColor(this, R.color.textColor));

        TextView textView3 = new TextView(this);
        textView3.setHeight((int)AndroidUtils.px(48));
        textView3.setText("Delete photo");
        textView3.setTextSize(16);
        textView3.setGravity(Gravity.CENTER_VERTICAL);
        textView3.setTextColor(ContextCompat.getColor(this, R.color.textColor));

        parent.addView(textView1);
        parent.addView(textView2);
        parent.addView(textView3);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(parent);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void showAskAQuestionPopup() {
        LinearLayout parent = new LinearLayout(this);

        parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setPadding((int)AndroidUtils.px(24),(int)AndroidUtils.px(18), (int)AndroidUtils.px(24), 0);

        TextView textView1 = new TextView(this);
        textView1.setText("We try to respond as quickly as possible, but it may take a while.\n\nPlease take a look at\niChat FAQ: it has answers to most questions and important tips for troubleshooting.");
        textView1.setTextColor(ContextCompat.getColor(this, R.color.textColor));
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
        builder.setPositiveButton("OK", ((dialog, which) -> {}));
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
        rg.setPadding((int)AndroidUtils.px(18),(int)AndroidUtils.px(8),0,0);

        int colorsInt[] = {Color.rgb(255,0,0), Color.rgb(255,165,0), Color.rgb(255,255,0), Color.rgb(0,255,0), Color.rgb(0,255,255), Color.rgb(0,0,255), Color.rgb(238,130,238), Color.rgb(255, 192, 203), Color.rgb(245, 245, 245)};
        String colorsText[] = {"Red", "Orange", "Yellow", "Green", "Cyan", "Blue", "Violet", "Pink", "White"};

        for(int i=0; i<9; i++){
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

    public void showTextSizePopup() {
        int textSize = sharedPreferences.getInt(KEY_TEXT_SIZE, 16);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Messages Text Size");
        final CustomNumberPicker numberPicker = new CustomNumberPicker(this, null);
        numberPicker.setMinValue(12);
        numberPicker.setMaxValue(30);
        numberPicker.setValue(textSize);
        builder.setView(numberPicker);
        builder.setPositiveButton("Done", ((dialog, which) -> {
            sharedPreferences.edit().putInt(KEY_TEXT_SIZE, numberPicker.getValue()).apply();
            textSizeView.setText(String.valueOf(numberPicker.getValue()));
        }));
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (resultCode == Activity.RESULT_OK && requestCode == 5)
        {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                //chosen ringtone
                 Logger.d(this, "Chosen ringtone: "+uri.toString());
            }
            else
            {
                //chosen ringtone null
            }
        }
    }
}