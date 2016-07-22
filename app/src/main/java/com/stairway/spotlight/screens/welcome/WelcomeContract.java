package com.stairway.spotlight.screens.welcome;

import com.stairway.spotlight.core.BasePresenter;
import com.stairway.spotlight.core.BaseView;

/**
 * Created by vidhun on 22/07/16.
 */
public interface WelcomeContract {
    interface View extends BaseView {
    }

    interface Presenter extends BasePresenter<WelcomeContract.View> {

    }
}
