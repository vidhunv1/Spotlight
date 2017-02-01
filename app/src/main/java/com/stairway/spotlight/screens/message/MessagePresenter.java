package com.stairway.spotlight.screens.message;

import com.stairway.spotlight.MessageController;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.core.UseCaseSubscriber;
import com.stairway.spotlight.db.ContactStore;
import com.stairway.spotlight.db.MessageStore;
import com.stairway.spotlight.models.MessageResult;
import com.stairway.spotlight.screens.message.view_models.TextMessage;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.chatstates.ChatState;

import java.util.List;

import rx.Subscriber;
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

    private GetNameUseCase getNameUseCase;

    private MessageStore messageStore;
    private MessageController messageController;

    public MessagePresenter(MessageStore messageStore, MessageController messageController, ContactStore contactStore) {
        this.getNameUseCase = new GetNameUseCase(contactStore);

        this.messageController = messageController;
        this.messageStore = messageStore;
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
        Subscription subscription = messageStore.getMessages(chatId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(messageResults -> {
                    messageView.displayMessages(messageResults);
                });

        compositeSubscription.add(subscription);
    }

    @Override
    public void updateMessageRead(MessageResult result) {
        result.setMessageStatus(MessageResult.MessageStatus.SEEN);
        Subscription subscription = messageStore.updateMessage(result)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(messageResult -> {
                    messageController.sendReadReceiptUseCase.execute(result).subscribe(isReceiptSent -> {
                        // is_receipt_sent updated if true
                    });
                });
        compositeSubscription.add(subscription);
    }

    @Override
    public void sendTextMessage(String toId, String fromId, TextMessage message) {
        MessageResult result = new MessageResult(toId, fromId, message.toXML());
        result.setMessageStatus(MessageResult.MessageStatus.NOT_SENT);

        Subscription subscription = messageStore.storeMessage(result)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MessageResult>() {
                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(MessageResult messageResult) {
                        messageView.addMessageToList(messageResult);
                    }

                    @Override
                    public void onCompleted() {
                        Subscription sendMessage = messageController.sendMessageUseCase.execute(result)
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
        Subscription subscription = messageController.sendChatStateUseCase.execute(chatId, chatState)
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
        Subscription subscription = messageController.getPresenceUseCase.execute(chatId)
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
        Subscription subscription = messageController.sendReadReceiptUseCase.execute(chatId)
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
