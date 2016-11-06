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

/**
 * Created by vidhun on 26/07/16.
 */
public class XMPPManager {
    private String userName;

    private static AbstractXMPPConnection connection = null;

    public String getChatId() {
        return userName;
    }

    public XMPPManager(String userName, String pass) {
        XMPPTCPConnectionConfiguration.Builder config;
        String password = "spotlight";
        this.userName = userName;

        config = XMPPTCPConnectionConfiguration.builder();
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        config.setUsernameAndPassword(this.userName, password);
        config.setHost(DataConfig.XMPP_HOST);
        config.setPort(DataConfig.XMPP_PORT);
        config.setServiceName(DataConfig.XMPP_SERVICE_NAME);
        config.setDebuggerEnabled(true);

        connection = new XMPPTCPConnection(config.build());
        XMPPTCPConnection.setUseStreamManagementDefault(true);
        XMPPTCPConnection.setUseStreamManagementResumptionDefault(true);
        initConnection();
    }

    public static String getJidFromUserName(String jid) {
        return jid+"@"+DataConfig.XMPP_SERVICE_NAME;
    }

    public void initConnection(){

        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... arg0) {

                // Create a connection
                try {
                    connection.connect().login();
                    Logger.d("Init xmpp connection");
                } catch (IOException e) {
                    Logger.e("[XMPP]: "+e);
                } catch (SmackException e) {
                    Logger.e("[XMPP]: "+e);

                } catch (XMPPException e) {
                    Logger.e("[XMPP]: "+e);
                }

                return null;
            }
        };
        connectionThread.execute();
        Logger.v("Connected xmpp");
    }

    public AbstractXMPPConnection getConnection(){
        if(connection!=null)
            return connection;
        else {
            initConnection();
            return connection;
        }
    }

    public boolean isConnected() {
        return connection!=null;
    }

    public void setPresenceOnline() {
        Presence presenceOnline = new Presence(Presence.Type.available);
        presenceOnline.setMode(Presence.Mode.available);

        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                if(connection!=null)
                    try {
                        connection.sendStanza(presenceOnline);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                        initConnection();
                    }
                return null;
            }
        };
        connectionThread.execute();
    }

    public void setPresenceOffline() {
        Presence presenceOffline = new Presence(Presence.Type.available);
        presenceOffline.setMode(Presence.Mode.away);

        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                if(connection!=null)
                    try {
                        connection.sendStanza(presenceOffline);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                        initConnection();
                    }
                return null;
            }
        };
        connectionThread.execute();
    }
}
