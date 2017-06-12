package com.chat.ichat.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.chat.ichat.R;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.lib.AndroidUtils;
import com.chat.ichat.db.GenericCache;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import static com.chat.ichat.screens.message.audio.AudioRecord.AUDIO_RECORDER_FILE_EXT_MP4;
import static com.chat.ichat.screens.message.audio.AudioRecord.AUDIO_RECORDER_FOLDER;
/**
 * Created by vidhun on 11/05/17.
 */
public class AudioMessageView extends View {
    private Paint paint;
    private RectF viewrectF;
    private Timer timer;
    private boolean isRunning;
    private Context context;

    private int viewColor;
    private int playingColor;
    private int primaryColor;

    private float playPerc;
    private long durationMilli = 1000;
    private int delayMilli = 10;
    private int iterCount = 0;

    private String audioFile = null;
    private String audioUrl = null;
    private MediaPlayer mediaPlayer;

    private Path roundRectPath = null;
    private Path playPath = null;

    private boolean isStream = false;
    private String streamFileName = null;
    private boolean isReady = false;

    GenericCache genericCache;

    private AudioReadyListener audioReadyListener;
    public AudioMessageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setWillNotDraw(false);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        paint = new Paint();
        paint.setAntiAlias(true);
        this.genericCache = GenericCache.getInstance();

        viewrectF = new RectF(0, getHeight(), getWidth(), 0);
        this.playPerc = 0f;
        isRunning = false;

        this.setOnClickListener(v -> {
            if(isReady) {
                if (isRunning) {
                    timer.cancel();
                    isRunning = false;
                    mediaPlayer.pause();
                } else {
                    isRunning = true;
                    timer = new Timer();
                    timer.scheduleAtFixedRate(new ViewRefresher(), 0, delayMilli);
                    mediaPlayer.start();
                }
            }
        });

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AudioMessageView, 0, 0);
        try {
            this.viewColor = ta.getColor(R.styleable.AudioMessageView_view_color, ContextCompat.getColor(context, R.color.sendMessageBubble));
            this.playingColor = ta.getColor(R.styleable.AudioMessageView_playing_color, ContextCompat.getColor(context, R.color.sendMessageBubblePressed));
            this.primaryColor = ta.getColor(R.styleable.AudioMessageView_primary_color, ContextCompat.getColor(context, R.color.appElement));
        } finally {
            ta.recycle();
        }
    }

    public void setAudioReadyListener(AudioReadyListener audioReadyListener) {
        this.audioReadyListener = audioReadyListener;
    }

    public String getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(String audioFile) {
        this.isReady = true;
        this.isStream = false;

        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        Logger.d(this, "Setting audio file: "+audioFile);
        metaRetriever.setDataSource(audioFile);
        String duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long dur = Long.parseLong(duration);
        durationMilli = dur % 60000;
        if(audioReadyListener!=null) {
            audioReadyListener.onAudioReady(durationMilli);
        }
        this.audioFile = audioFile;
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFile);
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAudioUrl(String url) {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        String filename = context.getFilesDir().getAbsolutePath()+ "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_MP4;
        this.isStream = true;
        this.streamFileName = filename;
        this.audioUrl = url;

        if(genericCache.get(url)!=null) {
            this.isReady = true;
            this.streamFileName = genericCache.get(url);
            setAudioFile(this.streamFileName);
        } else {
            new DownloadFileAsync().execute(url, filename);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        viewrectF.left = 0;
        viewrectF.top = getHeight();
        viewrectF.right = getWidth();
        viewrectF.bottom = 0;
        paint.setColor(viewColor);

        canvas.drawRoundRect(viewrectF, AndroidUtils.px(18), AndroidUtils.px(18), paint);

        if(!isReady || audioFile==null)
            return;

        if(roundRectPath == null) {
            roundRectPath = RoundedRect(0, getHeight(), getWidth(), 0, AndroidUtils.px(18), AndroidUtils.px(18), true, true, true, true);
        }
        paint.setColor(playingColor);

        canvas.clipPath(roundRectPath);
        canvas.drawRect(0, getHeight(), getWidth()*playPerc,0, paint);

        int pt = getPaddingTop();
        int pb = getPaddingBottom();

        int usableHeight = getHeight() - (pt + pb);
        int centerY = pt + (usableHeight / 2);

        int circleCenterX = 19;

        paint.setColor(primaryColor);
        canvas.drawCircle(AndroidUtils.px(circleCenterX+3), centerY, AndroidUtils.px(8), paint);
        paint.setStrokeWidth(AndroidUtils.px(1));
        canvas.drawLine(AndroidUtils.px(circleCenterX+4), centerY, getWidth()-AndroidUtils.px(circleCenterX+4), centerY, paint);

        viewrectF.left = getWidth()-AndroidUtils.px(52);
        viewrectF.top = getHeight() - AndroidUtils.px(10.5f);
        viewrectF.right = getWidth() - AndroidUtils.px(circleCenterX-5);
        viewrectF.bottom = AndroidUtils.px(10.5f);
        canvas.drawRoundRect(viewrectF, AndroidUtils.px(18), AndroidUtils.px(18), paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(AndroidUtils.px(12));
        paint.setTextAlign(Paint.Align.CENTER);
        String time = getTimeString(durationMilli - delayMilli*iterCount);
        canvas.drawText(time, getWidth()-AndroidUtils.px(33), centerY + AndroidUtils.px(4), paint);

        float playX = 19.5f;
        if(!isRunning) { //play bitmap
            if(playPath == null) {
                if(isReady)
                    playPath = getPlayPath(AndroidUtils.px(playX), centerY);
            }
            canvas.drawPath(playPath, paint);
        } else { // pause bitmap
            float pauseX = 20.5f;
            paint.setStrokeWidth(AndroidUtils.px(1.5f));
            canvas.drawLine(AndroidUtils.px(pauseX), centerY - AndroidUtils.px(3.5f), AndroidUtils.px(pauseX), centerY + AndroidUtils.px(3.5f), paint);
            canvas.drawLine(AndroidUtils.px(pauseX+3.5f), centerY - AndroidUtils.px(3.5f), AndroidUtils.px(pauseX+3.5f), centerY + AndroidUtils.px(3.5f), paint);
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
        return String.format("%01d", min)+":"+String.format("%02d", sec+1);
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

    private Path getPlayPath(float x, float y) {
        Path playPath = new Path();
        playPath.setFillType(Path.FillType.EVEN_ODD);
        playPath.moveTo(x, y+AndroidUtils.px(4.5f));
        playPath.lineTo(x, y+AndroidUtils.px(4.5f));
        playPath.lineTo(x + AndroidUtils.px(6.3f), y);
        playPath.lineTo(x, y-AndroidUtils.px(4.5f));
        playPath.close();
        return playPath;
    }

    private void onDownloadStart() {
        isReady = false;
        invalidate();
    }

    private void onDownloadComplete() {
        if(audioUrl!=null && streamFileName!=null) {
            isReady = true;
            genericCache.put(audioUrl, streamFileName);
            setAudioFile(streamFileName);
        }
        invalidate();
    }

    private void onProgressUpdate(int progress) {}

    private class DownloadFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            onDownloadStart();
        }

        @Override
        protected String doInBackground(String... aurl) {
            int count;
            try {
                URL url = new URL(aurl[0]);
                String filename = aurl[1];
                URLConnection conexion = url.openConnection();
                conexion.connect();
                int lenghtOfFile = conexion.getContentLength();
                Log.d("ANDRO_ASYNC", "Length of file: " + lenghtOfFile);
                InputStream input = new BufferedInputStream(url.openStream());

                OutputStream output = new FileOutputStream(filename);
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(""+(int)((total*100)/lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {}
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            Log.d("ANDRO_ASYNC",progress[0]);
            AudioMessageView.this.onProgressUpdate(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            onDownloadComplete();
        }
    }

    public interface AudioReadyListener {
        void onAudioReady(long duration);
    }
}