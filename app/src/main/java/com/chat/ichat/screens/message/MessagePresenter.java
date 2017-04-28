package com.chat.ichat.screens.message;

import com.chat.ichat.MessageController;
import com.chat.ichat.api.ApiError;
import com.chat.ichat.api.bot.BotApi;
import com.chat.ichat.api.bot.PersistentMenu;
import com.chat.ichat.api.user.UserApi;
import com.chat.ichat.api.user.UserResponse;
import com.chat.ichat.core.Logger;
import com.chat.ichat.db.BotDetailsStore;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.db.MessageStore;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.models.MessageResult;
import com.chat.ichat.screens.new_chat.AddContactUseCase;

import org.jivesoftware.smackx.chatstates.ChatState;

import java.util.List;

import rx.Observable;
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
    private ContactStore contactStore;
    private BotDetailsStore botDetailsStore;
    private UserApi userApi;
    private AddContactUseCase addContactUseCase;

    private SendMessageUseCase sendMessageUseCase;
    private SendReadReceiptUseCase sendReadReceiptUseCase;

    public MessagePresenter(MessageStore messageStore, MessageController messageController, BotDetailsStore botDetailsStore, ContactStore contactStore, UserApi userApi,  BotApi botApi) {
        this.messageController = messageController;
        this.messageStore = messageStore;
        this.botDetailsStore = botDetailsStore;
        this.contactStore = contactStore;
        this.userApi = userApi;
        this.addContactUseCase = new AddContactUseCase(userApi, contactStore, botApi, botDetailsStore);
        sendReadReceiptUseCase = new SendReadReceiptUseCase(messageController, messageStore);
        sendMessageUseCase = new SendMessageUseCase(messageController, messageStore);
        this.compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void addContact(String userId) {
        ContactResult contactResult = new ContactResult();
        contactResult.setUserId(userId);
        contactResult.setAdded(true);
        Subscription subscription = addContactUseCase.execute(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ContactResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        ApiError error = new ApiError(e);
                        messageView.showError(error.getTitle(), error.getMessage());
                    }

                    @Override
                    public void onNext(ContactResult contactResult) {
                        if(contactResult == null) {
                            messageView.showError("Error", "There was an error adding this contact.");
                        } else {
                            messageView.showContactAddedSuccess();
                        }
                    }
                });
        compositeSubscription.add(subscription);
    }

    @Override
    public void blockContact(String userId, boolean shouldBlock) {
        ContactResult contactResult = new ContactResult();
        contactResult.setUserId(userId);
        contactResult.setBlocked(shouldBlock);
        Observable<UserResponse> a;
        if(shouldBlock) {
            a = userApi.blockContact(userId);
        } else {
            a = userApi.unblockContact(userId);
        }
        Subscription subscription = a
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserResponse>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        ApiError error = new ApiError(e);
                        messageView.showError(error.getTitle(), error.getMessage());
                    }

                    @Override
                    public void onNext(UserResponse userResponse) {
                        contactResult.setBlocked(shouldBlock);
                        contactStore.update(contactResult)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<ContactResult>() {
                                    @Override
                                    public void onCompleted() {}

                                    @Override
                                    public void onError(Throwable e) {}

                                    @Override
                                    public void onNext(ContactResult contactResult) {
                                        messageView.showContactBlockedSuccess(shouldBlock);
                                    }
                                });
                    }
                });
        compositeSubscription.add(subscription);
    }

    @Override
    public void loadContactDetails(String chatUserName) {
        Subscription subscription = contactStore.getContactByUserName(chatUserName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ContactResult>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(ContactResult contactResult) {
                        messageView.setContactDetails(contactResult);
                    }
                });

        compositeSubscription.add(subscription);
    }

    @Override
    public void loadMessages(String chatUserName) {
        Logger.d(this, "Loading chat messages: "+chatUserName);
        Subscription subscription = messageStore.getMessages(chatUserName)
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
                        e.printStackTrace();
                        messageView.setKeyboardType(false);
                    }

                    @Override
                    public void onNext(List<PersistentMenu> persistentMenus) {
                        Logger.d(this, "PM "+persistentMenus.toString());
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
                                        messageView.updateDeliveryStatus(messageResult.getMessageId(), messageResult.getReceiptId(), messageResult.getMessageStatus());
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
        Subscription subscription = messageController.getLastActivity(chatId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {}
                    @Override
                    public void onError(Throwable e) {
                        messageView.updateLastActivity(null);
                    }

                    @Override
                    public void onNext(String time) {
                        messageView.updateLastActivity(time);
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
                .subscribe(onResult -> {});

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
