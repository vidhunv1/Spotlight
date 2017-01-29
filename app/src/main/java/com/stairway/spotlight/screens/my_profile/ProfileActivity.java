//package com.stairway.spotlight.screens.my_profile;
//
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.database.Cursor;
//import android.net.Uri;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.support.v4.content.FileProvider;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.widget.Toolbar;
//import android.view.MenuItem;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.engine.DiskCacheStrategy;
//import com.bumptech.glide.signature.StringSignature;
//import com.stairway.data.config.Logger;
//import com.stairway.data.source.user.UserSessionResult;
//import com.stairway.spotlight.R;
//import com.stairway.spotlight.core.BaseActivity;
//import com.stairway.spotlight.core.di.component.ComponentContainer;
//import com.stairway.spotlight.core.lib.ImageUtils;
//import com.stairway.spotlight.screens.home.HomeActivity;
//import com.stairway.spotlight.screens.my_profile.di.ProfileViewModule;
//
//import java.io.File;
//import java.io.IOException;
//
//import javax.inject.Inject;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//
///**
// * Created by vidhun on 08/01/17.
// */
//
//public class ProfileActivity extends BaseActivity implements ProfileContract.View {
//    @Bind(R.id.iv_profileImage)
//    ImageView profileImage;
//
//    @Inject
//    ProfilePresenter presenter;
//
//    @Bind(R.id.tb_profile)
//    Toolbar toolbar;
//
//    @Bind(R.id.tb_profile_title)
//    TextView title;
//
//    private UserSessionResult userSession;
//    private String currentPhotoPath;
//
//    private static final int REQUEST_GALLERY = 1;
//    private static final int REQUEST_CAMERA = 2;
//
//    public static Intent callingIntent(Context context) {
//        return new Intent(context, ProfileActivity.class);
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_profile);
//
//        ButterKnife.bind(this);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        title.setText("  Profile");
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if((item.getItemId() == android.R.id.home)) {
//            super.onBackPressed();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        presenter.attachView(this);
//        presenter.init(userSession);
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        presenter.detachView();
//    }
//
//    @OnClick(R.id.iv_profileImage)
//    public void onProfileClicked() {
//        CharSequence options[] = new CharSequence[] {"Camera", "Gallery"};
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Profile Photo");
//        builder.setItems(options, ((dialog, which) -> {
//            if(which==0) {
//                // ** Load image from camera **
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                PackageManager pm = this.getPackageManager();
//                if (cameraIntent.resolveActivity(this.getPackageManager()) != null && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
//                    File photoFile = null;
//                    try {
//                        photoFile = ImageUtils.createImageFile(this);
//                    } catch (IOException ex) {
//                        Logger.d(this, "Error creating image file.");
//                    }
//                    if (photoFile != null) {
//                        Uri photoURI = FileProvider.getUriForFile(this, "com.stairway.spotlight.fileprovider", photoFile);
//                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                        currentPhotoPath = photoFile.getAbsolutePath();
//                        startActivityForResult(cameraIntent, REQUEST_CAMERA);
//                    }
//                }
//            } else if(which==1) {
//                // ** Load image from gallery **
//                Intent loadIntent = new Intent(Intent.ACTION_PICK,
//                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//
//                startActivityForResult(loadIntent, REQUEST_GALLERY);
//            }
//        }));
//        builder.show();
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK && data!=null) {
//            Uri selectedImage = data.getData();
//            Logger.d(this, selectedImage.toString());
//            String[] filePathColumn = { MediaStore.Images.Media.DATA };
//
//            Cursor cursor = this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
//            cursor.moveToFirst();
//            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            String picturePath = cursor.getString(columnIndex);
//            cursor.close();
//
//            setProfileDP(new File(picturePath));
//            presenter.uploadProfileDP(new File(picturePath), userSession);
//
//        } else if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
//            if(currentPhotoPath!=null) {
//                setProfileDP(new File(currentPhotoPath));
//                presenter.uploadProfileDP(new File(currentPhotoPath), userSession);
//                galleryAddPic(currentPhotoPath);
//            }
//        }
//    }
//
//    @Override
//    public void updateProfileDP(String url) {
//        Logger.d(this, "update dp: "+url);
//        long millis = System.currentTimeMillis() / (24 * 60 * 60 * 1000);
//        StringSignature signature = new StringSignature(String.valueOf(millis));
//        Glide.with(this).load(url)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .skipMemoryCache(true)
//                .signature(signature)
//                .into(profileImage);
//    }
//
//    @Override
//    public void setProfileDP(String url) {
//        Logger.d(this, "set dp:"+url);
//        Glide.with(this).load(url)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .skipMemoryCache(true)
//                .into(profileImage);
//    }
//
//    @Override
//    public void setProfileDP(File file) {
//        Uri uri = Uri.fromFile(file);
//        profileImage.setImageURI(uri);
//    }
//
//    private void galleryAddPic(String path) {
//        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        File f = new File(path);
//        Uri contentUri = Uri.fromFile(f);
//        mediaScanIntent.setData(contentUri);
//        this.sendBroadcast(mediaScanIntent);
//    }
//    @Override
//    protected void injectComponent(ComponentContainer componentContainer) {
//        componentContainer.userSessionComponent().plus(new ProfileViewModule()).inject(this);
//        userSession = componentContainer.userSessionComponent().getUserSession();
//    }
//
//}
