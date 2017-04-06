package com.stairway.spotlight;

import android.os.AsyncTask;
import android.widget.Toast;

import com.stairway.spotlight.application.SpotlightApplication;
import com.stairway.spotlight.config.AppConfig;
import com.stairway.spotlight.core.ReadReceiptExtension;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by vidhun on 26/07/16.
 */
public class XMPPManager implements Serializable {
    private static XMPPManager instance;
    private String userName;
    private XMPPTCPConnection connection = null;

    public XMPPManager(String userName, String password) {
        XMPPTCPConnectionConfiguration.Builder config;
        this.userName = userName;

        config = XMPPTCPConnectionConfiguration.builder();
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        config.setUsernameAndPassword(this.userName, password);
        config.setHost(AppConfig.XMPP_HOST);
        config.setPort(AppConfig.XMPP_PORT);
        config.setServiceName(AppConfig.XMPP_SERVICE_NAME);
        config.setDebuggerEnabled(true);
        config.setSendPresence(true);

        connection = new XMPPTCPConnection(config.build());
        connection.setUseStreamManagement(true);
        connection.setUseStreamManagementResumption(true);
        XMPPTCPConnection.setUseStreamManagementDefault(true);
        XMPPTCPConnection.setUseStreamManagementResumptionDefault(true);
        DeliveryReceiptManager.getInstanceFor(this.getConnection()).setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.always);
        ProviderManager.addExtensionProvider(ReadReceiptExtension.ELEMENT, ReadReceiptExtension.NAMESPACE, new ReadReceiptExtension.Provider());
    }

    public static XMPPManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException();
        }

        return instance;
    }

    public static void init(String userName, String pass) {
        instance = new XMPPManager(userName, pass);
        instance.getConnection();
    }

    public static String getJidFromUserName(String userName) {
        return userName + "@" + AppConfig.XMPP_SERVICE_NAME;
    }

    public static String getUserNameFromJid(String jid) {
        return jid.split("@")[0];
    }

    public XMPPTCPConnection getConnection() {
        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                if(!connection.isConnected()) {
                    try {
                        connection.connect();
                    } catch (SmackException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    }
                }
                if(!connection.isAuthenticated()) {
                    try {
                        connection.login();
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    } catch (SmackException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
        connectionThread.execute();

        return connection;
    }

    public String getChatId() {
        return userName;
    }

//    public boolean isConnected() {
//        return connection!=null;
//    }

//    public void setPresenceOnline() {
//        Presence presenceOnline = new Presence(Presence.Type.available);
//
//        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {
//            @Override
//            protected Boolean doInBackground(Void... params) {
//                if(connection!=null)
//                    try {
//                        connection.sendStanza(presenceOnline);
//                    } catch (SmackException.NotConnectedException e) {
//                        e.printStackTrace();
//                    }
//                return null;
//            }
//        };
//        connectionThread.execute();
//    }
//
//    public void setPresenceOffline() {
//        Presence presenceOffline = new Presence(Presence.Type.unavailable);
//
//        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {
//            @Override
//            protected Boolean doInBackground(Void... params) {
//                if(connection!=null)
//                    try {
//                        connection.sendStanza(presenceOffline);
//                        if(isConnected())
//                            connection.disconnect();
//                    } catch (SmackException.NotConnectedException e) {
//                        e.printStackTrace();
////                        initConnection();
//                    }
//                return null;
//            }
//        };
//        connectionThread.execute();
//    }
}
