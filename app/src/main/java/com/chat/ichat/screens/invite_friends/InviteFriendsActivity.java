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
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
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
    @Bind(R.id.count)
    TextView inviteCount;
    @Bind(R.id.list_identifier)
    TextView listIdentifier;
    @Bind(R.id.invite_tick)
    ImageView inviteTick;

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

        setInviteCount(0);

        selectAll.setOnCheckedChangeListener((buttonView , isChecked) -> {
            if(inviteFriendsAdapter!=null) {
                inviteFriendsAdapter.setAllSelected(isChecked);
                int size = inviteFriendsAdapter.getSelected().size();
                setInviteCount(size);
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(0xff212121);
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
            inviteCount.setVisibility(View.INVISIBLE);
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
            setInviteCount(size);
        });
        contactList.setAdapter(inviteFriendsAdapter);
        RecyclerView.LayoutManager layoutManager = contactList.getLayoutManager();
        contactList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final View child = findOneVisibleChild(0, layoutManager.getChildCount(), true, false);
                int pos = child == null ? RecyclerView.NO_POSITION : contactList.getChildAdapterPosition(child);
                if(pos <= 0) {
                    listIdentifier.setText("Favorites");
                } else {
                    listIdentifier.setText(inviteFriendsAdapter.getFirstChar(pos));
                }
            }
        });
    }

    View findOneVisibleChild(int fromIndex, int toIndex, boolean completelyVisible,
                             boolean acceptPartiallyVisible) {
        OrientationHelper helper;

        if (contactList.getLayoutManager().canScrollVertically()) {
            helper = OrientationHelper.createVerticalHelper(contactList.getLayoutManager());
        } else {
            helper = OrientationHelper.createHorizontalHelper(contactList.getLayoutManager());
        }

        final int start = helper.getStartAfterPadding();
        final int end = helper.getEndAfterPadding();
        final int next = toIndex > fromIndex ? 1 : -1;
        View partiallyVisible = null;
        for (int i = fromIndex; i != toIndex; i += next) {
            final View child = contactList.getLayoutManager().getChildAt(i);
            final int childStart = helper.getDecoratedStart(child);
            final int childEnd = helper.getDecoratedEnd(child);
            if (childStart < end && childEnd > start) {
                if (completelyVisible) {
                    if (childStart >= start && childEnd <= end) {
                        return child;
                    } else if (acceptPartiallyVisible && partiallyVisible == null) {
                        partiallyVisible = child;
                    }
                } else {
                    return child;
                }
            }
        }
        return partiallyVisible;
    }

    public void setInviteCount(int count) {
        if(count==0) {
            inviteCount.setVisibility(View.INVISIBLE);
            inviteTick.setImageAlpha(127);
            invite.setAlpha(0.5f);
        } else {
            inviteTick.setImageAlpha(255);
            invite.setAlpha(1f);
            inviteCount.setVisibility(View.VISIBLE);
            inviteCount.setText(count + "");
        }
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
