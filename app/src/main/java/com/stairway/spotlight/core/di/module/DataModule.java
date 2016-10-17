package com.stairway.spotlight.core.di.module;

import android.content.Context;

import com.stairway.data.manager.XMPPManager;
import com.stairway.data.source.user.UserSessionStore;
import com.stairway.data.source.user.UserAuthApi;
import com.stairway.data.source.contacts.ContactsContent;
import com.stairway.data.source.message.MessageApi;
import com.stairway.data.source.message.MessageStore;
import com.stairway.spotlight.core.di.scope.ApplicationScope;
import com.stairway.spotlight.core.di.scope.UserSessionScope;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 20/07/16.
 */

@Module
public class DataModule {
    @Provides
    @ApplicationScope
    public UserSessionStore userSessionStore(Context context) {
        return new UserSessionStore(context);
    }

    @Provides
    @ApplicationScope
    public UserAuthApi userSessionApi() {
        return new UserAuthApi();
    }

    @Provides
    @UserSessionScope
    public MessageApi messageApi(XMPPManager xmppManager) {
        return new MessageApi(xmppManager);
    }

    @Provides
    @UserSessionScope
    public MessageStore messageStore() {
        return new MessageStore();
    }

    @Provides
    @UserSessionScope
    public ContactsContent contactsContent(Context context) {
        return new ContactsContent(context);
    }
}
