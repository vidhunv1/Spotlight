package com.stairway.spotlight.screens.my_profile;

import com.stairway.data.config.Logger;
import com.stairway.data.source.user.UserApi;
import com.stairway.data.source.user.UserSessionResult;
import com.stairway.data.source.user.UserSessionStore;
import com.stairway.data.source.user.gson_models.UserResponse;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vidhun on 03/01/17.
 */

public class UpdateProfileDPUseCase {
    private UserApi userApi;
    private UserSessionStore userSessionStore;

    @Inject
    public UpdateProfileDPUseCase(UserApi userApi, UserSessionStore userSessionStore) {
        this.userApi = userApi;
        this.userSessionStore = userSessionStore;
    }

    public Observable<UserResponse> execute(File imagefile, UserSessionResult userSession) {
        return Observable.create(subscriber -> {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = userSession.getUserId()+"_"+timeStamp;
            int i = imagefile.getName().lastIndexOf('.');
            if (i > 0) {
                filename = filename+imagefile.getName().substring(i);
            } else {
                filename = filename+"."+imagefile.getName();
            }
            userApi.uploadProfileDP(imagefile, filename, userSession.getAccessToken()).subscribe(new Subscriber<UserResponse>() {
                @Override
                public void onCompleted() {}
                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    Logger.d(this, e.getMessage());
                }
                @Override
                public void onNext(UserResponse userResponse) {
                    UserSessionResult userSessionResult = new UserSessionResult();
                    userSessionResult.setProfileDP(userResponse.getUser().getProfileDP());

                    updateToken(userSessionResult);
                    subscriber.onNext(userResponse);
                }
            });
        });
    }

    private void updateToken(UserSessionResult userSessionResult) {
        userSessionResult.setProfileDP(userSessionResult.getProfileDP().replace("https://", "http://"));

        Observable<Boolean> putUserSession = userSessionStore.putUserSession(userSessionResult)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());

        putUserSession.subscribe(new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {}
            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Logger.e(this, "Error storing new Token to UserSessionStore");
            }

            @Override
            public void onNext(Boolean aBoolean) {
                if(aBoolean)
                    Logger.d(this, "Stored new Session token to UserSessionStore");
            }
        });
    }
}
