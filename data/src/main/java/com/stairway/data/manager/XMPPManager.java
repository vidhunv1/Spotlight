package com.stairway.data.manager;

import android.os.AsyncTask;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by vidhun on 26/07/16.
 */
public class XMPPManager implements Serializable{
    private static String userName;
    private static String password = "spotlight";

    private static AbstractXMPPConnection connection = null;

    public String getChatId() {
        return userName;
    }

    public XMPPManager(String userName, String pass) {
        XMPPTCPConnectionConfiguration.Builder config;
        this.userName = userName;

        config = XMPPTCPConnectionConfiguration.builder();
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        config.setUsernameAndPassword(this.userName, password);
        config.setHost(DataConfig.XMPP_HOST);
        config.setPort(DataConfig.XMPP_PORT);
        config.setServiceName(DataConfig.XMPP_SERVICE_NAME);
        config.setDebuggerEnabled(true);
        config.setSendPresence(true);

        connection = new XMPPTCPConnection(config.build());
        XMPPTCPConnection.setUseStreamManagementDefault(true);
        XMPPTCPConnection.setUseStreamManagementResumptionDefault(true);
    }

    public static String getJidFromUserName(String userName) {
        return userName+"@"+DataConfig.XMPP_SERVICE_NAME;
    }

    public static String getUserNameFromJid(String jid) {
        return jid.split("@")[0];
    }

    public static AbstractXMPPConnection getConnection(){
            return connection;
    }

    public boolean isConnected() {
        return connection!=null;
    }

    public void setPresenceOnline() {
        Presence presenceOnline = new Presence(Presence.Type.available);

        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                if(connection!=null)
                    try {
                        connection.sendStanza(presenceOnline);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                return null;
            }
        };
        connectionThread.execute();
    }

    public void setPresenceOffline() {
        Presence presenceOffline = new Presence(Presence.Type.unavailable);

        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                if(connection!=null)
                    try {
                        connection.sendStanza(presenceOffline);
                        if(isConnected())
                            connection.disconnect();
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
//                        initConnection();
                    }
                return null;
            }
        };
        connectionThread.execute();
    }
}
