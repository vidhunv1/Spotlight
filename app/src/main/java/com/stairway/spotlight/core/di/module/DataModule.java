package com.stairway.spotlight.core.di.module;

import android.content.Context;

import com.stairway.data.config.XMPPManager;
import com.stairway.data.source.contacts.ContactApi;
import com.stairway.data.source.contacts.ContactContent;
import com.stairway.data.source.contacts.ContactStore;
import com.stairway.data.source.user.UserApi;
import com.stairway.data.source.user.UserSessionStore;
import com.stairway.data.source.message.MessageApi;
import com.stairway.data.source.message.MessageStore;
import com.stairway.spotlight.core.di.scope.ApplicationScope;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 20/07/16.
 */

@Module
public class DataModule {
//    @Provides
//    @ApplicationScope
//    public UserSessionStore userSessionStore(Context context) {
//        return new UserSessionStore(context);
//    }

    @Provides
    @ApplicationScope
    public UserApi userSessionApi() {
        return new UserApi();
    }

    @Provides
    @ApplicationScope
    public MessageApi messageApi() {
        return new MessageApi(XMPPManager.getConnection());
    }

    @Provides
    @ApplicationScope
    public MessageStore messageStore() {
        return new MessageStore();
    }

    @Provides
    @ApplicationScope
    public ContactContent contactsContent(Context context) {
        return new ContactContent(context);
    }

    @Provides
    @ApplicationScope
    public ContactStore contactStore() {
        return new ContactStore();
    }

    @Provides
    @ApplicationScope
    public ContactApi contactApi() {
        return new ContactApi();
    }
}
