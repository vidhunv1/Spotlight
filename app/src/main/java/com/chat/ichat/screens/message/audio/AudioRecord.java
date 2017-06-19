package com.chat.ichat.screens.message.audio;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.chat.ichat.R;
import com.chat.ichat.config.AnalyticsConstants;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.lib.AndroidUtils;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
/**
 * Created by vidhun on 05/05/17.
 */
public class AudioRecord extends View {
    private Paint paint;
    private int radiusMin = -1;
    private int radiusMax = -1;
    private int centerX;
    private int centerY;
    private Timer timer;
    private int touchState = 0;

    private long startTime = 0;
    private boolean running;
    private AudioRecordListener recordListener = null;

    public static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
    public static final String AUDIO_RECORDER_FOLDER = "ichat_audio";

    private MediaRecorder recorder = null;
    private String recordFileName;
    private FirebaseAnalytics firebaseAnalytics;

    private int micColor = 0xFFFE4034;

    public AudioRecord(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AudioRecord, 0, 0);
        try {
            radiusMin = ta.getDimensionPixelSize(R.styleable.AudioRecord_mic_min_radius, 50);
            radiusMax = ta.getDimensionPixelSize(R.styleable.AudioRecord_mic_max_radius, 60);
        } finally {
            ta.recycle();
        }
        paint = new Paint();
        paint.setAntiAlias(true);
        this.timer = new Timer();
        running = false;

        this.firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public void setRadiusMin(int radiusMin) {
        this.radiusMin = radiusMin;
    }

    public void setRadiusMax(int radiusMax) {
        this.radiusMax = radiusMax;
    }

    public void setRecordListener(AudioRecordListener recordListener) {
        this.recordListener = recordListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();
        double distanceFromCenter = Math.sqrt((centerX - x)*(centerX - x) + (centerY - y)*(centerY - y));
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(distanceFromCenter <= radiusMax) {
                    if(recordListener!=null) {
                        startRecording();
                        recordListener.onRecordStart();
                        firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_AUDIO_START_RECORD, null);
                    }

                    Logger.d(this, "Recording...");
                    startTimer();
                    timer = new Timer();
                    timer.scheduleAtFixedRate(new ViewRefresher(), 0 , 1000);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if(distanceFromCenter <= radiusMin) {
                    if(recordListener!=null) {
                        stopRecording();
                        recordListener.onRecordStop(recordFileName);
                        firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_AUDIO_STOP_RECORD, null);
                    }

                    Logger.d(this, "Release");
                    timer.cancel();
                    stopTimer();
                    invalidate();
                } else {
                    if(recordListener!=null) {
                        stopRecording();
                        recordListener.onRecordCancel();
                        firebaseAnalytics.logEvent(AnalyticsConstants.Event.MESSAGE_AUDIO_CANCEL_RECORD, null);
                    }

                    Logger.d(this, "Cancel");
                    timer.cancel();
                    stopTimer();
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(running) {
                    if(distanceFromCenter <= radiusMin) {
                        touchState = 1;
                        invalidate();
                    } else {
                        touchState = 0;
                        invalidate();
                    }
                }
                break;
        }
        return false;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        int pl = getPaddingLeft();
        int pr = getPaddingRight();
        int pt = getPaddingTop();
        int pb = getPaddingBottom();

        int usableWidth = w - (pl + pr);
        int usableHeight = h - (pt + pb);

        float rad;
        this.centerX = pl + (usableWidth / 2);
        this.centerY = pt + (usableHeight / 2);

        paint.setColor(micColor);
        if(running) {
            rad = radiusMin;
            if(touchState == 0) {
                canvas.drawColor(micColor);

                paint.setColor(Color.WHITE);
            }
        } else {
            rad = radiusMax;
        }
        canvas.drawCircle(centerX, centerY, rad, paint);

        String circleText = "Record";
        paint.setColor(Color.WHITE);
        if(running) {
            paint.setTextSize(AndroidUtils.px(16));
            if(touchState == 0) {
                paint.setColor(micColor);
                circleText = getElapsedTimeText();
            }
        } else {
            paint.setTextSize(AndroidUtils.px(20));
        }

        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(circleText, (canvas.getWidth()/2), (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)), paint);

        String titleText;
        if(running) {
            if(touchState == 1) {
                paint.setColor(micColor);
                titleText = getElapsedTimeText();
            } else {
                paint.setColor(Color.WHITE);
                titleText = "Release to cancel recording";
            }
        } else {
            titleText = getElapsedTimeText();
            paint.setColor(Color.GRAY);
        }

        paint.setTextSize(AndroidUtils.px(17));
        paint.setTextSize(AndroidUtils.px(20));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(titleText, (canvas.getWidth()/2), (int) ((usableHeight - radiusMax*2)/4 - ((paint.descent() + paint.ascent()) / 2)), paint);
    }

    public interface AudioRecordListener {
        void onRecordStart();
        void onRecordStop(String fileName);
        void onRecordCancel();
    }

    //stop watch methods
    private void startTimer() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
    }

    private void stopTimer() {
        this.running = false;
    }

    private String getElapsedTimeText() {
        long sec;
        long min;
        if (running) {
            sec = ((System.currentTimeMillis() - startTime) / 1000) % 60;
            min = (((System.currentTimeMillis() - startTime) / 1000) / 60 ) % 60;
            if(min>=1) {
                sec = sec - ((min-1) * 60);
            }
            if(min>=60) {
                min = 0;
                sec = 0;
                if(recordListener!=null) {
                    stopRecording();
                    recordListener.onRecordStop(recordFileName);

                }
            }
            return String.format("%01d", min)+":"+String.format("%02d", sec);
        }
        return "0:00";
    }

    class ViewRefresher extends TimerTask {
        @Override
        public void run() {
            postInvalidate();
        }
    }

    // Audio Record methods.
    private String getFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_MP4);
    }

    private String startRecording() {
        String fileName = getFilename();
        recorder = new MediaRecorder();

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(fileName);

        recorder.setOnErrorListener(errorListener);
        recorder.setOnInfoListener(infoListener);

        try {
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.recordFileName = fileName;
        return fileName;
    }

    private void stopRecording() {
        if(null != recorder){
            recorder.stop();
            recorder.reset();
            recorder.release();

            recorder = null;
        }
    }

    private MediaRecorder.OnErrorListener errorListener = (mr, what, extra) -> {};

    private MediaRecorder.OnInfoListener infoListener = (mr, what, extra) -> {};
}