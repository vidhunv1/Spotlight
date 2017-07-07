package com.chat.ichat.core.lib;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.chat.ichat.application.SpotlightApplication;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by vidhun on 26/01/17.
 */

public abstract class AndroidUtils {
    public static void showSoftInput(Activity activity, EditText editText) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus) {
                InputMethodManager inputMgr = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                inputMgr.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        editText.requestFocus();
    }

    public static void hideSoftInput(Activity activity) {
        View view = activity.getCurrentFocus();
        if(view!=null) {
            view.clearFocus();
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static float px(float dp){
        Context context = SpotlightApplication.getContext();
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static int dp(float px){
        Context context = SpotlightApplication.getContext();
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int)Math.ceil(px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static String displayNameStyle(String text) {
//        text = text.toLowerCase();
//        String[] arr = text.split(" ");
//        StringBuffer sb = new StringBuffer();
//
//        for (int i = 0; i < arr.length; i++) {
//            sb.append(Character.toUpperCase(arr[i].charAt(0)))
//                    .append(arr[i].substring(1)).append(" ");
//        }
        return text.trim();
    }

    public static String lastActivityAt(DateTime time) {
        DateTime timeNow = DateTime.now();
        DateTimeFormatter timeFormat = DateTimeFormat.forPattern("h:mm a");
        String t = "at "+time.toString(timeFormat).toUpperCase().replace(".", "");
        if(timeNow.getDayOfMonth() == time.getDayOfMonth()) {
            return t;
        } else if(time.getDayOfMonth() == (timeNow.getDayOfMonth()-1)) {
            return "yesterday "+t;
        } else if(timeNow.getYear() == time.getYear()) {
            return time.monthOfYear().getAsShortText()+" "+time.getDayOfMonth() + " "+t;
        } else {
            return "recently";
        }
    }
}
