package com.stairway.data.manager;

import android.os.AsyncTask;

import com.stairway.data.manager.Logger;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vidhun on 26/07/16.
 */
public class XMPPManager {
    private String userName;
    private String password;
    private String serviceName = "spotlight.p1.im";
    private String host = "hosted.im";
    private int port = 5222;
    private Presence presenceOnline;
    private Presence presenceOffline;

    private XMPPTCPConnectionConfiguration.Builder config;
    private static AbstractXMPPConnection connection = null;

    public XMPPManager(String userName, String password) {
        this.password = "spotlight";
        this.userName = userName;
        presenceOnline = new Presence(Presence.Type.available);
        presenceOffline = new Presence(Presence.Type.unavailable);

        config = XMPPTCPConnectionConfiguration.builder();
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        config.setUsernameAndPassword(this.userName, this.password);
        config.setHost(host);
        config.setPort(port);
        config.setServiceName(this.serviceName);
        config.setDebuggerEnabled(true);


        connection = new XMPPTCPConnection(config.build());
        XMPPTCPConnection.setUseStreamManagementDefault(true);
        XMPPTCPConnection.setUseStreamManagementResumptionDefault(true);
        initConnection();

//        connection = new XMPPTCPConnection(config);

    }

    public String getServiceName() {
        return serviceName;
    }

    public void initConnection(){
    /*    Observable<AbstractXMPPConnection> xmppConnection = Observable.create(subscriber -> {
            try {

                Logger.d("THread::"+Thread.currentThread().getName());
                AbstractXMPPConnection c = new XMPPTCPConnection(config).connect();
                //subscriber.onNext(c);
                subscriber.onNext(null);
                subscriber.onCompleted();
            } catch (Exception e) {subscriber.onError(e);}
//            catch (SmackException e) {
//                subscriber.onError(e);
//            } catch (IOException e) {
//                subscriber.onError(e);
//            } catch (XMPPException e) {
//                subscriber.onError(e);
//            }

        });

        xmppConnection.subscribeOn(Schedulers.io());
        xmppConnection.observeOn(AndroidSchedulers.mainThread());

        xmppConnection.subscribe(new Subscriber<AbstractXMPPConnection>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Logger.e(e.getMessage());
            }

            @Override
            public void onNext(AbstractXMPPConnection abstractXMPPConnection) {
//                connection = abstractXMPPConnection;
            }
        });*/




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

    public void setPresenceOnline() {

        if(connection!=null)
            try {
                connection.sendStanza(presenceOnline);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
                initConnection();
            }
    }

    public void setPresenceOffline() {
        Presence presence = new Presence(Presence.Type.unavailable);

        if(connection!=null)
            try {
                connection.sendStanza(presenceOffline);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
                initConnection();
            }
    }
}
