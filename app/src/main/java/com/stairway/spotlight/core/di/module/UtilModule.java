package com.stairway.spotlight.core.di.module;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.stairway.data.local.GenericCache;
import com.stairway.spotlight.application.SpotlightApplication;
import com.stairway.spotlight.core.di.scope.ApplicationScope;

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
