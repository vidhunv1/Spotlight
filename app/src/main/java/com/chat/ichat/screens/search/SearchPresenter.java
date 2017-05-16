package com.chat.ichat.screens.search;

import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.user.SuggestionsResponse;
import com.chat.ichat.api.user.UserApi;
import com.chat.ichat.api.user.UserResponse;
import com.chat.ichat.api.user._User;
import com.chat.ichat.core.Logger;
import com.chat.ichat.db.BotDetailsStore;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.screens.new_chat.AddContactUseCase;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 17/12/16.
 */

public class SearchPresenter implements SearchContract.Presenter {
    private CompositeSubscription compositeSubscription;
    private SearchContract.View searchView;

    public SearchPresenter() {
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void searchContacts(String name) {
        Subscription subscription = ContactStore.getInstance().getContacts(name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<ContactResult>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<ContactResult> contactResults) {
                        SearchModel searchModel = new SearchModel(name);
                        searchModel.setContactsModelList(contactResults);
                        searchView.displaySearch(searchModel);
                    }
                });
        compositeSubscription.add(subscription);
    }

    @Override
    public void init() {
        ContactStore.getInstance().getContacts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<ContactResult>>() {
                    @Override
                    public void onCompleted() {}
                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(List<ContactResult> contactResults) {
                        List<ContactResult> contactsModels = new ArrayList<>();
                        for (ContactResult contactResult : contactResults) {
                            if(!contactResult.isAdded()) {
                                contactsModels.add(contactResult);
                            }
                        }
                        SearchModel searchModel = new SearchModel(null, null, contactsModels);
                        searchView.initSearch(searchModel);
                    }
                });

        ApiManager.getUserApi().getUserSuggestions()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<SuggestionsResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.d(this, "Error fetch: ");
                        ContactStore.getInstance().getContacts()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<List<ContactResult>>() {
                                    @Override
                                    public void onCompleted() {}
                                    @Override
                                    public void onError(Throwable e) {
                                    }

                                    @Override
                                    public void onNext(List<ContactResult> contactResults) {
                                        List<ContactResult> contactsModels = new ArrayList<>();
                                        for (ContactResult contactResult : contactResults) {
                                            if(!contactResult.isAdded()) {
                                                contactsModels.add(contactResult);
                                            }
                                        }
                                        SearchModel searchModel = new SearchModel(null, null, contactsModels);
                                        searchView.initSearch(searchModel);
                                    }
                                });
                    }

                    @Override
                    public void onNext(SuggestionsResponse suggestionsResponse) {
                        List<ContactResult> contactsResultsm = new ArrayList<>(suggestionsResponse.getUsers().size());
                        for (_User user : suggestionsResponse.getUsers()) {
                            ContactResult contactResult = new ContactResult(user.getCountryCode(), user.getPhone(), user.getName());
                            contactResult.setProfileDP(user.getProfileDP());
                            contactResult.setAdded(false);
                            contactResult.setUsername(user.getUsername());
                            contactResult.setUserId(user.getUserId());
                            contactResult.setUserType(user.getUserType());
                            contactsResultsm.add(contactResult);
                        }

                        ContactStore.getInstance().getContacts()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<List<ContactResult>>() {
                                    @Override
                                    public void onCompleted() {}
                                    @Override
                                    public void onError(Throwable e) {
                                    }

                                    @Override
                                    public void onNext(List<ContactResult> contactResults) {
                                        AddContactUseCase addContactUseCase = new AddContactUseCase(ApiManager.getUserApi(), ContactStore.getInstance(), ApiManager.getBotApi(), BotDetailsStore.getInstance());
                                        List<ContactResult> getContacts = new ArrayList<>(contactsResultsm.size());
                                        getContacts.addAll(contactsResultsm);
                                        List<Observable<ContactResult>> observables = new ArrayList<>();
                                        for (ContactResult contactResult : contactResults) {
                                            if(contactResult.isAdded())
                                                contactsResultsm.remove(contactResult);
                                            getContacts.remove(contactResult);
                                        }

                                        for (ContactResult contactResult : getContacts) {
                                            observables.add(addContactUseCase.execute(contactResult.getUserId(), false));
                                        }

                                        Observable.zip(observables, (i) -> "Done Sync")
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Subscriber<String>() {
                                                    @Override
                                                    public void onCompleted() {}

                                                    @Override
                                                    public void onError(Throwable e) {}

                                                    @Override
                                                    public void onNext(String s) {
                                                        SearchModel searchModel = new SearchModel(null, null, contactsResultsm);
                                                        searchView.initSearch(searchModel);
                                                    }
                                                });
                                    }
                                });
                    }
                });

    }

    @Override
    public void attachView(SearchContract.View view) {
        this.searchView = view;
    }

    @Override
    public void detachView() {
        this.searchView = null;
    }
}
