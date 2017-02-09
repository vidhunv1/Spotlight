package com.stairway.spotlight.screens.search;

import com.stairway.spotlight.core.BasePresenter;
import com.stairway.spotlight.core.BaseView;
import com.stairway.spotlight.models.ContactResult;

import java.util.List;

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
