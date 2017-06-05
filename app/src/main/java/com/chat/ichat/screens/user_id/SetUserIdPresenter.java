package com.chat.ichat.screens.user_id;

import com.chat.ichat.MessageController;
import com.chat.ichat.UserSessionManager;
import com.chat.ichat.api.ApiError;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.app.AppApi;
import com.chat.ichat.api.phone_contacts.PhoneContactRequest;
import com.chat.ichat.api.phone_contacts.PhoneContactsApi;
import com.chat.ichat.api.phone_contacts._PhoneContact;
import com.chat.ichat.api.user.UserApi;
import com.chat.ichat.api.user.UserRequest;
import com.chat.ichat.api.user.UserResponse;
import com.chat.ichat.api.user._User;
import com.chat.ichat.application.SpotlightApplication;
import com.chat.ichat.core.Logger;
import com.chat.ichat.db.BotDetailsStore;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.db.ContactsContent;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.models.UserSession;
import com.chat.ichat.screens.new_chat.AddContactUseCase;
import com.chat.ichat.screens.search.SearchModel;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 09/03/17.
 */

public class SetUserIdPresenter implements SetUserIdContract.Presenter {
    private SetUserIdContract.View setUserIdView;
    private CompositeSubscription subscriptions;

    private UserApi userApi;
    private UserSessionManager userSessionManager;
    private PhoneContactsApi contactApi;
    private ContactsContent contactContent;
    private ContactStore appContactStore;

    public SetUserIdPresenter(UserApi userApi, UserSessionManager userSessionManager, PhoneContactsApi contactApi, ContactsContent contactContent, ContactStore appContactStore) {
        this.userApi = userApi;
        this.contactApi = contactApi;
        this.contactContent = contactContent;
        this.appContactStore = appContactStore;
        this.userSessionManager = userSessionManager;
        subscriptions = new CompositeSubscription();
    }

    @Override
    public void checkUserIdAvailable(String userId) {
        Subscription subscription = userApi.findUserByUserId(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        ApiError error = new ApiError(e);
                        setUserIdView.showError(error.getTitle(), error.getMessage());
                    }

                    @Override
                    public void onNext(UserResponse userResponse) {
                        if(!userResponse.isSuccess()) {
                            if(userResponse.getError().getCode() == 404) {
                                setUserIdView.showUserIdAvailable();
                            }
                        } else {
                            setUserIdView.showUserIdNotAvailableError();
                        }
                    }
                });
        subscriptions.add(subscription);
    }

    @Override
    public void setUserId(String userId) {
        UserRequest request = new UserRequest();
        _User user = new _User();
        user.setUserId(userId);
        request.setUser(user);

        Subscription subscription = userApi.updateUser(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserResponse>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        ApiError error = new ApiError(e);
                        e.printStackTrace();
                        setUserIdView.showError(error.getTitle(), error.getMessage());
                    }

                    @Override
                    public void onNext(UserResponse userResponse) {
                        if(!userResponse.isSuccess()) {
                            if(userResponse.getError().getCode() == 409) {
                                setUserIdView.showError("User ID", "This User ID is not available.");
                            } else {
                                setUserIdView.showError(userResponse.getError().getTitle(), userResponse.getError().getTitle());
                            }
                        } else {
                            UserSession userSession = new UserSession();
                            userSession.setUserId(userResponse.getUser().getUserId());
                            userSession.setUserName(userResponse.getUser().getUsername());
                            userSessionManager.save(userSession);

                            SpotlightApplication.getContext().initSession();
                            setUserIdView.onSetUserIdSuccess();
                        }
                    }
                });
        subscriptions.add(subscription);
    }

    @Override
    public void initialize() {
        Subscription subscription = contactContent.getContacts()
                .subscribe(new Subscriber<List<ContactResult>>() {
                    @Override
                    public void onCompleted() {}
                    @Override
                    public void onError(Throwable e) {
                        setUserIdView.navigateToHome();
                    }

                    @Override
                    public void onNext(List<ContactResult> contactResults) {
                        contactApi.createContacts(new PhoneContactRequest(contactResults))
                                .map(contactResponse -> {
                                    List<ContactResult>  contacts = new ArrayList<>(contactResponse.getContacts().size());
                                    Logger.d(this,contactResponse.getContacts().size()+"");
                                    for (_PhoneContact contact : contactResponse.getContacts()) {
                                        Logger.d(this, contact.toString());
                                        ContactResult contactResult = new ContactResult(contact.getCountryCode(), contact.getPhone(), contact.getName());
                                        contactResult.setUsername(contact.getUsername());
                                        contactResult.setUserId(contact.getUserId());
                                        contactResult.setAdded(true);
                                        contactResult.setBlocked(false);
                                        contactResult.setProfileDP(contact.getProfileDP());
                                        contactResult.setRegistered(contact.isRegistered());
                                        contacts.add(contactResult);
                                    }
                                    return contacts; })
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<List<ContactResult>>() {
                                    @Override
                                    public void onCompleted() {}
                                    @Override
                                    public void onError(Throwable e) {
                                        e.printStackTrace();
                                        Logger.d(this, "error1: "+e.getMessage());
                                        setUserIdView.navigateToHome();
                                    }

                                    @Override
                                    public void onNext(List<ContactResult> contacts) {
                                        AddContactUseCase addContactUseCase = new AddContactUseCase(ApiManager.getUserApi(), ContactStore.getInstance(), ApiManager.getBotApi(), BotDetailsStore.getInstance());
                                        List<Observable<ContactResult>> observables = new ArrayList<>();
                                        for (ContactResult contactResult : contacts) {
                                            // default behaviour, we auto add phone contacts
                                            //TODO: [1]Sync phone contacts not working
                                            if(contactResult.isRegistered()) {
                                                MessageController.getInstance().getLastActivity(contactResult.getUsername());
                                                observables.add(addContactUseCase.execute(contactResult.getUserId(), false));
                                            }
                                        }

                                        Observable.zip(observables, (i) -> "Done Sync")
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Subscriber<String>() {
                                                    @Override
                                                    public void onCompleted() {}

                                                    @Override
                                                    public void onError(Throwable e) {
                                                        Logger.d(this, "error2: "+e.getMessage());
                                                        setUserIdView.navigateToHome();
                                                    }

                                                    @Override
                                                    public void onNext(String s) {
                                                        Logger.d(this, "onNext: ");
                                                        setUserIdView.navigateToHome();
                                                    }
                                                });
                                    }
                                });
                    }
                });
        subscriptions.add(subscription);
    }

    @Override
    public void attachView(SetUserIdContract.View view) {
        this.setUserIdView = view;
    }

    @Override
    public void detachView() {
        this.setUserIdView = null;
        subscriptions.clear();
    }
}
