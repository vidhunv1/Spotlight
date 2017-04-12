package com.chat.ichat.screens.shared_media;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.chat.ichat.R;
import com.chat.ichat.core.BaseActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vidhun on 04/03/17.
 */

public class SharedMediaActivity extends BaseActivity {
    @Bind(R.id.tb_shared_media)
    Toolbar toolbar;

    private static String KEY_USER_NAME = "UserProfileActivity.USER_NAME";
    public static Intent callingIntent(Context context, String username) {
        Intent intent = new Intent(context, SharedMediaActivity.class);
        intent.putExtra(KEY_USER_NAME, username);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_media);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
