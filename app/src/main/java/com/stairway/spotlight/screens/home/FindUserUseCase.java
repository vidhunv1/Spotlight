package com.stairway.spotlight.screens.home;

import com.stairway.spotlight.api.user.UserApi;
import com.stairway.spotlight.api.user.UserResponse;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.local.ContactStore;
import com.stairway.spotlight.models.ContactResult;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 07/01/17.
 */

public class FindUserUseCase {
    private UserApi userApi;
    private ContactStore contactStore;

    public FindUserUseCase(UserApi userApi, ContactStore contactStore) {
        this.userApi = userApi;
        this.contactStore = contactStore;
    }

    public Observable<ContactResult> executeLocal(String userId) {
        return Observable.create(subscriber -> {
            contactStore.getContactByUserId(userId).subscribe(new Subscriber<ContactResult>() {
                @Override
                public void onCompleted() {}

                @Override
                public void onError(Throwable e) {
                    Logger.d(this, "error getting contact");
                }

                @Override
                public void onNext(ContactResult res) {
                    if (res != null) {
                        res.setAdded(res.isAdded());
                        subscriber.onNext(res);
                        subscriber.onCompleted();
                    } else {
                        subscriber.onNext(null);
                        subscriber.onCompleted();
                    }
                }
            });
        });
    }

    public Observable<ContactResult> execute(String userId, String authToken) {
        return Observable.create(subscriber -> {

            contactStore.getContactByUserId(userId).subscribe(new Subscriber<ContactResult>() {
                @Override
                public void onCompleted() {}
                @Override
                public void onError(Throwable e) {
                    Logger.d(this, "error getting contact");
                }

                @Override
                public void onNext(ContactResult res) {
                    if(res!=null) {
                        res.setAdded(res.isAdded());
                        subscriber.onNext(res);
                        subscriber.onCompleted();
                    } else {
                        userApi.findUser(userId).subscribe(new Subscriber<UserResponse>() {
                            @Override
                            public void onCompleted() {}

                            @Override
                            public void onError(Throwable e) {
                                subscriber.onError(e);
                            }

                            @Override
                            public void onNext(UserResponse userResponse) {
                                userResponse.getUser();
                                ContactResult contactResult = new ContactResult();
                                contactResult.setRegistered(true);
                                contactResult.setUserId(userResponse.getUser().getUserId());
                                contactResult.setUsername(userResponse.getUser().getUsername());
                                contactResult.setDisplayName(userResponse.getUser().getName());
                                contactResult.setAdded(true);

                                contactStore.storeContact(contactResult).subscribe(new Subscriber<Boolean>() {
                                    @Override
                                    public void onCompleted() {}

                                    @Override
                                    public void onError(Throwable e) {}

                                    @Override
                                    public void onNext(Boolean b) {
                                        subscriber.onNext(contactResult);
                                        subscriber.onCompleted();
                                    }
                                });
                            }
                        });
                    }
                }
            });
        });
    }
}
