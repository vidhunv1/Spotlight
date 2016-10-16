package com.stairway.spotlight.screens.register.signup;

import com.stairway.spotlight.core.BasePresenter;
import com.stairway.spotlight.core.BaseView;

/**
 * Created by vidhun on 16/10/16.
 */

public interface SignUpContract {
    interface View extends BaseView{
        void navigateToVerifyOtp(String countryCode, String phone);
    }

    interface Presenter extends BasePresenter<SignUpContract.View>{
        void createUser(String countryCode, String phone);
    }
}
