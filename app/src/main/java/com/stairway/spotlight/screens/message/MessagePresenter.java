package com.stairway.spotlight.screens.message;

import com.stairway.data.manager.Logger;
import com.stairway.data.source.message.MessageResult;
import com.stairway.spotlight.core.UseCaseSubscriber;

import java.util.List;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 06/08/16.
 */
public class MessagePresenter implements MessageContract.Presenter {
    private MessageContract.View messageView;
    private CompositeSubscription compositeSubscription;
    private LoadMessagesUseCase getMessageUseCase;
    private StoreMessageUseCase storeMessageUseCase;
    private SendMessageUseCase sendMessageUseCase;
    private GetPresenceUseCase getPresenceUseCase;
    private UpdateMessageUseCase updateMessageUseCase;

    public MessagePresenter(LoadMessagesUseCase messageUseCase,
                            StoreMessageUseCase storeMessageUseCase,
                            SendMessageUseCase sendMessageUseCase,
                            GetPresenceUseCase getPresenceUseCase,
                            UpdateMessageUseCase updateMessageUseCase) {
        this.getMessageUseCase = messageUseCase;
        this.storeMessageUseCase = storeMessageUseCase;
        this.sendMessageUseCase = sendMessageUseCase;
        this.getPresenceUseCase = getPresenceUseCase;
        this.updateMessageUseCase = updateMessageUseCase;
        this.compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void loadMessages(String chatId) {
        Logger.d("[MessagePresenter]Loading chat messages: "+chatId);
        Subscription subscription = getMessageUseCase.execute(chatId)
                .observeOn(messageView.getUiScheduler())
                .subscribe(new UseCaseSubscriber<List<MessageResult>>(messageView) {
                    @Override
                    public void onResult(List<MessageResult> result) {
                        messageView.displayMessages(result);
                    }
                });

        compositeSubscription.add(subscription);
    }

    @Override
    public void updateMessageSeen(MessageResult result) {
        result.setMessageStatus(MessageResult.MessageStatus.SEEN);
        Subscription subscription = updateMessageUseCase.execute(result).subscribe(new UseCaseSubscriber<MessageResult>(messageView) {
            @Override
            public void onResult(MessageResult result) {
                //Updated message to seen
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
    public void getPresence(String chatId) {
        Subscription subscription = getPresenceUseCase.execute(chatId)
                .observeOn(messageView.getUiScheduler())
                .subscribe(new UseCaseSubscriber<String>(messageView) {
                    @Override
                    public void onResult(String result) {
                        messageView.updatePresence(result);
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
