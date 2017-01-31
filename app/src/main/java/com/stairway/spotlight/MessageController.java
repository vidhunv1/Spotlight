package com.stairway.spotlight;

import com.stairway.spotlight.local.MessageApi;
import com.stairway.spotlight.local.MessageStore;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;

/**
 * Created by vidhun on 29/01/17.
 */

public class MessageController {
    private static MessageController instance;

    public static void init(XMPPTCPConnection conn, MessageApi messageApi, MessageStore messageStore) {
        instance = new MessageController(conn, messageApi, messageStore);
    }

    public static MessageController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("[EventService Not Initialized]");
        }

        return instance;
    }

    private XMPPTCPConnection conn;
    private MessageApi messageApi;
    private MessageStore messageStore;

    private MessageController(XMPPTCPConnection conn, MessageApi messageApi, MessageStore messageStore) {
        this.conn = conn;
        this.messageApi = messageApi;
        this.messageStore = messageStore;
    }
}
