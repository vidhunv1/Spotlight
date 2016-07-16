package com.stairway.spotlight.core;

import com.stairway.data.manager.Logger;

import rx.Subscriber;

/**
 * Created by vidhun on 16/07/16.
 */
public abstract class UseCaseSubscriber<T> extends Subscriber<T>
{
    protected View baseView;

    public UseCaseSubscriber(View view)
    {
        baseView = view;
    }

    @Override
    public void onCompleted()
    {

    }

    @Override
    public void onNext(T t)
    {
        onResult(t);
    }

    @Override
    public void onError(Throwable e)
    {
        // TODO: Call based on error

        // if network error, onNetworkError()
        // if server error, on ServerError()
        // id sessionExpired, sessionExpired()
    }

    public void onNetworkError()
    {
        Logger.v("NETWORK ERROR");

        // TODO: Handle Network error.
    }

    public void onServerError() {
        Logger.v("Server error");

        // TODO: Handle server error.
    }

    public void sessionExpired()
    {
        Logger.v("Session expired.");

        // TODO: Handle session error
    }

    public abstract void onResult(T result);
}

