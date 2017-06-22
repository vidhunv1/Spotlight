package com.chat.ichat.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.chat.ichat.core.lib.AndroidUtils;

/**
 * Created by vidhun on 20/06/17.
 */
public class RoundRectCornerImageView extends android.support.v7.widget.AppCompatImageView {

    private float radiusMax = AndroidUtils.px(18);
    private int width = -1;
    private int height = -1;
    private Path path;
    private RectF rect;

    public RoundRectCornerImageView(Context context) {
        super(context);
        init();
    }

    public RoundRectCornerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundRectCornerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(width <0 && height < 0 || width!=getWidth() || height!=getHeight()){
            width = this.getWidth();
            height = this.getHeight();
            rect = new RectF(0, 0, this.getWidth(), this.getHeight());
            path.addRoundRect(rect, radiusMax, radiusMax, Path.Direction.CW);
        }
        canvas.clipPath(path);
        super.onDraw(canvas);
    }
}