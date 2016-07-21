package com.stairway.spotlight.core;

/**
 * Created by vidhun on 04/07/16.
 */
public interface BasePresenter<V extends BaseView> {
    void attachView(V view);
    void detachView();

}
