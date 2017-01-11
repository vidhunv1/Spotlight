//package com.stairway.spotlight.screens.profile;
//
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.database.Cursor;
//import android.net.Uri;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.support.annotation.Nullable;
//import android.support.v4.content.FileProvider;
//import android.support.v7.app.AlertDialog;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.engine.DiskCacheStrategy;
//import com.bumptech.glide.signature.StringSignature;
//import com.stairway.data.config.Logger;
//import com.stairway.data.source.user.UserSessionResult;
//import com.stairway.spotlight.R;
//import com.stairway.spotlight.core.BaseFragment;
//import com.stairway.spotlight.core.di.component.ComponentContainer;
//import com.stairway.spotlight.core.lib.ImageUtils;
//import com.stairway.spotlight.screens.profile.di.ProfileViewModule;
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
//import static android.app.Activity.RESULT_OK;
//
///**
// * Created by vidhun on 04/10/16.
// */
////TODO: Glide cache problems
//public class ProfileFragment extends BaseFragment implements ProfileContract.View{
//    @Bind(R.id.iv_profileImage)
//    ImageView profileImage;
//
//    @Inject
//    ProfilePresenter presenter;
//
//    private UserSessionResult userSession;
//    private String currentPhotoPath;
//
//    private static final int REQUEST_GALLERY = 1;
//    private static final int REQUEST_CAMERA = 2;
//
//    public ProfileFragment() {}
//
//    public static ProfileFragment getInstance() {
//        ProfileFragment profileFragment = new ProfileFragment();
//        return profileFragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.activity_profile, container, false);
//        ButterKnife.bind(this, view);
//        Logger.d(this, userSession.toString());
//
//        return view;
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
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle("Profile Photo");
//        builder.setItems(options, ((dialog, which) -> {
//            if(which==0) {
//                // ** Load image from camera **
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                PackageManager pm = getActivity().getPackageManager();
//                if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
//                    File photoFile = null;
//                    try {
//                        photoFile = ImageUtils.createImageFile(getActivity());
//                    } catch (IOException ex) {
//                        Logger.d(this, "Error creating image file.");
//                    }
//                    if (photoFile != null) {
//                        Uri photoURI = FileProvider.getUriForFile(this.getActivity(), "com.stairway.spotlight.fileprovider", photoFile);
//                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                        currentPhotoPath = photoFile.getAbsolutePath();
//                        startActivityForResult(cameraIntent, REQUEST_CAMERA);
//                    }
//                }
//            } else if(which==1) {
//                // ** Load image from gallery **
//                Intent loadIntent = new Intent(Intent.ACTION_PICK,
//                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
//            Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
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
//        this.getActivity().sendBroadcast(mediaScanIntent);
//    }
//
//    @Override
//    protected void injectComponent(ComponentContainer componentContainer) {
//        componentContainer.userSessionComponent().plus(new ProfileViewModule()).inject(this);
//        userSession = componentContainer.userSessionComponent().getUserSession();
//    }
//}