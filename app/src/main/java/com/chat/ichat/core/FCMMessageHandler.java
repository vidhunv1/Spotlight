package com.chat.ichat.core;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.chat.ichat.application.SpotlightApplication;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;



import java.util.Map;

import static com.chat.ichat.core.NotificationController.MESSAGE_NOTIFICATION_ID;

/**
 * Created by vidhun on 17/10/16.
 */

public class FCMMessageHandler extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
//        String from = remoteMessage.getFrom();
        Logger.d(this, "Received FCM");
        NotificationController.getInstance().handleNewMessageNotification(data.get("username"), data.get("message"), data.get("message_id"));
    }
}
