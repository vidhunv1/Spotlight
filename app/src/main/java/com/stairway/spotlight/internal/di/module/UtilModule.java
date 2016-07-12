package com.stairway.spotlight.internal.di.module;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.stairway.data.GenericCache;
import com.stairway.spotlight.SpotlightApplication;
import com.stairway.spotlight.internal.di.scope.ApplicationScope;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 12/07/16.
 */
@Module
public class UtilModule {

    @Provides
    @ApplicationScope
    SharedPreferences providesSharedPreferences(SpotlightApplication application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides
    @ApplicationScope
    GenericCache providesGenericCache() {
        return GenericCache.getInstance();
    }
}
