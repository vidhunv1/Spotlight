package com.chat.ichat.screens.message.audio;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.chat.ichat.R;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.lib.AndroidUtils;

/**
 * Created by vidhun on 03/05/17.
 */

public class AudioViewHelper{
    Context mContext;
    private int layoutHeightpx;

    private ViewGroup audioLayout;
    private View audioPickerLayout;
    private Window window;

    private boolean isAudioViewInflated = false;
    private boolean isAudioState = true;

    private SharedPreferences sharedPreferences;

    private static String KEY_KEYBOARD_HEIGHT = "KeyboardHeight";
    private static String PREFS_NAME = "EmojiViewHelper";

    public AudioViewHelper(Context mContext, final ViewGroup viewGroup, Window window) {
        this.mContext = mContext;
        this.audioLayout = viewGroup;
        this.window = window;
        this.audioPickerLayout = createCustomView();
        sharedPreferences = mContext.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
        float cachedHeight = sharedPreferences.getFloat(KEY_KEYBOARD_HEIGHT, -1);
        if(cachedHeight!=-1) {
            layoutHeightpx = (int) AndroidUtils.px(cachedHeight);
        } else {
            layoutHeightpx = (int) AndroidUtils.px(230);
        }
        updateKeyboardHeight();
    }

    private void updateKeyboardHeight() {
        ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = () -> {
            Rect r = new Rect();
            audioLayout.getWindowVisibleDisplayFrame(r);

            int screenHeight = getUsableScreenHeight();
            int heightDifference = screenHeight - (r.bottom - r.top);
            int resourceId = mContext.getResources()
                    .getIdentifier("status_bar_height",
                            "dimen", "android");
            if (resourceId > 0) {
                heightDifference -= mContext.getResources().getDimensionPixelSize(resourceId);
            }
            if ((screenHeight - r.bottom) > (screenHeight * 0.15)) {
                if(layoutHeightpx!=heightDifference) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putFloat(KEY_KEYBOARD_HEIGHT, AndroidUtils.dp(heightDifference));
                    editor.apply();
                }
                layoutHeightpx = heightDifference;
            } else
                Log.d("DEF", "CLOSE");
        };
        audioLayout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    public void addAudioView() {
        if(!isAudioViewInflated) {
            this.isAudioViewInflated = true;
            ViewGroup.LayoutParams layoutParams = audioLayout.getLayoutParams();
            Logger.d(this, "adding smiley view, Setting height: "+layoutHeightpx);
            layoutParams.height = layoutHeightpx;
            audioLayout.setLayoutParams(layoutParams);
            audioLayout.addView(audioPickerLayout);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }
    }

    public void removeAudioPickerView() {
        if(isAudioViewInflated) {
            Log.d("DEF", "DEFLATING VIEW");
            isAudioViewInflated = false;
            ViewGroup.LayoutParams layoutParams = audioLayout.getLayoutParams();
            layoutParams.height = 0;
            audioLayout.setLayoutParams(layoutParams);
            audioLayout.removeAllViews();
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    private int getUsableScreenHeight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();

            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(metrics);

            return metrics.heightPixels;

        } else {
            return audioLayout.getRootView().getHeight();
        }
    }

    public void handleBackPress() {
        if(!isAudioState()) {
            reset();
        } else {
            removeAudioPickerView();
        }
    }

    public void audioButtonToggle() {
        Activity currentActivity = (Activity) mContext;
        View view = currentActivity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        if(isAudioState) {
            addAudioView();
            isAudioState = false;
        } else {
            isAudioState = true;
        }
    }

    public boolean isAudioState() {
        return isAudioState;
    }

    public void reset() {
        isAudioState = true;
        removeAudioPickerView();
    }

    private View createCustomView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.audio_layout, null, false);
        return view;
    }
}