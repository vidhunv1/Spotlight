package com.stairway.spotlight.screens.message;

import com.stairway.spotlight.MessageController;
import com.stairway.spotlight.api.bot.PersistentMenu;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.db.BotDetailsStore;
import com.stairway.spotlight.db.MessageStore;
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
    private MessageController messageController;
    private BotDetailsStore botDetailsStore;

    private SendMessageUseCase sendMessageUseCase;
    private SendReadReceiptUseCase sendReadReceiptUseCase;

    public MessagePresenter(MessageStore messageStore, MessageController messageController, BotDetailsStore botDetailsStore) {
        this.messageController = messageController;
        this.messageStore = messageStore;
        this.botDetailsStore = botDetailsStore;

        sendReadReceiptUseCase = new SendReadReceiptUseCase(messageController, messageStore);
        sendMessageUseCase = new SendMessageUseCase(messageController, messageStore);
        this.compositeSubscription = new CompositeSubscription();
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
    public void loadKeyboard(String chatId) {
        Subscription subscription= botDetailsStore.getMenu(chatId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<PersistentMenu>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        messageView.setKeyboardType(false);
                    }

                    @Override
                    public void onNext(List<PersistentMenu> persistentMenus) {
                        if(persistentMenus!=null && !persistentMenus.isEmpty()) {
                            messageView.setKeyboardType(true);
                            messageView.initBotMenu(persistentMenus);
                        } else {
                            messageView.setKeyboardType(false);
                        }
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
                                .subscribe(new Subscriber<MessageResult>() {
                                    @Override
                                    public void onCompleted() {}

                                    @Override
                                    public void onError(Throwable e) {}

                                    @Override
                                    public void onNext(MessageResult messageResult) {
                                        messageView.updateDeliveryStatus(messageResult);
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
    public void getLastActivity(String chatId) {

        Subscription subscription = messageController.getLastActivity(chatId).subscribe(new Subscriber<Long>() {
            @Override
            public void onCompleted() {}
            @Override
            public void onError(Throwable e) {}

            @Override
            public void onNext(Long secAgo) {
                if(secAgo == 0)
                    messageView.updatePresence(Presence.Type.available);
                else
                    messageView.updateLastActivity(secAgo);
            }
        });

        compositeSubscription.add(subscription);
    }

    @Override
    public void sendReadReceipt(String chatId) {
        // TODO: send only if there is an unsent receipt
        Subscription subscription = sendReadReceiptUseCase.execute(chatId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onResult -> {

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
