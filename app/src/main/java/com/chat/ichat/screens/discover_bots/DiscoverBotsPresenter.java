package com.chat.ichat.screens.discover_bots;

import com.chat.ichat.api.ApiError;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.bot.DiscoverBotsResponse;
import com.chat.ichat.core.Logger;
import com.chat.ichat.db.BotDetailsStore;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.screens.new_chat.AddContactUseCase;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 01/06/17.
 */

public class DiscoverBotsPresenter implements DiscoverBotsContract.Presenter {
    private CompositeSubscription compositeSubscription;
    private DiscoverBotsContract.View botsView;

    public DiscoverBotsPresenter() {
        this.compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void discoverBots() {
        ApiManager.getBotApi().discoverBots()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<DiscoverBotsResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        ApiError error = new ApiError(e);
                        botsView.showError(error.getTitle(), error.getMessage());
                    }

                    @Override
                    public void onNext(DiscoverBotsResponse discoverBotsResponse) {
                        Logger.d(this, "DBOTS: "+discoverBotsResponse.toString());
                        botsView.displayBots(discoverBotsResponse);
                    }
                });
    }

    @Override
    public void openContact(String userId, String coverPicture, String description, String category) {
        AddContactUseCase addContactUseCase = new AddContactUseCase(ApiManager.getUserApi(), ContactStore.getInstance(), ApiManager.getBotApi(), BotDetailsStore.getInstance());
        addContactUseCase.execute(userId, false)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ContactResult>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        ApiError error = new ApiError(e);
                        botsView.showError(error.getTitle(), error.getMessage());
                    }

                    @Override
                    public void onNext(ContactResult contactResult) {
                        botsView.navigateToMessage(contactResult.getUsername(), coverPicture, description, category);
                    }
                });
    }

    @Override
    public void attachView(DiscoverBotsContract.View view) {
        this.botsView = view;
    }

    @Override
    public void detachView() {
        this.compositeSubscription.clear();
    }
}
