package com.chat.ichat.screens.discover_bots;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.chat.ichat.R;
import com.chat.ichat.api.bot.DiscoverBotsResponse;
import com.chat.ichat.config.AnalyticsConstants;
import com.chat.ichat.core.BaseActivity;
import com.chat.ichat.core.Logger;
import com.chat.ichat.screens.message.MessageActivity;
import com.chat.ichat.screens.search.SearchActivity;
import com.google.firebase.analytics.FirebaseAnalytics;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
/**
 * Created by vidhun on 30/05/17.
 */
public class DiscoverBotsActivity extends BaseActivity implements DiscoverBotsContract.View, DiscoverBotsAdapter.ContactClickListener {
    @Bind(R.id.tb)
    Toolbar toolbar;

    @Bind(R.id.rv_bots)
    RecyclerView recyclerView;

    DiscoverBotsPresenter discoverBotsPresenter;
    private FirebaseAnalytics firebaseAnalytics;
    final ProgressDialog[] progressDialog = new ProgressDialog[1];

    public static Intent callingIntent(Context context) {
        return new Intent(context, DiscoverBotsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_bots);
        ButterKnife.bind(this);

        discoverBotsPresenter = new DiscoverBotsPresenter();
        discoverBotsPresenter.attachView(this);
        progressDialog[0] = new ProgressDialog(DiscoverBotsActivity.this) {
            @Override
            public void onBackPressed() {
                finish();
            }};
        progressDialog[0].setMessage("loading please wait...");
        progressDialog[0].show();
        discoverBotsPresenter.discoverBots();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onResume() {
        discoverBotsPresenter.attachView(this);
        super.onResume();
        firebaseAnalytics.setCurrentScreen(this, AnalyticsConstants.Event.DISCOVER_BOTS_SCREEN, null);
    }

    @Override
    public void onBackPressed() {
        firebaseAnalytics.setCurrentScreen(this, AnalyticsConstants.Event.DISCOVER_BOTS_BACK, null);
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        discoverBotsPresenter.detachView();
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if ((id == android.R.id.home)) {
            onBackPressed();
            finish();
            return true;
        } else if(id == R.id.search) {
            startActivity(SearchActivity.callingIntent(this));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.discoverbots_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void showError(String title, String message) {
        if(progressDialog[0].isShowing())
            progressDialog[0].dismiss();
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage("\n"+message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    @Override
    public void displayBots(DiscoverBotsResponse discoverBotsResponse) {
        Logger.d(this, "DiscoverBotsResponse: "+discoverBotsResponse.toString());
        if(progressDialog[0].isShowing())
            progressDialog[0].dismiss();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        DiscoverBotsAdapter discoverBotsAdapter = new DiscoverBotsAdapter(this, discoverBotsResponse, this);
        recyclerView.setAdapter(discoverBotsAdapter);
    }

    @Override
    public void onContactItemClicked(String userId, String coverPicture, String description, String category) {
        progressDialog[0] = new ProgressDialog(DiscoverBotsActivity.this) {
            @Override
            public void onBackPressed() {
                finish();
            }};
        progressDialog[0].setMessage("loading please wait...");
        progressDialog[0].show();
        /*              Analytics           */
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_ID, userId);
        firebaseAnalytics.logEvent(AnalyticsConstants.Event.DISCOVER_BOTS_CHAT_OPEN, bundle);
        discoverBotsPresenter.openContact(userId, coverPicture, description, category);
    }

    @Override
    public void navigateToMessage(String username, String coverPicture, String description, String botCategory) {
        startActivity(MessageActivity.callingIntent(this, username, coverPicture, description, botCategory));
        finish();
    }

    @OnClick(R.id.iv_back)
    public void onBackClicked() {
        super.onBackPressed();
    }
}
