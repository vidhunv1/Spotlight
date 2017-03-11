package com.stairway.spotlight.screens.user_id;

import com.stairway.spotlight.core.BasePresenter;
import com.stairway.spotlight.core.BaseView;

/**
 * Created by vidhun on 09/03/17.
 */

public class SetUserIdContract {
    interface View extends BaseView {
        void showUserIdNotAvailableError();
        void navigateToHome();
    }

    interface Presenter extends BasePresenter<View> {
        void setUserId(String userId);
    }
}
