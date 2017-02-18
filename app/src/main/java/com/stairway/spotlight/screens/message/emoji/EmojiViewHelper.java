package com.stairway.spotlight.screens.message.emoji;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.stairway.spotlight.R;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.core.lib.AndroidUtils;
import com.stairway.spotlight.db.GenericCache;
import com.stairway.spotlight.screens.message.emoji.emoji_objects.Emojicon;
import com.stairway.spotlight.screens.message.emoji.emoji_objects.Nature;
import com.stairway.spotlight.screens.message.emoji.emoji_objects.Objects;
import com.stairway.spotlight.screens.message.emoji.emoji_objects.People;
import com.stairway.spotlight.screens.message.emoji.emoji_objects.Places;
import com.stairway.spotlight.screens.message.emoji.emoji_objects.Symbols;

import java.util.Arrays;
import java.util.List;

/**
 * Created by vidhun on 15/02/17.
 */

public class EmojiViewHelper implements ViewPager.OnPageChangeListener, EmojiconRecents{
    private View[] mEmojiTabs;
    Context mContext;
    EmojiconGridView.OnEmojiconClickedListener onEmojiconClickedListener;
    OnEmojiconBackspaceClickedListener onEmojiconBackspaceClickedListener;
    private PagerAdapter mEmojisAdapter;
    private EmojiconRecentsManager mRecentsManager;
    private ViewPager emojisPager;
    private int layoutHeightpx;

    private ViewGroup smileyLayout;
    private View emojiPickerLayout;
    private Window window;

    private boolean isEmojiViewInflated = false;
    private boolean isEmojiState = true;

    private SharedPreferences sharedPreferences;

    private static String KEY_KEYBOARD_HEIGHT = "KeyboardHeight";
    private static String PREFS_NAME = "EmojiViewHelper";

    public EmojiViewHelper(Context mContext, final ViewGroup viewGroup, Window window) {
        this.mContext = mContext;
        this.smileyLayout = viewGroup;
        this.window = window;
        this.emojiPickerLayout = createCustomView();
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
        ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                smileyLayout.getWindowVisibleDisplayFrame(r);

                int screenHeight = getUsableScreenHeight();
                int heightDifference = screenHeight - (r.bottom - r.top);
                int resourceId = mContext.getResources()
                        .getIdentifier("status_bar_height",
                                "dimen", "android");
                if (resourceId > 0) {
                    heightDifference -= mContext.getResources().getDimensionPixelSize(resourceId);
                }
                if ((screenHeight - r.bottom) > (screenHeight * 0.15)) {
                    if(layoutHeightpx!=heightDifference) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putFloat(KEY_KEYBOARD_HEIGHT, AndroidUtils.dp(heightDifference));
                        editor.apply();
                    }
                    layoutHeightpx = heightDifference;
                } else
                    Log.d("DEF", "CLOSE");
            }
        };
        smileyLayout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    public void addEmojiPickerView() {
        if(!isEmojiViewInflated) {
            this.isEmojiViewInflated = true;
            ViewGroup.LayoutParams layoutParams = smileyLayout.getLayoutParams();
            Logger.d(this, "adding smiley view, Setting height: "+layoutHeightpx);
            layoutParams.height = layoutHeightpx;
            smileyLayout.setLayoutParams(layoutParams);
            smileyLayout.addView(emojiPickerLayout);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }
    }

    public void removeEmojiPickerView() {
        if(isEmojiViewInflated) {
            Log.d("DEF", "DEFLATING VIEW");
            isEmojiViewInflated = false;
            ViewGroup.LayoutParams layoutParams = smileyLayout.getLayoutParams();
            layoutParams.height = 0;
            smileyLayout.setLayoutParams(layoutParams);
            smileyLayout.removeAllViews();
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    private int getUsableScreenHeight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();

            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(metrics);

            return metrics.heightPixels;

        } else {
            return smileyLayout.getRootView().getHeight();
        }
    }

    public void handleBackPress() {
        if(!isEmojiState()) {
            reset();
        } else {
            removeEmojiPickerView();
        }
    }

    public void emojiButtonToggle() {
            Activity currentActivity = (Activity) mContext;
            View view = currentActivity.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        if(isEmojiState) {
            addEmojiPickerView();
            isEmojiState = false;
        } else {
            isEmojiState = true;
        }
    }

    public boolean isEmojiState() {
        return isEmojiState;
    }

    public void reset() {
        isEmojiState = true;
        removeEmojiPickerView();
    }
    private View createCustomView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.emojicons, null, false);
        emojisPager = (ViewPager) view.findViewById(R.id.emojis_pager);
        emojisPager.setOnPageChangeListener(this);
        EmojiconRecents recents = this;
        mEmojisAdapter = new EmojisPagerAdapter(
                Arrays.asList(
                        new EmojiconRecentsGridView(mContext, null, null, this),
                        new EmojiconGridView(mContext, People.DATA, recents, this),
                        new EmojiconGridView(mContext, Nature.DATA, recents, this),
                        new EmojiconGridView(mContext, Objects.DATA, recents, this),
                        new EmojiconGridView(mContext, Places.DATA, recents, this),
                        new EmojiconGridView(mContext, Symbols.DATA, recents, this)
                )
        );
        emojisPager.setAdapter(mEmojisAdapter);
        mEmojiTabs = new View[6];
        mEmojiTabs[0] = view.findViewById(R.id.emojis_tab_0_recents);
        mEmojiTabs[1] = view.findViewById(R.id.emojis_tab_1_people);
        mEmojiTabs[2] = view.findViewById(R.id.emojis_tab_2_nature);
        mEmojiTabs[3] = view.findViewById(R.id.emojis_tab_3_objects);
        mEmojiTabs[4] = view.findViewById(R.id.emojis_tab_4_cars);
        mEmojiTabs[5] = view.findViewById(R.id.emojis_tab_5_punctuation);
        for (int i = 0; i < mEmojiTabs.length; i++) {
            final int position = i;
            mEmojiTabs[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    emojisPager.setCurrentItem(position);
                }
            });
        }
        view.findViewById(R.id.emojis_backspace).setOnTouchListener(new RepeatListener(1000, 50, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(onEmojiconBackspaceClickedListener != null)
                    onEmojiconBackspaceClickedListener.onEmojiconBackspaceClicked(v);
            }
        }));

        // get last selected page
        mRecentsManager = EmojiconRecentsManager.getInstance(view.getContext());
        int page = mRecentsManager.getRecentPage();
        // last page was recents, check if there are recents to use
        // if none was found, go to page 1
        if (page == 0 && mRecentsManager.size() == 0) {
            page = 1;
        }

        if (page == 0) {
            onPageSelected(page);
        }
        else {
            emojisPager.setCurrentItem(page, false);
        }
        return view;
    }

    private static class EmojisPagerAdapter extends PagerAdapter {
        private List<EmojiconGridView> views;
        public EmojiconRecentsGridView getRecentFragment(){
            for (EmojiconGridView it : views) {
                if(it instanceof EmojiconRecentsGridView)
                    return (EmojiconRecentsGridView)it;
            }
            return null;
        }
        public EmojisPagerAdapter(List<EmojiconGridView> views) {
            super();
            this.views = views;
        }

        @Override
        public int getCount() {
            return views.size();
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v = views.get(position).rootView;
            ((ViewPager)container).addView(v, 0);
            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object view) {
            ((ViewPager)container).removeView((View)view);
        }

        @Override
        public boolean isViewFromObject(View view, Object key) {
            return key == view;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void addRecentEmoji(Context context, Emojicon emojicon) {

    }

    /**
     * A class, that can be used as a TouchListener on any view (e.g. a Button).
     * It cyclically runs a clickListener, emulating keyboard-like behaviour. First
     * click is fired immediately, next before initialInterval, and subsequent before
     * normalInterval.
     * <p/>
     * <p>Interval is scheduled before the onClick completes, so it has to run fast.
     * If it runs slow, it does not generate skipped onClicks.
     */
    public static class RepeatListener implements View.OnTouchListener {

        private Handler handler = new Handler();

        private int initialInterval;
        private final int normalInterval;
        private final View.OnClickListener clickListener;

        private Runnable handlerRunnable = new Runnable() {
            @Override
            public void run() {
                if (downView == null) {
                    return;
                }
                handler.removeCallbacksAndMessages(downView);
                handler.postAtTime(this, downView, SystemClock.uptimeMillis() + normalInterval);
                clickListener.onClick(downView);
            }
        };

        private View downView;

        /**
         * @param initialInterval The interval before first click event
         * @param normalInterval  The interval before second and subsequent click
         *                        events
         * @param clickListener   The OnClickListener, that will be called
         *                        periodically
         */
        public RepeatListener(int initialInterval, int normalInterval, View.OnClickListener clickListener) {
            if (clickListener == null)
                throw new IllegalArgumentException("null runnable");
            if (initialInterval < 0 || normalInterval < 0)
                throw new IllegalArgumentException("negative interval");

            this.initialInterval = initialInterval;
            this.normalInterval = normalInterval;
            this.clickListener = clickListener;
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downView = view;
                    handler.removeCallbacks(handlerRunnable);
                    handler.postAtTime(handlerRunnable, downView, SystemClock.uptimeMillis() + initialInterval);
                    clickListener.onClick(view);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    handler.removeCallbacksAndMessages(downView);
                    downView = null;
                    return true;
            }
            return false;
        }
    }

    /**
     * Set the listener for the event when any of the emojicon is clicked
     */
    public void setOnEmojiconClickedListener(EmojiconGridView.OnEmojiconClickedListener listener){
        this.onEmojiconClickedListener = listener;
    }

    /**
     * Set the listener for the event when backspace on emojicon popup is clicked
     */
    public void setOnEmojiconBackspaceClickedListener(OnEmojiconBackspaceClickedListener listener){
        this.onEmojiconBackspaceClickedListener = listener;
    }

    public interface OnEmojiconBackspaceClickedListener {
        void onEmojiconBackspaceClicked(View v);
    }
}
