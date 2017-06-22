package com.chat.ichat.components.textinputlayout;


/**
 * android.support.design
 */
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;

class TintInfo {
    public ColorStateList mTintList;
    public PorterDuff.Mode mTintMode;
    public boolean mHasTintMode;
    public boolean mHasTintList;

    void clear() {
        mTintList = null;
        mHasTintList = false;
        mTintMode = null;
        mHasTintMode = false;
    }
}

