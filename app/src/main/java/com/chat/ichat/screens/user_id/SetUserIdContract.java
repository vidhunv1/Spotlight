package com.chat.ichat.screens.user_id;

import com.chat.ichat.core.BasePresenter;
import com.chat.ichat.core.BaseView;

/**
 * Created by vidhun on 09/03/17.
 */

public interface SetUserIdContract {
    interface View extends BaseView {
        void showUserIdNotAvailableError();
        void showUserIdAvailable();
        void onSetUserIdSuccess();
        void navigateToHome();
    }

    interface Presenter extends BasePresenter<View> {
        void setUserId(String userId);
        void checkUserIdAvailable(String userId);
        void initialize();
    }
}
