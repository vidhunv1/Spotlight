package com.stairway.spotlight.core;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.stairway.spotlight.R;
import com.stairway.spotlight.application.SpotlightApplication;
import com.stairway.spotlight.db.ContactStore;
import com.stairway.spotlight.db.MessageStore;
import com.stairway.spotlight.models.ContactResult;
import com.stairway.spotlight.models.MessageResult;
import com.stairway.spotlight.screens.home.HomeActivity;
import com.stairway.spotlight.screens.message.MessageActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;

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
    private NotificationController() {
        this.contactStore = ContactStore.getInstance();
        this.messageStore = MessageStore.getInstance();
    }

    public void handleNewMessageNotification(String username, String messageJson, String messageId) {
        Logger.d(this, "username: "+username+", messageJson: "+messageJson);
        MessageResult newMessage = new MessageResult(username, username, messageJson);
        newMessage.setMessageStatus(MessageResult.MessageStatus.UNSEEN);
        newMessage.setReceiptId(messageId);

        messageStore.storeMessage(newMessage)
                .subscribe(new Subscriber<MessageResult>() {
                    @Override
                    public void onCompleted() {
                        showNotification(true);
                    }
                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(MessageResult messageResult) {}
                });
    }

    public void showNotification(boolean shouldAlert) {
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
                                    String content = "";
                                    String conv = "";
                                    String contentTitle = "";

                                    for (int i = 0; i < uniqueUsernames.size(); i++) {
                                        for (int j = 0; j < messageResults.size(); j++) {
                                            if(messageResults.get(j).getChatId().equals(uniqueUsernames.get(i))) {
                                                if(uniqueUsernames.size()==1) {
                                                    content = content + messageResults.get(j).getMessage() + "\n";
                                                } else {
                                                    content = content + contactNames.get(messageResults.get(i).getChatId()) + ": " + messageResults.get(j).getMessage() + "\n";
                                                }
                                            }
                                        }
                                    }

                                    Intent intent;
                                    PendingIntent pendingIntent;

                                    if(uniqueUsernames.size() == 1) {
                                        intent = MessageActivity.callingIntent(context, uniqueUsernames.get(0), contactNames.get(uniqueUsernames.get(0)));
                                        pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                                        if(messageCount==1) {
                                            conv = messageCount + " new message";
                                        } else if(messageCount>1) {
                                            conv = messageCount + " new messages";
                                        }
                                        contentTitle = contactNames.get(uniqueUsernames.get(0));
                                    } else {
                                        intent = HomeActivity.callingIntent(context);
                                        pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

                                        conv = messageCount+" messages from "+uniqueUsernames.size()+" conversations";
                                        contentTitle = "iChat";
                                    }

                                    NotificationCompat.Builder mBuilder;

                                    if(shouldAlert) {
                                        mBuilder = new NotificationCompat.Builder(context)
                                                .setSmallIcon(R.mipmap.ic_launcher)
                                                .setAutoCancel(true)
                                                .setContentTitle(contentTitle)
                                                .setDefaults(Notification.DEFAULT_ALL)
                                                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                                                .setSubText(conv)
                                                .setContentText(content)
                                                .setContentIntent(pendingIntent)
                                                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                                                .setPriority(Notification.PRIORITY_HIGH);
                                    } else {
                                        mBuilder = new NotificationCompat.Builder(context)
                                                .setSmallIcon(R.mipmap.ic_launcher)
                                                .setAutoCancel(true)
                                                .setContentTitle(contentTitle)
                                                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                                                .setSubText(conv)
                                                .setContentText(content)
                                                .setContentIntent(pendingIntent)
                                                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                                                .setPriority(Notification.PRIORITY_LOW);
                                    }

                                    NotificationManager notificationManager = (NotificationManager) context
                                            .getSystemService(Context.NOTIFICATION_SERVICE);
                                    notificationManager.notify(MESSAGE_NOTIFICATION_ID, mBuilder.build());
                                }
                            });
                });
    }

    public void updateNotification() {
        showNotification(false);
    }

    public void dismissNotification() {
        Context context = SpotlightApplication.getContext();
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(MESSAGE_NOTIFICATION_ID);
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
}
