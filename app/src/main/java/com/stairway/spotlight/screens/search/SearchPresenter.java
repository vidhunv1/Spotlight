package com.stairway.spotlight.screens.search;

import com.stairway.data.config.Logger;
import com.stairway.spotlight.core.UseCaseSubscriber;

import java.util.List;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 17/12/16.
 */

public class SearchPresenter implements SearchContract.Presenter {
    private CompositeSubscription compositeSubscription;
    private SearchContract.View searchView;
    private SearchContactsUseCase searchContactsUseCase;

    public SearchPresenter(SearchContactsUseCase searchContactsUseCase) {
        this.searchContactsUseCase = searchContactsUseCase;
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void searchContacts(String name) {
        Logger.d("SearchContacts presenter");
        Subscription subscription = searchContactsUseCase.execute(name)
                .subscribe(new UseCaseSubscriber<List<ContactsModel>>(searchView) {
                    @Override
                    public void onResult(List<ContactsModel> result) {
                        searchView.displayContacts(name, result);
                    }
                });

        compositeSubscription.add(subscription);
    }

    @Override
    public void searchMessages(String message) {
        //TODO
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
