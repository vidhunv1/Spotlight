package com.chat.ichat.screens.search;

import com.chat.ichat.core.BasePresenter;
import com.chat.ichat.core.BaseView;

/**
 * Created by vidhun on 17/12/16.
 */

public interface SearchContract {
    interface View extends BaseView {
        void displaySearch(SearchModel searchModel);
    }
    interface Presenter extends BasePresenter<SearchContract.View> {
        void search(String query);
    }
}
