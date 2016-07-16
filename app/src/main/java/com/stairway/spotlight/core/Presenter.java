package com.stairway.spotlight.core;

/**
 * Created by vidhun on 04/07/16.
 */
public interface Presenter<V extends View> {
    void attachView(V view);
    void detachView();

}
