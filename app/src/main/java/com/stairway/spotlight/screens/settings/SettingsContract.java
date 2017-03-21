package com.stairway.spotlight.screens.settings;

import com.stairway.spotlight.core.BasePresenter;
import com.stairway.spotlight.core.BaseView;

import java.io.File;

/**
 * Created by vidhun on 14/03/17.
 */

public class SettingsContract {
    interface View extends BaseView {
        void onLogoutSuccess();
        void updateProfileDP(String url);
    }

    interface Presenter extends BasePresenter<View> {
        void logoutUser();
        void uploadProfileDP(File image);
    }
}
