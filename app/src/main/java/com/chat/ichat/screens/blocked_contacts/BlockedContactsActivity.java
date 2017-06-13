package com.chat.ichat.screens.blocked_contacts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chat.ichat.R;
import com.chat.ichat.core.BaseActivity;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.lib.AndroidUtils;
import com.chat.ichat.core.lib.ImageUtils;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.screens.message.MessageActivity;
import com.chat.ichat.screens.user_profile.UserProfileActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
/**
 * Created by vidhun on 12/06/17.
 */
public class BlockedContactsActivity extends BaseActivity {
    @Bind(R.id.tb)
    Toolbar toolbar;
    @Bind(R.id.rv_blocked_list)
    RecyclerView blockedList;

    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, BlockedContactsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_contacts);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        blockedList.setLayoutManager(new LinearLayoutManager(this));

        Context context = this;
        ContactStore.getInstance().getContacts()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<ContactResult>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(List<ContactResult> contactResults) {

                        List<ContactResult> cc = new ArrayList<>();
                        for (ContactResult contactResult : contactResults) {
                            if(contactResult.isBlocked())
                                cc.add(contactResult);
                        }
                        BlockedContactsAdapter blockedContactsAdapter = new BlockedContactsAdapter(context, cc, userName -> {
                            LinearLayout parent = new LinearLayout(context);

                            parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                            parent.setOrientation(LinearLayout.VERTICAL);
                            parent.setPadding((int) AndroidUtils.px(16),(int)AndroidUtils.px(8), 0, (int)AndroidUtils.px(8));

                            TextView textView1 = new TextView(context);
                            textView1.setText("Unblock");
                            textView1.setTextColor(ContextCompat.getColor(context, R.color.textColor));
                            textView1.setTextSize(16);
                            textView1.setGravity(Gravity.CENTER_VERTICAL);
                            textView1.setHeight((int)AndroidUtils.px(48));
                            textView1.setOnClickListener(v -> {
                                ContactStore.getInstance().getContactByUserName(userName)
                                        .subscribeOn(Schedulers.newThread())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Subscriber<ContactResult>() {
                                            @Override
                                            public void onCompleted() {}

                                            @Override
                                            public void onError(Throwable e) {}

                                            @Override
                                            public void onNext(ContactResult crs) {
                                                crs.setBlocked(false);
                                                ContactStore.getInstance().update(crs)
                                                        .subscribeOn(Schedulers.newThread())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(new Subscriber<ContactResult>() {
                                                            @Override
                                                            public void onCompleted() {}

                                                            @Override
                                                            public void onError(Throwable e) {}

                                                            @Override
                                                            public void onNext(ContactResult contactResult) {
                                                                startActivity(UserProfileActivity.callingIntent(context, contactResult.getUsername(), contactResult.getUserId(), contactResult.getContactName(), contactResult.isBlocked(), contactResult.getProfileDP()));
                                                            }
                                                        });
                                            }
                                        });
                            });

                            parent.addView(textView1);
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setView(parent);
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        });
                        blockedList.setAdapter(blockedContactsAdapter);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.blocked_contacts_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if((id == android.R.id.home)) {
            finish();
            return true;
        } else if(id == R.id.action_add) {
        }
        return super.onOptionsItemSelected(item);
    }
}
