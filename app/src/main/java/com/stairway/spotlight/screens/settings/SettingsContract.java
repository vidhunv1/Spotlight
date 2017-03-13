package com.stairway.spotlight.screens.settings;

import com.stairway.spotlight.core.BasePresenter;
import com.stairway.spotlight.core.BaseView;

/**
 * Created by vidhun on 14/03/17.
 */

public class SettingsContract {
    interface View extends BaseView {
        void onLogoutSuccess();
    }

    interface Presenter extends BasePresenter<View> {
        void logoutUser();
    }
}
