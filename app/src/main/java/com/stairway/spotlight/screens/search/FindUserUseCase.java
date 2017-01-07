package com.stairway.spotlight.screens.search;

import com.stairway.data.source.contacts.ContactResult;
import com.stairway.data.source.user.UserApi;
import com.stairway.data.source.user.gson_models.UserResponse;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 07/01/17.
 */

public class FindUserUseCase {
    private UserApi userApi;

    @Inject
    public FindUserUseCase(UserApi userApi) {
        this.userApi = userApi;
    }

    public Observable<ContactResult> execute(String userName, String authToken) {
        return Observable.create(subscriber -> {
            userApi.findUser(userName, authToken).subscribe(new Subscriber<UserResponse>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    subscriber.onError(e);
                }

                @Override
                public void onNext(UserResponse userResponse) {
                    userResponse.getUser();
                    ContactResult contactResult = new ContactResult();
                    contactResult.setAdded(true);
                    contactResult.setRegistered(true);
                    contactResult.setUserId(userResponse.getUser().getUserId());
                    contactResult.setUsername(userResponse.getUser().getUsername());
                    contactResult.setDisplayName(userResponse.getUser().getName());

                    subscriber.onNext(contactResult);
                    subscriber.onCompleted();
                }
            });
        });
    }
}
