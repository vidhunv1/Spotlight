package com.chat.ichat.screens.image_viewer;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chat.ichat.R;
import com.chat.ichat.screens.user_profile.UserProfileActivity;
import com.google.firebase.analytics.FirebaseAnalytics;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ImageViewerActivity extends AppCompatActivity {
    @Bind(R.id.image)
    ImageView imageView;

    @Bind(R.id.tb)
    Toolbar toolbar;

    private static String KEY_IMAGE = "UmageViewrActivity.IMAGE";

    private FirebaseAnalytics firebaseAnalytics;
    private final String SCREEN_NAME = "image_viewer";

    private String image;
    public static Intent callingIntent(Context context, String uri) {
        Intent intent = new Intent(context, ImageViewerActivity.class);
        intent.putExtra(KEY_IMAGE, uri);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent receivedIntent = getIntent();
        if(!receivedIntent.hasExtra(KEY_IMAGE))
            return;

        image = receivedIntent.getStringExtra(KEY_IMAGE);
        Glide.with(this).load(image)
                .crossFade()
                .into(imageView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
