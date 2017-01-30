package com.stairway.spotlight.screens.launcher;

import com.stairway.spotlight.core.BasePresenter;
import com.stairway.spotlight.core.BaseView;

/**
 * Created by vidhun on 21/07/16.
 */
public interface LauncherContract {
    interface View extends BaseView {
        void navigateToHomeActivity();
        void navigateToWelcomeActivity();
    }

    interface Presenter extends BasePresenter<LauncherContract.View> {
        void getUserSession();
    }
}
