package com.stairway.spotlight.screens.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.core.lib.AndroidUtils;
import com.stairway.spotlight.core.lib.ImageUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends BaseActivity {

    @Bind(R.id.tb_settings)
    Toolbar toolbar;

    @Bind(R.id.main_content)
    CoordinatorLayout rootLayout;

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_toolbar, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if ((id == android.R.id.home)) {
            super.onBackPressed();
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

    @OnClick(R.id.settings_sound_row)
    public void onSoundClicked() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
        this.startActivityForResult(intent, 5);
    }

    public void showVibratePopup() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View vibratePopupView = inflater.inflate(R.layout.popup_settings_vibrate, null);
        PopupWindow vibratePopupWindow = new PopupWindow(
                vibratePopupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
        if(Build.VERSION.SDK_INT>=21)
            vibratePopupWindow.setElevation(5.0f);

        vibratePopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        vibratePopupWindow.showAtLocation(rootLayout, Gravity.CENTER,0,0);

        FrameLayout out = (FrameLayout) vibratePopupView.findViewById(R.id.fl_settings_vibrate);
        out.setOnClickListener(view -> vibratePopupWindow.dismiss());
    }

    public void showLedColorPopup() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View ledPopupView = inflater.inflate(R.layout.popup_settings_ledcolor, null);
        PopupWindow ledPopupWindow = new PopupWindow(
                ledPopupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
        if(Build.VERSION.SDK_INT>=21)
            ledPopupWindow.setElevation(5.0f);

        ledPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        ledPopupWindow.showAtLocation(rootLayout, Gravity.CENTER,0,0);

        FrameLayout out = (FrameLayout) ledPopupView.findViewById(R.id.fl_settings_ledcolor);
        out.setOnClickListener(view -> ledPopupWindow.dismiss());
    }

    public void showTextSizePopup() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View textSizePopupView = inflater.inflate(R.layout.popup_settings_textsize, null);
        PopupWindow textSizePopupWindow = new PopupWindow(
                textSizePopupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
        if(Build.VERSION.SDK_INT>=21)
            textSizePopupWindow.setElevation(5.0f);

        textSizePopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        textSizePopupWindow.showAtLocation(rootLayout, Gravity.CENTER,0,0);

        NumberPicker numberPicker = (NumberPicker) textSizePopupView.findViewById(R.id.settings_number_picker);
        numberPicker.setMinValue(12);
        numberPicker.setMaxValue(30);
        setDividerColor(numberPicker, ContextCompat.getColor(this, R.color.activeIndicator));

        FrameLayout out = (FrameLayout) textSizePopupView.findViewById(R.id.fl_settings_textsize);
        out.setOnClickListener(view -> textSizePopupWindow.dismiss());
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent)
    {
        if (resultCode == Activity.RESULT_OK && requestCode == 5)
        {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

            if (uri != null)
            {
                //chosen ringtone
                 Logger.d(this, "Chosen ringtone: "+uri.toString());
            }
            else
            {
                //chosen ringtone null
            }
        }
    }

    private void setDividerColor(NumberPicker picker, int color) {
        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}
