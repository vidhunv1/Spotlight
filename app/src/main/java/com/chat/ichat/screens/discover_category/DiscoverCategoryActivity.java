package com.chat.ichat.screens.discover_category;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.chat.ichat.R;
import com.chat.ichat.api.ApiError;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.bot.DiscoverBotsResponse;
import com.chat.ichat.db.BotDetailsStore;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.screens.message.MessageActivity;
import com.chat.ichat.screens.new_chat.AddContactUseCase;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.Serializable;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vidhun on 30/07/17.
 */

public class DiscoverCategoryActivity extends AppCompatActivity {

    private static String KEY_BOTS = "DiscoverCategoryActivity.Bots";

    private FirebaseAnalytics firebaseAnalytics;
    private final String SCREEN_NAME = "discover_category";
    private List<DiscoverBotsResponse.Bots> botsList;

    @Bind(R.id.tb_message_title)
    TextView actionBarTitle;
    @Bind(R.id.tb)
    Toolbar toolbar;
    @Bind(R.id.rv)
    RecyclerView recyclerView;

    public static Intent callingIntent(Context context, List<DiscoverBotsResponse.Bots> botsList) {
        Intent intent = new Intent(context, DiscoverCategoryActivity.class);
        intent.putExtra(KEY_BOTS, (Serializable)botsList);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_category);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        Intent receivedIntent = getIntent();
        if(!receivedIntent.hasExtra(KEY_BOTS))
            return;

        botsList = (List<DiscoverBotsResponse.Bots>)receivedIntent.getSerializableExtra(KEY_BOTS);
        actionBarTitle.setText(botsList.get(0).getCategory());
        Context context = this;
        DiscoverCategoryAdapter discoverCategoryAdapter = new DiscoverCategoryAdapter(this, botsList, new DiscoverCategoryAdapter.ContactClickListener() {
            @Override
            public void onContactItemClicked(String userId, String coverPicture, String botDescription, String category) {
                ProgressDialog[] progressDialog = new ProgressDialog[1];
                progressDialog[0] = new ProgressDialog(DiscoverCategoryActivity.this) {
                    @Override
                    public void onBackPressed() {
                        finish();
                    }};
                progressDialog[0].setMessage("loading please wait...");
                progressDialog[0].show();
                AddContactUseCase addContactUseCase = new AddContactUseCase(ApiManager.getUserApi(), ContactStore.getInstance(), ApiManager.getBotApi(), BotDetailsStore.getInstance());
                addContactUseCase.execute(userId, false)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<ContactResult>() {
                            @Override
                            public void onCompleted() {}

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onNext(ContactResult contactResult) {
                                startActivity(MessageActivity.callingIntent(context, contactResult.getUsername(), coverPicture, botDescription, category));
                                finish();
                            }
                        });
                finish();
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(discoverCategoryAdapter);
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

    @OnClick(R.id.iv_back)
    public void onBackClick() {
        super.onBackPressed();
    }
}
