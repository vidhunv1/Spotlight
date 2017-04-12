package com.chat.ichat.screens.launcher;

import com.chat.ichat.core.BasePresenter;
import com.chat.ichat.core.BaseView;

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
