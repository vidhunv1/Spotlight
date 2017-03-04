package com.stairway.spotlight.screens.user_profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.lib.ImageUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
public class UserProfileActivity extends BaseActivity {
    @Bind(R.id.iv_userprofile_dp)
    ImageView profileDP;

    @Bind(R.id.tb_user_profile)
    Toolbar toolbar;

    @Bind(R.id.user_profile_message)
    FloatingActionButton fab;

    @Bind(R.id.tv_contact_name)
    TextView contactNameView;

    @Bind(R.id.tv_user_profile_id)
    TextView userIdView;

    private static int RESULT_LOAD_IMAGE = 1;
    private String username;
    private String contactName;

    private static String KEY_USER_NAME = "UserProfileActivity.USER_NAME";
    private static String KEY_CONTACT_NAME = "UserProfileActivity.CONTACT_NAME";
    public static Intent callingIntent(Context context, String userId, String contactName) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(KEY_USER_NAME, userId);
        intent.putExtra(KEY_CONTACT_NAME, contactName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);
        Intent receivedIntent = getIntent();
        if(!receivedIntent.hasExtra(KEY_USER_NAME))
            return;

        username = receivedIntent.getStringExtra(KEY_USER_NAME);
        contactName = receivedIntent.getStringExtra(KEY_CONTACT_NAME);

        contactNameView.setText(contactName);
        userIdView.setText("airtel");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        profileDP.setImageDrawable(ImageUtils.getDefaultProfileImage(contactName, username, 25.5));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_profile_toolbar, menu);

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

    @OnClick(R.id.user_profile_message)
    public void onMessageClicked() {
        onBackPressed();
    }

//    @OnClick(R.id.iv_userprofile_dp)
//    public void onProfileClicked() {
//        Logger.d(this, "Profile clicked");
//        Intent loadIntent = new Intent(Intent.ACTION_PICK,
//                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//
//        startActivityForResult(loadIntent, RESULT_LOAD_IMAGE);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
//            Uri selectedImage = data.getData();
//            Logger.d(this, selectedImage.toString());
//            String[] filePathColumn = { MediaStore.Images.Media.DATA };
//
//            Cursor cursor = getContentResolver().query(selectedImage,
//                    filePathColumn, null, null, null);
//            cursor.moveToFirst();
//
//            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            String picturePath = cursor.getString(columnIndex);
//            cursor.close();
//
//            profileDP.setImageBitmap(BitmapFactory.decodeFile(picturePath));
//        }
//    }
}