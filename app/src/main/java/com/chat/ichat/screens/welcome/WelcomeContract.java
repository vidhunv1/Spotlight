package com.chat.ichat.screens.welcome;

import com.chat.ichat.core.BasePresenter;
import com.chat.ichat.core.BaseView;

/**
 * Created by vidhun on 22/07/16.
 */
public interface WelcomeContract {
    interface View extends BaseView {
    }

    interface Presenter extends BasePresenter<WelcomeContract.View> {
    }
}
