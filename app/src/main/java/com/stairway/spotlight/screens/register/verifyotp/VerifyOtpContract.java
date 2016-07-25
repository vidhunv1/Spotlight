package com.stairway.spotlight.screens.register.verifyotp;

import com.stairway.data.source.auth.UserSessionResult;
import com.stairway.spotlight.core.BasePresenter;
import com.stairway.spotlight.core.BaseView;

/**
 * Created by vidhun on 25/07/16.
 */
public interface VerifyOtpContract {
    interface View extends BaseView {
        void navigateToHome(UserSessionResult userSessionResult);
        void invalidOtpError();
    }

    interface Presenter extends BasePresenter<VerifyOtpContract.View> {
        void registerUser(String mobile, String otp);
    }
}
