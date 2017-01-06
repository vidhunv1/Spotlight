package com.stairway.spotlight.screens.user_profile;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.stairway.data.config.Logger;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseActivity;
import com.stairway.spotlight.core.di.component.ComponentContainer;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
public class UserProfileActivity extends BaseActivity {
    @Bind(R.id.iv_userprofile_dp)
    ImageView profileDP;

    private static int RESULT_LOAD_IMAGE = 1;

    private static String USER_ID = "USERID";
    public static Intent callingIntent(Context context, String userId) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra(USER_ID, userId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);

        profileDP.setImageResource(R.drawable.default_profile_image);
    }

    @OnClick(R.id.iv_userprofile_dp)
    public void onProfileClicked() {
        Logger.d(this, "Profile clicked");
        Intent loadIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(loadIntent, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            Logger.d(this, selectedImage.toString());
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            profileDP.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
    }
}
