//package com.stairway.spotlight.db;
//
//import com.stairway.spotlight.MessageController;
//import com.stairway.spotlight.XMPPManager;
//import com.stairway.spotlight.core.Logger;
//import com.stairway.spotlight.core.ReadReceiptExtension;
//import com.stairway.spotlight.models.MessageResult;
//
//import org.jivesoftware.smack.ConnectionListener;
//import org.jivesoftware.smack.SmackException;
//import org.jivesoftware.smack.XMPPConnection;
//import org.jivesoftware.smack.XMPPException;
//import org.jivesoftware.smack.chat.Chat;
//import org.jivesoftware.smack.chat.ChatManager;
//import org.jivesoftware.smack.packet.Message;
//import org.jivesoftware.smack.packet.Presence;
//import org.jivesoftware.smack.roster.Roster;
//import org.jivesoftware.smack.roster.RosterListener;
//import org.jivesoftware.smack.sm.StreamManagementException;
//import org.jivesoftware.smack.tcp.XMPPTCPConnection;
//import org.jivesoftware.smackx.chatstates.ChatState;
//import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
//import org.jivesoftware.smackx.iqlast.LastActivityManager;
//import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
//
//import java.util.Collection;
//
//import rx.Observable;
//import rx.Subscriber;
//
///**
// * Created by vidhun on 06/08/16.
// */
//public class MessageApi {
//    private static MessageApi instance;
//
//    public static void init(XMPPTCPConnection conn) {
//        instance = new MessageApi(conn);
//    }
//
//    public static MessageApi getInstance() {
//        if (instance == null) {
//            throw new IllegalStateException("[MessageController Not Initialized]");
//        }
//
//        return instance;
//    }
//
//    private XMPPTCPConnection connection;
//    public MessageApi(XMPPTCPConnection connection) {
//        this.connection = connection;
//    }
//
//
//}
