package com.chat.ichat.screens.message.audio;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
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

    private ViewGroup audioLayout;
    private View audioPickerLayout;
    private Window window;

    private boolean isAudioViewInflated = false;
    private boolean isAudioState = true;

    private AudioRecord.AudioRecordListener audioRecordListener = null;
    ComposerViewHelper composerViewHelper;

    public AudioViewHelper(Context mContext, final ViewGroup viewGroup, Window window) {
        this.mContext = mContext;
        this.audioLayout = viewGroup;
        this.window = window;
        this.audioPickerLayout = createCustomView();
        composerViewHelper = new ComposerViewHelper(mContext, viewGroup);
    }

    public void addAudioView() {
        if(!isAudioViewInflated) {
            this.isAudioViewInflated = true;
            ViewGroup.LayoutParams layoutParams = audioLayout.getLayoutParams();
            Logger.d(this, "adding smiley view, Setting height: "+composerViewHelper.getLayoutHeightpx());
            layoutParams.height = composerViewHelper.getLayoutHeightpx();
            audioLayout.setLayoutParams(layoutParams);
            audioLayout.addView(audioPickerLayout);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

            AudioRecord audioRecord = (AudioRecord) audioLayout.findViewById(R.id.audio_record);

            if(audioRecordListener!=null) {
                audioRecord.setRecordListener(audioRecordListener);
            }
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

    public void setAudioRecordListener(AudioRecord.AudioRecordListener audioRecordListener) {
        this.audioRecordListener = audioRecordListener;
    }
}