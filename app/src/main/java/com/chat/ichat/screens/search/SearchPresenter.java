package com.chat.ichat.screens.search;

import com.chat.ichat.core.Logger;

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
    private SearchUseCase searchUseCase;

    public SearchPresenter(SearchUseCase searchUseCase) {
        this.searchUseCase = searchUseCase;
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void search(String query) {
        Logger.d(this, "SearchContacts presenter");
        Subscription subscription = searchUseCase.execute(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(searchModel -> {
                    searchView.displaySearch(searchModel);
                });

        compositeSubscription.add(subscription);
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
