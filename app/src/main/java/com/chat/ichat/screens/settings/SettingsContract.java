package com.chat.ichat.screens.settings;

import com.chat.ichat.core.BasePresenter;
import com.chat.ichat.core.BaseView;

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
