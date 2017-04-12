package com.chat.ichat.screens.message;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by vidhun on 16/02/17.
 */

public class MessageEditText extends EditText{

    private EditTextImeBackListener mOnImeBack;

    public MessageEditText(Context context) {
        super(context);
    }

    public MessageEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MessageEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK &&
                event.getAction() == KeyEvent.ACTION_UP) {
            if (mOnImeBack != null)
                mOnImeBack.onImeBack();
        }
        return super.dispatchKeyEvent(event);
    }

    public void setOnEditTextImeBackListener(EditTextImeBackListener listener) {
        mOnImeBack = listener;
    }
    public interface EditTextImeBackListener {
        public abstract void onImeBack();
    }
}


