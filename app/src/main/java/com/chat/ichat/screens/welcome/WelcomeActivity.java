package com.chat.ichat.screens.welcome;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.chat.ichat.R;
import com.chat.ichat.config.AnalyticsConstants;
import com.chat.ichat.core.lib.AndroidUtils;
import com.chat.ichat.screens.sign_up.SignUpActivity1;
import com.google.firebase.analytics.FirebaseAnalytics;


public class WelcomeActivity extends AppCompatActivity{
    private FirebaseAnalytics firebaseAnalytics;

    private ViewPager viewPager;
    private ImageView topImage1;
    private ImageView topImage2;
    private ViewGroup bottomPages;
    private int lastPage = 0;
    private boolean justCreated = false;
    private boolean startPressed = false;
    private int[] icons;
    private int[] titles;
    private int[] messages;


    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_welcome);

            icons = new int[]{
                    R.mipmap.ic_launcher,
                    R.mipmap.ic_launcher,
                    R.mipmap.ic_launcher,
            };
            titles = new int[]{
                    R.string.Page7Title,
                    R.string.Page6Title,
                    R.string.Page5Title,
            };
            messages = new int[]{
                    R.string.Page7Message,
                    R.string.Page6Message,
                    R.string.Page5Message,
            };

        viewPager = (ViewPager) findViewById(R.id.intro_view_pager);
        TextView startMessagingButton = (TextView) findViewById(R.id.start_messaging_button);
        startMessagingButton.setText("START MESSAGING");
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(startMessagingButton, "translationZ", AndroidUtils.dp(2), AndroidUtils.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(startMessagingButton, "translationZ", AndroidUtils.dp(4), AndroidUtils.dp(2)).setDuration(200));
            startMessagingButton.setStateListAnimator(animator);
        }
        topImage1 = (ImageView) findViewById(R.id.icon_image1);
        topImage2 = (ImageView) findViewById(R.id.icon_image2);
        bottomPages = (ViewGroup) findViewById(R.id.bottom_pages);
        topImage2.setVisibility(View.GONE);
        viewPager.setAdapter(new IntroAdapter());
        viewPager.setPageMargin(0);
        viewPager.setOffscreenPageLimit(1);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int i) {
                if(i == 0) {
                    firebaseAnalytics.logEvent(AnalyticsConstants.Event.WELCOME_PAGE_1, null);
                } else if (i == 1) {
                    firebaseAnalytics.logEvent(AnalyticsConstants.Event.WELCOME_PAGE_2, null);
                } else if (i == 2) {
                    firebaseAnalytics.logEvent(AnalyticsConstants.Event.WELCOME_PAGE_3, null);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                if (i == ViewPager.SCROLL_STATE_IDLE || i == ViewPager.SCROLL_STATE_SETTLING) {
                    if (lastPage != viewPager.getCurrentItem()) {
                        lastPage = viewPager.getCurrentItem();

                        final ImageView fadeoutImage;
                        final ImageView fadeinImage;
                        if (topImage1.getVisibility() == View.VISIBLE) {
                            fadeoutImage = topImage1;
                            fadeinImage = topImage2;

                        } else {
                            fadeoutImage = topImage2;
                            fadeinImage = topImage1;
                        }

                        fadeinImage.bringToFront();
                        fadeinImage.setImageResource(icons[lastPage]);
                        fadeinImage.clearAnimation();
                        fadeoutImage.clearAnimation();

                        Animation outAnimation = AnimationUtils.loadAnimation(WelcomeActivity.this, R.anim.cb_face_out);
                        outAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                fadeoutImage.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });

                        Animation inAnimation = AnimationUtils.loadAnimation(WelcomeActivity.this, R.anim.cb_fade_in);
                        inAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                fadeinImage.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });


                        fadeoutImage.startAnimation(outAnimation);
                        fadeinImage.startAnimation(inAnimation);
                    }
                }
            }
        });
        Context context = this;

        startMessagingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startPressed) {
                    return;
                }
                startPressed = true;
                firebaseAnalytics.logEvent(AnalyticsConstants.Event.WELCOME_CLICK_START_MESSAGING, null);
                startActivity(SignUpActivity1.callingIntent(context));
            }
        });

        justCreated = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (justCreated) {
            viewPager.setCurrentItem(0);
            lastPage = 0;
            justCreated = false;
            firebaseAnalytics.setCurrentScreen(this, AnalyticsConstants.Event.WELCOME_SCREEN, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private class IntroAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = View.inflate(container.getContext(), R.layout.layout_intro_view, null);
            TextView headerTextView = (TextView) view.findViewById(R.id.header_text);
            TextView messageTextView = (TextView) view.findViewById(R.id.message_text);
            container.addView(view, 0);

            headerTextView.setText(getString(titles[position]));
            messageTextView.setText(getString(messages[position]));

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            int count = bottomPages.getChildCount();
            for (int a = 0; a < count; a++) {
                View child = bottomPages.getChildAt(a);
                if (a == position) {
                    child.setBackgroundResource(R.drawable.bg_chat_notification);
                } else {
                    child.setBackgroundResource(R.drawable.bg_intro_pager_inactive);
                }
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }
    }
}
