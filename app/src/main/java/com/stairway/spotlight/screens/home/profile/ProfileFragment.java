package com.stairway.spotlight.screens.home.profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseFragment;
import com.stairway.spotlight.core.di.component.ComponentContainer;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vidhun on 04/10/16.
 */

public class ProfileFragment extends BaseFragment {
    @Bind(R.id.iv_profileImage)
    ImageView profileImage;

    public ProfileFragment() {
    }

    public static ProfileFragment getInstance() {
        ProfileFragment profileFragment = new ProfileFragment();
        return profileFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);
        profileImage.setImageResource(R.drawable.default_profile_image);
        return view;
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
    }
}