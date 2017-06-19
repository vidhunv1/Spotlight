package com.chat.ichat.screens.shared_media;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.TextView;

import com.chat.ichat.R;
import com.chat.ichat.config.AnalyticsConstants;
import com.chat.ichat.core.BaseActivity;
import com.chat.ichat.core.GsonProvider;
import com.chat.ichat.core.Logger;
import com.chat.ichat.db.MessageStore;
import com.chat.ichat.models.Message;
import com.chat.ichat.models.MessageResult;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vidhun on 04/03/17.
 */

public class SharedMediaActivity extends BaseActivity {
    @Bind(R.id.tb_shared_media)
    Toolbar toolbar;

    @Bind(R.id.grid)
    GridView gridLayout;

    @Bind(R.id.date)
    TextView date;

    private FirebaseAnalytics firebaseAnalytics;
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
        Intent receivedIntent = getIntent();
        if(!receivedIntent.hasExtra(KEY_USER_NAME))
            return;
        Context context = this;
        MessageStore.getInstance().getMessages(receivedIntent.getStringExtra(KEY_USER_NAME))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<MessageResult>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<MessageResult> messageResults) {
                        date.setText(messageResults.get(0).getTime().monthOfYear().getAsShortText() + " "+messageResults.get(0).getTime().getYear());
                        List<String> images = new ArrayList<>();
                        for (MessageResult messageResult : messageResults) {
                            Message message = GsonProvider.getGson().fromJson(messageResult.getMessage(), Message.class);
                            if(message.getMessageType() == Message.MessageType.image) {
                                if(messageResult.isMe())
                                    images.add(message.getImageMessage().getFileUri());
                                else
                                    images.add(message.getImageMessage().getImageUrl());
                            }
                        }
                        Logger.d(this, "ImagesSize: "+images.size());
                        MediaAdapter mediaAdapter = new MediaAdapter(context, images);
                        gridLayout.setAdapter(mediaAdapter);

                    }
                });

        this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAnalytics.setCurrentScreen(this, AnalyticsConstants.Event.SHARED_MEDIA_SCREEN, null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        firebaseAnalytics.logEvent(AnalyticsConstants.Event.SHARED_MEDIA_BACK, null);
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
