//package com.stairway.spotlight.screens.home;
//
//import com.stairway.spotlight.core.Logger;
//import com.stairway.spotlight.db.ContactStore;
//import com.stairway.spotlight.db.MessageStore;
//import com.stairway.spotlight.models.ContactResult;
//import com.stairway.spotlight.models.MessageResult;
//
//import java.util.ArrayList;
//import java.util.List;
//import rx.Observable;
//import rx.Subscriber;
//
///**
// * Created by vidhun on 11/10/16.
// */
//public class GetChatsUseCase {
//    private MessageStore messageStore;
//    private ContactStore contactStore;
//
//    public GetChatsUseCase(MessageStore messageStore, ContactStore contactStore) {
//        this.messageStore = messageStore;
//        this.contactStore = contactStore;
//    }
//
//    public Observable<List<ChatItem>> execute() {
//        Observable<List<ChatItem>> getContacts = Observable.create(subscriber -> {
//            messageStore.getChatList()
//                    .subscribe(new Subscriber<List<MessageResult>>() {
//                        @Override
//                        public void onCompleted() {
//                            subscriber.onCompleted();
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//
//                        }
//
//                        @Override
//                        public void onNext(List<MessageResult> messageResults) {
//                            List<ChatItem> chatItems = new ArrayList<>(messageResults.size());
//                            for (MessageResult messageResult : messageResults) {
//                                Logger.d(this, "MessageRslt: - "+messageResult.toString());
//                                contactStore.getContactByUserName(messageResult.getChatId()).subscribe(new Subscriber<ContactResult>() {
//                                    @Override
//                                    public void onCompleted() {}
//                                    @Override
//                                    public void onError(Throwable e) {}
//
//                                    @Override
//                                    public void onNext(ContactResult contactResult) {
//                                        String name;
//                                        if(contactResult!=null)
//                                            name = contactResult.getDisplayName();
//                                        else {
//                                            //TODO: get user details from server
//                                            name = messageResult.getChatId();
//                                        }
//
//                                        chatItems.add(new ChatItem(
//                                                messageResult.getChatId(),
//                                                name,
//                                                messageResult.getMessage(),
//                                                messageResult.getTime(),
//                                                messageResult.getUnSeenCount()));
//                                    }
//                                });
//                            }
//
//
//
//                            subscriber.onNext(chatItems);
//                            subscriber.onCompleted();
//                        }
//                    });
//        });
//        return getContacts;
//    }
//}
