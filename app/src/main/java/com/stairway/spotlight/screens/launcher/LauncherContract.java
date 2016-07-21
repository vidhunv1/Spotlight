package com.stairway.spotlight.screens.launcher;

import com.stairway.data.source.auth.UserSessionResult;
import com.stairway.spotlight.core.BasePresenter;
import com.stairway.spotlight.core.BaseView;
import com.stairway.spotlight.core.di.component.ComponentContainer;

/**
 * Created by vidhun on 21/07/16.
 */
public interface LauncherContract {
    interface View extends BaseView {
        void navigateToHomeActivity();
        void navigateToRegisterActivity();
        void updateSessionDetails(UserSessionResult userSessionResult);
    }

    interface Presenter extends BasePresenter<LauncherContract.View> {
        void getUserSession();
    }
}
