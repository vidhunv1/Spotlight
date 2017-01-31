package com.stairway.spotlight.screens.search;

import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.core.UseCaseSubscriber;
import com.stairway.spotlight.models.ContactResult;
import com.stairway.spotlight.screens.home.FindUserUseCase;

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
    private FindUserUseCase findUserUseCase;

    public SearchPresenter(SearchUseCase searchUseCase, FindUserUseCase findUserUseCase) {
        this.searchUseCase = searchUseCase;
        this.findUserUseCase = findUserUseCase;
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void search(String query) {
        Logger.d(this, "SearchContacts presenter");
        Subscription subscription = searchUseCase.execute(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UseCaseSubscriber<SearchModel>(searchView) {
                    @Override
                    public void onResult(SearchModel searchModel) {
                        searchView.displaySearch(searchModel);
                    }
                });

        compositeSubscription.add(subscription);
    }

    @Override
    public void findContact(String userId, String accessToken) {
        Subscription subscription = findUserUseCase.execute(userId, accessToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UseCaseSubscriber<ContactResult>(searchView) {
                    @Override
                    public void onResult(ContactResult result) {
                        Logger.d(this, result.toString());
                        searchView.navigateToAddContact(result);
                    }

                    @Override
                    public void onError(Throwable e) {
                        //TODO: display error in view
                        Logger.d(this, "No contact found with id: "+userId);
                        Logger.d(this,e.getMessage());
                    }
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
