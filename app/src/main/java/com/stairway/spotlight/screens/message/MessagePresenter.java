package com.stairway.spotlight.screens.message;

import com.stairway.spotlight.MessageController;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.core.UseCaseSubscriber;
import com.stairway.spotlight.db.ContactStore;
import com.stairway.spotlight.db.MessageStore;
import com.stairway.spotlight.models.ContactResult;
import com.stairway.spotlight.models.MessageResult;

import org.jivesoftware.smack.packet.Presence;
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

    private MessageStore messageStore;
    private ContactStore contactStore;
    private MessageController messageController;

    private SendMessageUseCase sendMessageUseCase;
    private SendReadReceiptUseCase sendReadReceiptUseCase;

    public MessagePresenter(MessageStore messageStore, MessageController messageController, ContactStore contactStore) {
        this.contactStore = contactStore;

        this.messageController = messageController;
        this.messageStore = messageStore;

        sendReadReceiptUseCase = new SendReadReceiptUseCase(messageController, messageStore);
        sendMessageUseCase = new SendMessageUseCase(messageController, messageStore);
        this.compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void getName(String username) {
        Subscription subscription = contactStore.getContactByUserName(username)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ContactResult>() {
            @Override
            public void onCompleted() {}
            @Override
            public void onError(Throwable e) {}

            @Override
            public void onNext(ContactResult contactResult) {
                Logger.d(this, contactResult.toString());
                if(!contactResult.getContactName().isEmpty())
                    messageView.setName(contactResult.getDisplayName());
                if(!contactResult.getDisplayName().isEmpty())
                    messageView.setName(contactResult.getDisplayName());
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
                .subscribe(new Subscriber<List<MessageResult>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Logger.e(this, "load messages");
                    }

                    @Override
                    public void onNext(List<MessageResult> messageResults) {
                        messageView.displayMessages(messageResults);
                    }
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
                    sendReadReceiptUseCase.execute(result).subscribe(isReceiptSent -> {
                        // is_receipt_sent updated if true
                    });
                });
        compositeSubscription.add(subscription);
    }

    @Override
    public void sendTextMessage(String toId, String fromId, String message) {
        MessageResult result = new MessageResult(toId, fromId, message);
        result.setMessageStatus(MessageResult.MessageStatus.NOT_SENT);

        Subscription subscription = messageStore.storeMessage(result)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MessageResult>() {
                    @Override
                    public void onError(Throwable e) {
                        Logger.d(this, "Store message error");
                    }

                    @Override
                    public void onNext(MessageResult messageResult) {
                        messageView.addMessageToList(messageResult);
                    }

                    @Override
                    public void onCompleted() {
                        sendMessageUseCase.execute(result)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new UseCaseSubscriber<MessageResult>(messageView) {
                                    @Override
                                    public void onResult(MessageResult result) {
                                        messageView.updateDeliveryStatus(result);
                                    }
                                });
                    }
                });
        compositeSubscription.add(subscription);
    }

    @Override
    public void sendChatState(String chatId, ChatState chatState) {
        Subscription subscription = messageController.sendChatState(chatId, chatState)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(Boolean aBoolean) {
                    }
                });

        compositeSubscription.add(subscription);
    }

    @Override
    public void getPresence(String chatId) {

        Subscription subscription = messageController.getLastActivity(chatId).subscribe(new Subscriber<Long>() {
            @Override
            public void onCompleted() {}
            @Override
            public void onError(Throwable e) {}

            @Override
            public void onNext(Long secAgo) {
                if(secAgo == 0)
                    messageView.updatePresence("Active now");
                else
                    messageView.updatePresence("Active "+secAgo+"s ago..");
            }
        });


        Subscription subscription1 = messageController.getPresence(chatId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Presence.Type>() {
                    @Override
                    public void onCompleted() {}
                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(Presence.Type presence) {
                        if (presence == Presence.Type.available) {
                            messageView.updatePresence("Active now");
                        } else {
                            messageController.getLastActivity(chatId).subscribe(new Subscriber<Long>() {
                                @Override
                                public void onCompleted() {}
                                @Override
                                public void onError(Throwable e) {}

                                @Override
                                public void onNext(Long secAgo) {
                                    messageView.updatePresence("Active "+secAgo+"s ago..");
                                }
                            });
                        }
                    }
                });

        compositeSubscription.add(subscription);
        compositeSubscription.add(subscription1);
    }

    @Override
    public void sendReadReceipt(String chatId) {
        // TODO: send only if there is an unsent receipt
        Subscription subscription = sendReadReceiptUseCase.execute(chatId)
                .subscribeOn(Schedulers.newThread())
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
        messageView = null;
        compositeSubscription.clear();
    }
}
