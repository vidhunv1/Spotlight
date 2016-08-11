package com.stairway.spotlight.screens.message;

import com.stairway.data.manager.Logger;
import com.stairway.data.source.message.MessageResult;
import com.stairway.spotlight.core.UseCaseSubscriber;

import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 06/08/16.
 */
public class MessagePresenter implements MessageContract.Presenter {
    private MessageContract.View messageView;
    private CompositeSubscription compositeSubscription;
    private GetMessagesUseCase getMessageUseCase;
    private StoreMessageUseCase storeMessageUseCase;
    private SendMessageUseCase sendMessageUseCase;

    public MessagePresenter(GetMessagesUseCase messageUseCase, StoreMessageUseCase storeMessageUseCase, SendMessageUseCase sendMessageUseCase) {
        this.getMessageUseCase = messageUseCase;
        this.storeMessageUseCase = storeMessageUseCase;
        this.sendMessageUseCase = sendMessageUseCase;
        this.compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void loadMessages(String chatId) {
        Subscription subscription = getMessageUseCase.execute(chatId)
                .observeOn(messageView.getUiScheduler())
                .toList()
                .subscribe(new UseCaseSubscriber<List<MessageResult>>(messageView) {
                    @Override
                    public void onResult(List<MessageResult> result) {
                        messageView.displayMessages(result);
                    }
                });

        compositeSubscription.add(subscription);
    }

    @Override
    public void sendMessage(MessageResult result) {

        Subscription subscription = storeMessageUseCase.execute(result)
                .observeOn(messageView.getUiScheduler())
                .subscribe(new UseCaseSubscriber<MessageResult>(messageView) {
                    @Override
                    public void onResult(MessageResult message) {
                        messageView.addMessageToList(message);
                    }

                    @Override
                    public void onCompleted() {
                        Subscription sendMessage = sendMessageUseCase.execute(result)
                                .observeOn(messageView.getUiScheduler())
                                .subscribe(new UseCaseSubscriber<MessageResult>(messageView) {
                                    @Override
                                    public void onResult(MessageResult result) {
                                        Logger.d("Message send: "+result.toString());
                                        messageView.updateDeliveryStatus(result);
                                    }
                                });

                        compositeSubscription.add(sendMessage);
                    }
                });



        compositeSubscription.add(subscription);
    }

    @Override
    public void attachView(MessageContract.View view) {
        this.messageView = view;
    }

    @Override
    public void detachView() {
        compositeSubscription.clear();
        messageView = null;
    }
}
