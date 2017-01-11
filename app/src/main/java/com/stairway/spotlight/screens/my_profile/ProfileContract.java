package com.stairway.spotlight.screens.my_profile;

import com.stairway.data.source.user.UserSessionResult;
import com.stairway.spotlight.core.BasePresenter;
import com.stairway.spotlight.core.BaseView;

import java.io.File;

/**
 * Created by vidhun on 03/01/17.
 */

public class ProfileContract {
    interface View extends BaseView {
        void updateProfileDP(String url);
        void setProfileDP(String url);
        void setProfileDP(File file);
    }

    interface Presenter extends BasePresenter<ProfileContract.View> {
        void init(UserSessionResult userSession);
        void uploadProfileDP(File image, UserSessionResult userSession);
    }
}
