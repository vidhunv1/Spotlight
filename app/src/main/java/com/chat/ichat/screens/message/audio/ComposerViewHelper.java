package com.chat.ichat.screens.message.audio;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.chat.ichat.core.Logger;
import com.chat.ichat.core.lib.AndroidUtils;

/**
 * Created by vidhun on 17/05/17.
 */

public class ComposerViewHelper {
    Context mContext;
    private int layoutHeightpx;
    private SharedPreferences sharedPreferences;
    private ViewGroup layout;

    public static String KEY_KEYBOARD_HEIGHT = "KeyboardHeight";
    public static String PREFS_NAME = "ComposerViewHelper";

    public ComposerViewHelper(Context context, ViewGroup layout) {
        this.layout = layout;
        this.mContext = context;
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
            layout.getWindowVisibleDisplayFrame(r);

            int screenHeight = getUsableScreenHeight();
            int heightDifference = screenHeight - (r.bottom - r.top);
            int resourceId = mContext.getResources()
                    .getIdentifier("status_bar_height",
                            "dimen", "android");
            if (resourceId > 0) {
                heightDifference -= mContext.getResources().getDimensionPixelSize(resourceId);
            }
            if ((screenHeight - r.bottom) > (screenHeight * 0.15)) {
                if(layoutHeightpx!=heightDifference && AndroidUtils.dp(heightDifference)>=220) {
                    Logger.d(this, "HeightDifference: "+heightDifference);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putFloat(KEY_KEYBOARD_HEIGHT, AndroidUtils.dp(heightDifference));
                    editor.apply();
                    layoutHeightpx = heightDifference;
                }
            } else
                Log.d("DEF", "CLOSE");
        };
        layout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    private int getUsableScreenHeight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();

            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(metrics);

            return metrics.heightPixels;

        } else {
            return layout.getRootView().getHeight();
        }
    }

    public int getLayoutHeightpx() {
        return layoutHeightpx;
    }
}
