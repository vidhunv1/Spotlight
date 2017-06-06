package com.chat.ichat.screens.invite_friends;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chat.ichat.R;
import com.chat.ichat.core.BaseActivity;
import com.chat.ichat.core.Logger;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.db.ContactsContent;
import com.chat.ichat.models.ContactResult;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by vidhun on 03/06/17.
 */

public class InviteFriendsActivity extends BaseActivity implements InviteFriendsContract.View{
    @Bind(R.id.tb_invite_friends)
    Toolbar toolbar;
    @Bind(R.id.rv_invite_friends)
    RecyclerView contactList;
    @Bind(R.id.checkbox)
    CheckBox selectAll;
    @Bind(R.id.rl_invite_friends)
    RelativeLayout inviteFriends;
    @Bind(R.id.invite)
    TextView invite;

    InviteFriendsPresenter inviteFriendsPresenter;
    InviteFriendsAdapter inviteFriendsAdapter;

    public static Intent callingIntent(Context context) {
        Intent intent = new Intent(context, InviteFriendsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        contactList.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.ItemAnimator animator = contactList.getItemAnimator();
        if (animator instanceof SimpleItemAnimator)
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);

        ContactsContent contactsContent = new ContactsContent(this);
        inviteFriendsPresenter = new InviteFriendsPresenter(contactsContent, ContactStore.getInstance());
        inviteFriendsPresenter.attachView(this);

        selectAll.setOnCheckedChangeListener((buttonView , isChecked) -> {
            if(inviteFriendsAdapter!=null) {
                inviteFriendsAdapter.setAllSelected(isChecked);
            }
        });

        // Permissions
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            int permission = PackageManager.PERMISSION_GRANTED;
            int result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);

            if(!(result2 == permission)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 102);
            } else {
                inviteFriendsPresenter.getInviteList();
            }
        } else {
            inviteFriendsPresenter.getInviteList();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        inviteFriendsPresenter.attachView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        inviteFriendsPresenter.detachView();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if((id == android.R.id.home)) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.rl_invite_friends)
    public void onInviteFriendsClicked() {
        // Permissions
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            int permission = PackageManager.PERMISSION_GRANTED;
            int result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);

            if(!(result1==permission)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 101);
            } else {
                inviteFriends();
            }
        } else {
            inviteFriends();
        }
    }

    public void inviteFriends() {
        if(inviteFriendsAdapter!=null) {
            Toast.makeText(getApplicationContext(), "Inviting", Toast.LENGTH_SHORT).show();
            List<ContactResult> cr = inviteFriendsAdapter.getSelected();
            Logger.d(this, "Inviting: "+cr.size());
            SmsManager smsManager = SmsManager.getDefault();
            PendingIntent sentPI;
            sentPI = PendingIntent.getBroadcast(this, 0,new Intent("SMS_SENT"), 0);
            inviteFriendsAdapter.setAllSelected(false);
            invite.setText("INVITE");
            for (ContactResult contactResult : cr) {
                String phone = contactResult.getCountryCode()+contactResult.getPhoneNumber();
                try {
                    smsManager.sendTextMessage(phone, null, "Try out iChat!", sentPI, null);
                    Toast.makeText(this, "SMS Sent to "+contactResult.getContactName(), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "SMS to "+contactResult.getContactName()+"failed, please try again later!", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void showError(String title, String message) {}

    @Override
    public void displayInviteList(List<ContactResult> contactResultList) {
        inviteFriendsAdapter = new InviteFriendsAdapter(this, contactResultList, (phone, countryCode) -> {
            int size = inviteFriendsAdapter.getSelected().size();
            Logger.d(this, "onChecked total: "+size);
            if(size == 0) {
                invite.setText("INVITE");
            } else {
                invite.setText("INVITE ("+size+")");
            }
        });
        contactList.setAdapter(inviteFriendsAdapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    inviteFriends();
                } else {
                    //not granted
                }
                break;
            case 102:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    inviteFriendsPresenter.getInviteList();
                } else {
                    //not granted
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
