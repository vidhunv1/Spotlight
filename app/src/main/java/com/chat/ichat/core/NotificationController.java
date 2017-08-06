package com.chat.ichat.core;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.chat.ichat.MessageController;
import com.chat.ichat.screens.home.HomeTabActivity;
import com.google.gson.JsonSyntaxException;
import com.chat.ichat.ForegroundDetector;
import com.chat.ichat.R;
import com.chat.ichat.XMPPManager;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.bot.BotApi;
import com.chat.ichat.api.bot.BotResponse;
import com.chat.ichat.api.user.UserApi;
import com.chat.ichat.api.user.UserResponse;
import com.chat.ichat.api.user._User;
import com.chat.ichat.application.SpotlightApplication;
import com.chat.ichat.db.BotDetailsStore;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.db.MessageStore;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.models.Message;
import com.chat.ichat.models.MessageResult;
import com.chat.ichat.screens.message.MessageActivity;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.roster.Roster;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.chat.ichat.MessageService.XMPP_ACTION_RCV_MSG;
import static com.chat.ichat.MessageService.XMPP_RESULT_CONTACT;
import static com.chat.ichat.MessageService.XMPP_RESULT_MESSAGE;

/**
 * Created by vidhun on 24/02/17.
 */

public class NotificationController {
    public static final int MESSAGE_NOTIFICATION_ID = 435345;
    private static NotificationController instance;

    public static NotificationController getInstance() {
        if(instance == null) {
            instance = new NotificationController();
        }
        return instance;
    }

    private ContactStore contactStore;
    private MessageStore messageStore;
    private UserApi userApi;
    private BotDetailsStore botDetailsStore;
    private BotApi botApi;
    private NotificationController() {
        this.contactStore = ContactStore.getInstance();
        this.messageStore = MessageStore.getInstance();
        this.userApi = ApiManager.getUserApi();
        this.botDetailsStore = BotDetailsStore.getInstance();
        this.botApi = ApiManager.getBotApi();
    }

    public void handleNewMessageNotification(String username, String messageJson, String messageId) {
        Logger.d(this, "username: "+username+", messageJson: "+messageJson);
        MessageResult newMessage = new MessageResult(username, username, messageJson);
        newMessage.setMessageStatus(MessageResult.MessageStatus.UNSEEN);
        newMessage.setReceiptId(messageId);
        newMessage.setTime(DateTime.now());

        contactStore.getContactByUserName(newMessage.getChatId()).subscribe(new Subscriber<ContactResult>() {
            @Override
            public void onCompleted() {}

            @Override
            public void onError(Throwable e) {}

            @Override
            public void onNext(ContactResult contactResult) {
                if(contactResult == null) {
                    userApi.findUserByUserName(newMessage.getChatId()).subscribe(new Subscriber<UserResponse>() {
                        @Override
                        public void onCompleted() {}

                        @Override
                        public void onError(Throwable e) {}

                        @Override
                        public void onNext(UserResponse userResponse) {
                            ContactResult contactResult1 = new ContactResult();
                            contactResult1.setUserId(userResponse.getUser().getUserId());
                            contactResult1.setContactName(userResponse.getUser().getName());
                            contactResult1.setUsername(userResponse.getUser().getUsername());
                            contactResult1.setAdded(false);
                            contactResult1.setBlocked(false);
                            contactResult1.setUserType(userResponse.getUser().getUserType());
                            contactResult1.setProfileDP(userResponse.getUser().getProfileDP());
                            MessageController.getInstance().getLastActivity(contactResult1.getUsername());

                            Roster roster = Roster.getInstanceFor(XMPPManager.getInstance().getConnection());
                            if (!roster.isLoaded()) {
                                try {
                                    roster.reloadAndWait();
                                } catch (SmackException.NotLoggedInException | SmackException.NotConnectedException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                roster.createEntry(XMPPManager.getJidFromUserName(contactResult1.getUsername()), contactResult1.getContactName(), null);
                            } catch (SmackException.NotLoggedInException | SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException e) {
                                e.printStackTrace();
                            }

                            contactStore.storeContact(contactResult1)
                                    .subscribe(new Subscriber<Boolean>() {
                                        @Override
                                        public void onCompleted() {}

                                        @Override
                                        public void onError(Throwable e) {}

                                        @Override
                                        public void onNext(Boolean aBoolean) {
                                            // TODO: Move to use case file!!
                                            if(contactResult1.getUserType()== _User.UserType.regular) {
                                                storeAndBroadcastReceivedMessage(messageStore, newMessage, contactResult1);
                                            } else if(contactResult1.getUserType() == _User.UserType.official){
                                                botApi.getBotDetails(contactResult1.getUsername())
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(new Subscriber<BotResponse>() {
                                                            @Override
                                                            public void onCompleted() {
                                                                Logger.d(this, "onComplete");
                                                            }

                                                            @Override
                                                            public void onError(Throwable e) {
                                                                Logger.d(this, "Error: "+e.getMessage());
                                                                e.printStackTrace();
                                                            }

                                                            @Override
                                                            public void onNext(BotResponse data) {
                                                                if(data.isSuccess()) {
                                                                    BotResponse.Data botResponse = data.getData();
                                                                    Logger.d(this, botResponse.toString());
                                                                    botDetailsStore.putMenu(botResponse.getUsername(), botResponse.getPersistentMenus())
                                                                            .subscribeOn(Schedulers.newThread())
                                                                            .observeOn(AndroidSchedulers.mainThread())
                                                                            .subscribe(new Subscriber<Boolean>() {
                                                                                @Override
                                                                                public void onCompleted() {
                                                                                }

                                                                                @Override
                                                                                public void onError(Throwable e) {
                                                                                    storeAndBroadcastReceivedMessage(messageStore, newMessage, contactResult1);
                                                                                }

                                                                                @Override
                                                                                public void onNext(Boolean aBoolean) {
                                                                                    storeAndBroadcastReceivedMessage(messageStore, newMessage, contactResult1);
                                                                                }
                                                                            });
                                                                } else {
                                                                    Logger.d(this, "Error response");
                                                                    Logger.d(this, data.getError().toString());
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    });
                } else {
                    if(contactResult.isBlocked()) {
                        // do nothing
                    } else {
                        storeAndBroadcastReceivedMessage(messageStore, newMessage, contactResult);
                    }
                }
            }
        });
    }

    public void showNotificationAndAlert(boolean shouldAlert) {
        messageStore.getUnseenMessages()
                .subscribe(messageResults -> {
                    if(messageResults==null || messageResults.size()==0) {
                        dismissNotification();
                        return;
                    }
                    Context context = SpotlightApplication.getContext();
                    List<String> uniqueUsernames = getUniqueUsernames(messageResults);

                    getContactNames(uniqueUsernames)
                            .subscribe(new Subscriber<Map<String, String>>() {
                                @Override
                                public void onCompleted() {}
                                @Override
                                public void onError(Throwable e) {}

                                @Override
                                public void onNext(Map<String, String> contactNames) {

                                    int messageCount = messageResults.size();
                                    String conv = "";
                                    String contentTitle = "";
                                    int MAX_COUNT = 7;

                                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(SpotlightApplication.getContext());
                                    NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

                                    if(uniqueUsernames.size() == 1) {
                                        Intent intent;
                                        if(ForegroundDetector.getInstance().isForeground()) {
                                             intent = MessageActivity.callingIntent(context, messageResults.get(0).getChatId());
                                        } else  {
                                             intent = HomeTabActivity.callingIntent(context, 1, messageResults.get(0).getChatId());
                                        }
                                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                                        mBuilder.setContentIntent(pendingIntent);
                                        if(messageCount==1) {
                                            conv = messageCount + " new message";
                                            mBuilder.setContentText(getDisplayMessage(messageResults.get(0).getMessage()));
                                        } else if(messageCount>1) {
                                            conv = messageCount + " new messages";
                                            mBuilder.setContentText(conv);
                                        }
                                        contentTitle = contactNames.get(uniqueUsernames.get(0));

                                        NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(R.drawable.ic_reply,
                                                "Reply", pendingIntent)
                                                .build();

                                        mBuilder.addAction(replyAction);
                                    } else {
                                        Intent intent = HomeTabActivity.callingIntent(context, 0, null);
                                        mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT));
                                        conv = messageCount+" messages from "+uniqueUsernames.size()+" chats";
                                        contentTitle = SpotlightApplication.getContext().getString(R.string.app_name);
                                        inboxStyle.setSummaryText(conv);
                                        mBuilder.setContentText(conv);
                                    }

                                    mBuilder.setContentTitle(contentTitle)
                                            .setSmallIcon(R.drawable.ic_logo_notif)
                                            .setAutoCancel(true)
                                            .setNumber(messageCount)
                                            .setGroup("messages")
                                            .setGroupSummary(true)
                                            .setColor(ContextCompat.getColor(context, R.color.colorPrimary));

                                    mBuilder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
                                    inboxStyle.setBigContentTitle(contentTitle);

                                    for (int i = 0; i < uniqueUsernames.size(); i++) {
                                        for (int j = 0; j <= (MAX_COUNT-1) && j < messageResults.size(); j++) {
                                            if(messageResults.get(j).getChatId().equals(uniqueUsernames.get(i))) {
                                                if(uniqueUsernames.size()==1) {
                                                    inboxStyle.addLine(getDisplayMessage(messageResults.get(j).getMessage()));
                                                } else {
                                                    inboxStyle.addLine(contactNames.get(uniqueUsernames.get(i)) + ": " +getDisplayMessage(messageResults.get(j).getMessage()));
                                                }
                                            }
                                        }
                                    }

                                    mBuilder.setStyle(inboxStyle);

                                    if (!shouldAlert) {
                                        mBuilder.setPriority(NotificationCompat.PRIORITY_LOW);
                                    } else {
                                        mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH)
                                                .setDefaults(Notification.DEFAULT_ALL);
                                    }

                                    NotificationManager notificationManager = (NotificationManager) context
                                            .getSystemService(Context.NOTIFICATION_SERVICE);
                                    notificationManager.notify(MESSAGE_NOTIFICATION_ID, mBuilder.build());
                                }
                            });
                });
    }

    public void updateNotification() {
        showNotificationAndAlert(false);
    }

    public void clearNotification() {
        Context context = SpotlightApplication.getContext();
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public void dismissNotification() {
        Context context = SpotlightApplication.getContext();
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(MESSAGE_NOTIFICATION_ID);
    }

    private String getDisplayMessage(String message) {
        try {
            Message parsedMessage = GsonProvider.getGson().fromJson(message, Message.class);
            return parsedMessage.getDisplayText();
        } catch (JsonSyntaxException e) {
            return message;
        }
    }

    private List<String> getUniqueUsernames(List<MessageResult> messageResults) {
        List<String> usernames = new ArrayList<>();
        boolean containsName = false;
        for (int i = 0; i < messageResults.size(); i++) {
            for (String username : usernames) {
                if(username.equals(messageResults.get(i).getChatId())) {
                    containsName = true;
                    break;
                }
            }
            if(!containsName) {
                usernames.add(messageResults.get(i).getChatId());
            }
            containsName = false;
        }
        return usernames;
    }

    private Observable<Map<String, String>> getContactNames(List<String> usernames) {
        List<Observable<ContactResult>> resultObservable  = new ArrayList<>();
        for (int i = 0; i < usernames.size(); i++) {
            resultObservable.add(contactStore.getContactByUserName(usernames.get(i)));
        }

        return Observable.merge(resultObservable).toList().map(contactResults -> {
            Map<String, String> map = new HashMap<>();
            for (ContactResult contactResult : contactResults) {
                map.put(contactResult.getUsername(), contactResult.getContactName());
            }
            return map;
        });
    }

    public void storeAndBroadcastReceivedMessage(MessageStore messageStore, MessageResult messageId, ContactResult from) {
        Logger.d("[NotificationControllerBroadcast]");

        messageStore.storeMessage(messageId)
                .subscribe(new Subscriber<MessageResult>() {
                    @Override
                    public void onCompleted() {
                        showNotificationAndAlert(true);
                    }
                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(MessageResult messageResult) {
                        if(ForegroundDetector.getInstance().isForeground()) {
                            Logger.d("[NotifiationController] Application in foregorund. Broadcasting message");
                            LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(SpotlightApplication.getContext());
                            Intent intent = new Intent(XMPP_ACTION_RCV_MSG);
                            intent.putExtra(XMPP_RESULT_MESSAGE, messageId);
                            intent.putExtra(XMPP_RESULT_CONTACT, from);
                            broadcaster.sendBroadcast(intent);
                        }
                    }
                });
    }
}
