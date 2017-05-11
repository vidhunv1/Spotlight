package com.chat.ichat.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import com.chat.ichat.R;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.lib.AndroidUtils;
import java.util.Timer;
import java.util.TimerTask;
/**
 * Created by vidhun on 11/05/17.
 */

public class AudioMessageView extends View {
    private Paint paint;
    private RectF viewrectF;
    private Timer timer;
    private boolean isRunning;

    private int viewColor;
    private int playingColor;
    private int primaryColor;

    private float playPerc;
    private long durationMilli = 10000;
    private int delayMilli = 10;
    private int iterCount = 0;

    private String audioFile = null;
    private MediaPlayer mediaPlayer;

    public AudioMessageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);

        viewrectF = new RectF(0, getHeight(), getWidth(), 0);
        this.viewColor = ContextCompat.getColor(context, R.color.sendMessageBubble);
        this.playingColor = ContextCompat.getColor(context, R.color.sendMessageBubblePressed);
        this.primaryColor = ContextCompat.getColor(context, R.color.colorPrimary);
        this.playPerc = 0f;
        isRunning = false;

        this.setOnClickListener(v -> {
            if(isRunning) {
                timer.cancel();
                isRunning = false;
                mediaPlayer.pause();
            } else {
                isRunning = true;
                timer = new Timer();
                timer.scheduleAtFixedRate(new ViewRefresher(), 0 , delayMilli);
                mediaPlayer.start();
            }
        });
    }

    public String getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(String audioFile) {
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(audioFile);
        String duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long dur = Long.parseLong(duration);
        durationMilli = dur % 60000;
        this.audioFile = audioFile;
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFile);
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(audioFile == null)
            return;
        viewrectF.left = 0;
        viewrectF.top = getHeight();
        viewrectF.right = getWidth();
        viewrectF.bottom = 0;
        paint.setColor(viewColor);

        canvas.drawRoundRect(viewrectF, AndroidUtils.px(18), AndroidUtils.px(18), paint);

        paint.setColor(playingColor);
        Path path;
        path = RoundedRect(0, getHeight(), getWidth(), 0, AndroidUtils.px(18), AndroidUtils.px(18), true, true, true, true);

        canvas.clipPath(path);
        canvas.drawRect(0, getHeight(), getWidth()*playPerc,0, paint);

        int pt = getPaddingTop();
        int pb = getPaddingBottom();

        int usableHeight = getHeight() - (pt + pb);
        int centerY = pt + (usableHeight / 2);

        paint.setColor(primaryColor);
        canvas.drawCircle(AndroidUtils.px(15+4), centerY, AndroidUtils.px(8), paint);
        paint.setStrokeWidth(AndroidUtils.px(1));
        canvas.drawLine(AndroidUtils.px(15+4), centerY, getWidth()-AndroidUtils.px(15+4), centerY, paint);

        viewrectF.left = getWidth()-AndroidUtils.px(15+40);
        viewrectF.top = getHeight() - AndroidUtils.px(8);
        viewrectF.right = getWidth() - AndroidUtils.px(15);
        viewrectF.bottom = AndroidUtils.px(8);
        canvas.drawRoundRect(viewrectF, AndroidUtils.px(18), AndroidUtils.px(18), paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(AndroidUtils.px(12));
        paint.setTextAlign(Paint.Align.CENTER);
        String time = getTimeString(durationMilli - delayMilli*iterCount);
        Logger.d(this, "Time: "+(durationMilli - delayMilli*((int)playPerc*durationMilli/delayMilli)));
        canvas.drawText(time, getWidth()-AndroidUtils.px(35), centerY + AndroidUtils.px(4), paint);

        if(!isRunning) {
            Path playPath = new Path();
            playPath.setFillType(Path.FillType.EVEN_ODD);
            playPath.moveTo(AndroidUtils.px(17), centerY+AndroidUtils.px(5));
            playPath.lineTo(AndroidUtils.px(17), centerY+AndroidUtils.px(5));
            playPath.lineTo(AndroidUtils.px(17+6f), centerY);
            playPath.lineTo(AndroidUtils.px(17), centerY-AndroidUtils.px(5));
            playPath.close();
            canvas.drawPath(playPath, paint);
        } else {
            paint.setStrokeWidth(AndroidUtils.px(2));
            canvas.drawLine(AndroidUtils.px(17), centerY - AndroidUtils.px(4), AndroidUtils.px(17), centerY + AndroidUtils.px(4), paint);
            canvas.drawLine(AndroidUtils.px(17+4), centerY - AndroidUtils.px(4), AndroidUtils.px(17+4), centerY + AndroidUtils.px(4), paint);
        }
    }

    private class ViewRefresher extends TimerTask {
        @Override
        public void run() {
            postInvalidate();
            if(playPerc<=1) {
                playPerc = playPerc + (((float) delayMilli / (float) durationMilli));
                iterCount++;
            } else {
                playPerc = 0;
                iterCount = 0;
                this.cancel();
                isRunning = false;
            }
        }
    }

    private String getTimeString(long milli) {
        long min = milli/60000;
        long sec = milli/1000;
        if(min>=1) {
            sec = sec - ((min-1) * 60);
        }
        return String.format("%02d", min)+":"+String.format("%02d", sec+1);
    }

    private Path RoundedRect(float left, float top, float right, float bottom, float rx, float ry,
            boolean tl, boolean tr, boolean br, boolean bl) {
        Path path = new Path();
        if (rx < 0) rx = 0;
        if (ry < 0) ry = 0;
        float width = right - left;
        float height = bottom - top;
        if (rx > width / 2) rx = width / 2;
        if (ry > height / 2) ry = height / 2;
        float widthMinusCorners = (width - (2 * rx));
        float heightMinusCorners = (height - (2 * ry));

        path.moveTo(right, top + ry);
        if (tr)
            path.rQuadTo(0, -ry, -rx, -ry);
        else {
            path.rLineTo(0, -ry);
            path.rLineTo(-rx,0);
        }
        path.rLineTo(-widthMinusCorners, 0);
        if (tl)
            path.rQuadTo(-rx, 0, -rx, ry);
        else {
            path.rLineTo(-rx, 0);
            path.rLineTo(0,ry);
        }
        path.rLineTo(0, heightMinusCorners);

        if (bl)
            path.rQuadTo(0, ry, rx, ry);
        else {
            path.rLineTo(0, ry);
            path.rLineTo(rx,0);
        }

        path.rLineTo(widthMinusCorners, 0);
        if (br)
            path.rQuadTo(rx, 0, rx, -ry);
        else {
            path.rLineTo(rx,0);
            path.rLineTo(0, -ry);
        }
        path.rLineTo(0, -heightMinusCorners);
        path.close();

        return path;
    }
}