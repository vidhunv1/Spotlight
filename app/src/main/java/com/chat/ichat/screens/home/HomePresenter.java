package com.chat.ichat.screens.home;

import com.chat.ichat.MessageController;
import com.chat.ichat.api.app.AppApi;
import com.chat.ichat.api.app.VersionResponse;
import com.chat.ichat.core.Logger;
import com.chat.ichat.db.MessageStore;

import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 13/07/16.
 */
public class HomePresenter implements HomeContract.Presenter {
    private HomeContract.View contactsView;
    private MessageController messageController;
    private MessageStore messageStore;
    private CompositeSubscription compositeSubscription;
    private AppApi appApi;

    public HomePresenter(MessageController messageController, AppApi appApi, MessageStore messageStore) {
        this.messageController = messageController;
        this.appApi = appApi;
        this.messageStore = messageStore;
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void attachView(HomeContract.View view) {
        this.contactsView = view;
    }

    @Override
    public void detachView() {
        contactsView = null;
        compositeSubscription.clear();
    }

    @Override
    public void loadChatList() {
        Logger.d(this, " initChatList");
        Subscription subscription = messageController.getChatList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<ChatItem>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Logger.d(this, "Error initchatlist");
                    }

                    @Override
                    public void onNext(List<ChatItem> chatItems) {
                        contactsView.displayChatList(chatItems);
                    }
                });

        compositeSubscription.add(subscription);
    }

    @Override
    public void init(int currentVersionCode) {
        Subscription subscription = appApi.appVersion()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<VersionResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(VersionResponse versionResponse) {
                        if(versionResponse.getVersionCode() > currentVersionCode) {
                            contactsView.showUpdate(versionResponse.getVersionCode(), versionResponse.getVersionName(), versionResponse.isMandatory());
                        }
                    }
                });
        compositeSubscription.add(subscription);
    }

    @Override
    public void deleteChat(String chatId) {
        Subscription subscription = messageStore.deleteChat(chatId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        contactsView.removeChatItem(chatId);
                    }
                });
        compositeSubscription.add(subscription);
    }
}
