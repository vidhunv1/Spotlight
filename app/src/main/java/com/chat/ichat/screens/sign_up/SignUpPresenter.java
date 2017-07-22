//package com.chat.ichat.screens.sign_up;
//
//import android.content.SharedPreferences;
//
//import com.chat.ichat.MessageController;
//import com.chat.ichat.UserSessionManager;
//import com.chat.ichat.api.ApiError;
//import com.chat.ichat.api.ApiManager;
//import com.chat.ichat.api.phone_contacts.PhoneContactRequest;
//import com.chat.ichat.api.phone_contacts.PhoneContactsApi;
//import com.chat.ichat.api.phone_contacts._PhoneContact;
//import com.chat.ichat.api.user.UserApi;
//import com.chat.ichat.api.user.UserRequest;
//import com.chat.ichat.api.user.UserResponse;
//import com.chat.ichat.api.user._User;
//import com.chat.ichat.application.SpotlightApplication;
//import com.chat.ichat.core.Logger;
//import com.chat.ichat.db.BotDetailsStore;
//import com.chat.ichat.db.ContactStore;
//import com.chat.ichat.db.ContactsContent;
//import com.chat.ichat.db.core.DatabaseManager;
//import com.chat.ichat.models.ContactResult;
//import com.chat.ichat.models.UserSession;
//import com.chat.ichat.screens.new_chat.AddContactUseCase;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import rx.Observable;
//import rx.Subscriber;
//import rx.Subscription;
//import rx.android.schedulers.AndroidSchedulers;
//import rx.schedulers.Schedulers;
//import rx.subscriptions.CompositeSubscription;
//
//import static com.chat.ichat.core.FCMRegistrationIntentService.FCM_TOKEN;
///**
// * Created by vidhun on 08/03/17.
// */
//
//public class SignUpPresenter implements SignUpContract.Presenter {
//    private SignUpContract.View signUpView;
//    private CompositeSubscription subscriptions;
//
//    private UserApi userApi;
//    private UserSessionManager userSessionManager;
//    private SharedPreferences defaultSP;
//    private ContactsContent contactsContent;
//    private PhoneContactsApi phoneContactsApi;
//
//    public SignUpPresenter(UserApi userApi, ContactsContent contactsContent, PhoneContactsApi phoneContactsApi, UserSessionManager userSessionManager, SharedPreferences defaultSP) {
//        this.userApi = userApi;
//        this.userSessionManager = userSessionManager;
//        this.defaultSP = defaultSP;
//        this.contactsContent = contactsContent;
//        this.phoneContactsApi = phoneContactsApi;
//        this.subscriptions = new CompositeSubscription();
//    }
//
//    @Override
//    public void registerUser(String fullName, String email, String password, String countryCode, String mobile, String userId, String imei) {
//        UserRequest request = new UserRequest();
//        _User user = new _User();
//        user.setName(fullName);
//        user.setEmail(email);
//        user.setPassword(password);
//        user.setCountryCode(countryCode);
//        user.setPhone(mobile);
//        user.setUserType(_User.UserType.regular);
//        user.setIMEI(imei);
//        user.setNotificationToken(defaultSP.getString(FCM_TOKEN, ""));
//        user.setUserId(userId);
//        request.setUser(user);
//
//        Subscription subscription = userApi.createUser(request)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<UserResponse>() {
//                    @Override
//                    public void onCompleted() {}
//
//                    @Override
//                    public void onError(Throwable e) {
//                        ApiError error = new ApiError(e);
//                        signUpView.showError(error.getTitle(), error.getMessage());
//                    }
//
//                    @Override
//                    public void onNext(UserResponse userResponse) {
//                        if(!userResponse.isSuccess()) {
//                            signUpView.showError(userResponse.getError().getTitle(), userResponse.getError().getMessage());
//                        } else {
//                            UserSession userSession = new UserSession(userResponse.getAccessToken(), userResponse.getExpires(), password);
//                            userSession.setName(userResponse.getUser().getName());
//                            userSession.setEmail(userResponse.getUser().getEmail());
//                            userSession.setUserId(userResponse.getUser().getUserId());
//                            userSession.setUserName(userResponse.getUser().getUsername());
//                            userSessionManager.save(userSession);
//                            ApiManager.getInstance().setAuthorization(userSession.getAccessToken());
//                            SpotlightApplication.getContext().initSession();
//                            DatabaseManager.getSQLiteHelper().clearData(DatabaseManager.getInstance().openConnection());
//                            signUpView.onUserRegistered();
//                        }
//                    }
//                });
//        subscriptions.add(subscription);
//    }
//
//    @Override
//    public void checkUserIdAvailable(String userId) {
//        Subscription subscription = userApi.findUserByUserId(userId)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<UserResponse>() {
//                    @Override
//                    public void onCompleted() {}
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                        ApiError error = new ApiError(e);
//                        signUpView.showError(error.getTitle(), error.getMessage());
//                    }
//
//                    @Override
//                    public void onNext(UserResponse userResponse) {
//                        Logger.d(this, "RESP: "+userResponse.toString());
//                        if(!userResponse.isSuccess()) {
//                            if(userResponse.getError().getCode() == 404) {
//                                signUpView.showUserIdAvailable(userId, true);
//                            }
//                        } else {
//                            signUpView.showUserIdAvailable(userId, false);
//                        }
//                    }
//                });
//        subscriptions.add(subscription);
//    }
//
//    @Override
//    public void initialize() {
//        Subscription subscription = contactsContent.getContacts()
//                .subscribe(new Subscriber<List<ContactResult>>() {
//                    @Override
//                    public void onCompleted() {}
//                    @Override
//                    public void onError(Throwable e) {
//                        signUpView.navigateToHome();
//                    }
//
//                    @Override
//                    public void onNext(List<ContactResult> contactResults) {
//                        ApiManager.getPhoneContactsApi().createContacts(new PhoneContactRequest(contactResults))
//                                .map(contactResponse -> {
//                                    List<ContactResult>  contacts = new ArrayList<>(contactResponse.getContacts().size());
//                                    Logger.d(this,contactResponse.getContacts().size()+"");
//                                    for (_PhoneContact contact : contactResponse.getContacts()) {
//                                        Logger.d(this, contact.toString());
//                                        ContactResult contactResult = new ContactResult(contact.getCountryCode(), contact.getPhone(), contact.getName());
//                                        contactResult.setUsername(contact.getUsername());
//                                        contactResult.setUserId(contact.getUserId());
//                                        contactResult.setAdded(true);
//                                        contactResult.setBlocked(false);
//                                        contactResult.setProfileDP(contact.getProfileDP());
//                                        contactResult.setRegistered(contact.isRegistered());
//                                        contacts.add(contactResult);
//                                    }
//                                    return contacts; })
//                                .subscribeOn(Schedulers.io())
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .subscribe(new Subscriber<List<ContactResult>>() {
//                                    @Override
//                                    public void onCompleted() {}
//                                    @Override
//                                    public void onError(Throwable e) {
//                                        e.printStackTrace();
//                                        Logger.d(this, "error1: "+e.getMessage());
//                                        signUpView.navigateToHome();
//                                    }
//
//                                    @Override
//                                    public void onNext(List<ContactResult> contacts) {
//                                        AddContactUseCase addContactUseCase = new AddContactUseCase(ApiManager.getUserApi(), ContactStore.getInstance(), ApiManager.getBotApi(), BotDetailsStore.getInstance());
//                                        List<Observable<ContactResult>> observables = new ArrayList<>();
//                                        for (ContactResult contactResult : contacts) {
//                                            // default behaviour, we auto add phone contacts
//                                            if(contactResult.isRegistered()) {
//                                                MessageController.getInstance().getLastActivity(contactResult.getUsername());
//                                                observables.add(addContactUseCase.execute(contactResult.getUserId(), true));
//                                            }
//                                        }
//
//                                        Observable.zip(observables, (i) -> "Done Sync")
//                                                .subscribeOn(Schedulers.io())
//                                                .observeOn(AndroidSchedulers.mainThread())
//                                                .subscribe(new Subscriber<String>() {
//                                                    @Override
//                                                    public void onCompleted() {}
//
//                                                    @Override
//                                                    public void onError(Throwable e) {
//                                                        Logger.d(this, "error2: "+e.getMessage());
//                                                        signUpView.navigateToHome();
//                                                    }
//
//                                                    @Override
//                                                    public void onNext(String s) {
//                                                        Logger.d(this, "onNext: ");
//                                                        signUpView.navigateToHome();
//                                                    }
//                                                });
//                                    }
//                                });
//                    }
//                });
//        subscriptions.add(subscription);
//    }
//
//
//    @Override
//    public void attachView(SignUpContract.View view) {
//        this.signUpView = view;
//    }
//
//    @Override
//    public void detachView() {
//        subscriptions.clear();
//        signUpView = null;
//    }
//}