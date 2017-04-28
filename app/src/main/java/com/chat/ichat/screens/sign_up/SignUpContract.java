package com.chat.ichat.screens.sign_up;

import com.chat.ichat.core.BasePresenter;
import com.chat.ichat.core.BaseView;

/**
 * Created by vidhun on 08/03/17.
 */

public interface SignUpContract {
    interface View extends BaseView {
        void navigateToSetUserID();
        void showVerifyingOtp(String verificationUUID);
    }

    interface Presenter extends BasePresenter<SignUpContract.View> {
        void registerUser(String fullName, String email, String password, String countryCode, String mobile, String imei);
        void verifyOTP(String countryCode, String phone, String verificationCode, String verificationUUID);
    }
}
