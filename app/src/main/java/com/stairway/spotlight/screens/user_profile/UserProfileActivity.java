package com.stairway.spotlight.screens.user_profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ImageView;

import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.di.component.ComponentContainer;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UserProfileActivity extends BaseActivity {
    @Bind(R.id.iv_userprofile_dp)
    ImageView profileDP;

    private static String USER_ID = "USERID";
    public static Intent callingIntent(Context context, String userId) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra(USER_ID, userId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);

        profileDP.setImageResource(R.drawable.default_profile_image);
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
    }
}
