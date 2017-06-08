package com.chat.ichat.screens.discover_bots;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.chat.ichat.R;
import com.chat.ichat.api.bot.DiscoverBotsResponse;
import com.chat.ichat.core.BaseActivity;
import com.chat.ichat.screens.message.MessageActivity;
import com.chat.ichat.screens.people_nearby.PeopleNearbyActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
/**
 * Created by vidhun on 30/05/17.
 */
public class DiscoverBotsActivity extends BaseActivity implements DiscoverBotsContract.View, DiscoverBotsAdapter.ContactClickListener{
    @Bind(R.id.tb)
    Toolbar toolbar;

    @Bind(R.id.rv_bots)
    RecyclerView recyclerView;

    DiscoverBotsPresenter discoverBotsPresenter;
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void onResume() {
        discoverBotsPresenter.attachView(this);
        super.onResume();
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
            super.onBackPressed();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        if(progressDialog[0].isShowing())
            progressDialog[0].dismiss();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DiscoverBotsAdapter discoverBotsAdapter = new DiscoverBotsAdapter(this, discoverBotsResponse, this);
        recyclerView.setAdapter(discoverBotsAdapter);
    }

    @Override
    public void onContactItemClicked(String userId) {
        discoverBotsPresenter.openContact(userId);
    }

    @Override
    public void navigateToMessage(String username) {
        startActivity(MessageActivity.callingIntent(this, username));
    }
}
