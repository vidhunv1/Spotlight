package com.stairway.spotlight.screens.login;

import com.stairway.spotlight.core.BasePresenter;
import com.stairway.spotlight.core.BaseView;

/**
 * Created by vidhun on 12/03/17.
 */

public interface LoginContract {
    interface View extends BaseView {
        void navigateToHome();
        void navigateToSetUserId();
    }

    interface Presenter extends BasePresenter<LoginContract.View> {
        void loginUser(String account, String password);
    }
}
