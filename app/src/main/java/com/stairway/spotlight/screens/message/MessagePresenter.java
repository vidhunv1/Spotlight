package com.stairway.spotlight.screens.message;

import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.core.UseCaseSubscriber;
import com.stairway.spotlight.models.MessageResult;
import com.stairway.spotlight.screens.message.view_models.TextMessage;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.chatstates.ChatState;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
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
    private SendChatStateUseCase sendChatStateUseCase;
    private SendReadReceiptUseCase sendReadReceiptUseCase;
    private GetNameUseCase getNameUseCase;

    public MessagePresenter(LoadMessagesUseCase messageUseCase,
                            StoreMessageUseCase storeMessageUseCase,
                            SendMessageUseCase sendMessageUseCase,
                            GetPresenceUseCase getPresenceUseCase,
                            UpdateMessageUseCase updateMessageUseCase,
                            SendChatStateUseCase sendChatStateUseCase,
                            SendReadReceiptUseCase sendReadReceiptUseCase,
                            GetNameUseCase getNameUseCase) {
        this.getMessageUseCase = messageUseCase;
        this.storeMessageUseCase = storeMessageUseCase;
        this.sendMessageUseCase = sendMessageUseCase;
        this.getPresenceUseCase = getPresenceUseCase;
        this.updateMessageUseCase = updateMessageUseCase;
        this.sendChatStateUseCase = sendChatStateUseCase;
        this.sendReadReceiptUseCase = sendReadReceiptUseCase;
        this.getNameUseCase = getNameUseCase;
        this.compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void getName(String username) {
        Subscription subscription = getNameUseCase.execute(username)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UseCaseSubscriber<String>(messageView) {
                    @Override
                    public void onResult(String result) {
                        messageView.setName(result);
                    }
                });

        compositeSubscription.add(subscription);
    }

    @Override
    public void loadMessages(String chatId) {
        Logger.d(this, "Loading chat messages: "+chatId);
        Subscription subscription = getMessageUseCase.execute(chatId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UseCaseSubscriber<List<MessageResult>>(messageView) {
                    @Override
                    public void onResult(List<MessageResult> result) {
                        messageView.displayMessages(result);
                    }
                });

        compositeSubscription.add(subscription);
    }

    @Override
    public void updateMessageRead(MessageResult result) {
        result.setMessageStatus(MessageResult.MessageStatus.SEEN);
        Subscription subscription = updateMessageUseCase.execute(result)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UseCaseSubscriber<MessageResult>(messageView) {
            @Override
            public void onResult(MessageResult result) {
                // send read receipt
                sendReadReceiptUseCase.execute(result).subscribe(new UseCaseSubscriber<Boolean>(messageView) {
                    @Override
                    public void onResult(Boolean result) {
                        // is_receipt_sent updated if true
                    }
                });
            }
        });
        compositeSubscription.add(subscription);
    }

    @Override
    public void sendTextMessage(String toId, String fromId, TextMessage message) {
        MessageResult result = new MessageResult(toId, fromId, message.toXML());
        result.setMessageStatus(MessageResult.MessageStatus.NOT_SENT);

        Subscription subscription = storeMessageUseCase.execute(result)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UseCaseSubscriber<MessageResult>(messageView) {
                    @Override
                    public void onResult(MessageResult message) {
                        messageView.addMessageToList(message);
                    }

                    @Override
                    public void onCompleted() {
                        Subscription sendMessage = sendMessageUseCase.execute(result)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new UseCaseSubscriber<MessageResult>(messageView) {
                                    @Override
                                    public void onResult(MessageResult result) {
                                        messageView.updateDeliveryStatus(result);
                                    }
                                });

                        compositeSubscription.add(sendMessage);
                    }
                });

        compositeSubscription.add(subscription);
    }

    @Override
    public void sendChatState(String chatId, ChatState chatState) {
        Subscription subscription = sendChatStateUseCase.execute(chatId, chatState)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UseCaseSubscriber<String>(messageView) {
                    @Override
                    public void onResult(String result) {
                        Logger.d(this, "ChatState: "+result);
                    }
                });

        compositeSubscription.add(subscription);
    }

    @Override
    public void getPresence(String chatId) {
        Subscription subscription = getPresenceUseCase.execute(chatId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UseCaseSubscriber<String>(messageView) {
                    @Override
                    public void onResult(String result) {
                        messageView.updatePresence(result);
                    }
                });

        compositeSubscription.add(subscription);
    }

    @Override
    public void sendReadReceipt(String chatId) {
        Subscription subscription = sendReadReceiptUseCase.execute(chatId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UseCaseSubscriber<Boolean>(messageView) {
                    @Override
                    public void onResult(Boolean result) {
                        //sent read receipt. Add to cache to send later.
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
