package com.chat.ichat.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.EditText;

import com.chat.ichat.R;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.lib.AndroidUtils;
/**
 * Created by vidhun on 03/07/17.
 */
public class HintEditText extends EditText {

    private String hintText;
    private float textOffset;
    private float spaceSize;
    private float numberSize;
    private Paint paint = new Paint();
    private Rect rect = new Rect();

    public HintEditText(Context context) {
        super(context);
        paint.setColor(0xFF0000FF);
        this.setWillNotDraw(false);
    }

    public HintEditText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(0xFF0000FF);
        this.setWillNotDraw(false);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.HintEditText, 0, 0);
        try {
            this.hintText = ta.getString(R.styleable.HintEditText_hint_text);
            paint.setColor(ta.getColor(R.styleable.HintEditText_hint_text_color, ContextCompat.getColor(context, R.color.appElement)));
        } finally {
            ta.recycle();
        }
    }

    public String getHintText() {
        return hintText;
    }

    public void setHintText(String value) {
        hintText = value;
        onTextChange();
        setText(getText());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        onTextChange();
    }

    public void onTextChange() {
        textOffset = (length() > 0 ? getPaint().measureText(getText(), 0, length()) : 0);
        textOffset = textOffset + AndroidUtils.px(4);
        spaceSize = getPaint().measureText(" ");
        numberSize = getPaint().measureText("1");
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (hintText != null && length() < hintText.length()) {
            int t = (getMeasuredHeight() / 2) - AndroidUtils.dp(8);

            float offsetX = textOffset;

            for (int a = length(); a < hintText.length(); a++) {
                if (hintText.charAt(a) == ' ') {
                    offsetX += spaceSize;
                } else {
                    int left = (int) offsetX + AndroidUtils.dp(16);
                    int right = (int) (offsetX + numberSize) - AndroidUtils.dp(16);
                    int top = t - AndroidUtils.dp(20);
                    int bottom = t + AndroidUtils.dp(20);

                    rect.set(left, top, right, bottom);
                    canvas.drawRect(rect, paint);
                    offsetX += numberSize;
                }
            }
        }
    }
}