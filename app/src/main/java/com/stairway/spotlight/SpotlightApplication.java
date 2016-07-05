package com.stairway.spotlight;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by vidhun on 05/07/16.
 */
public class SpotlightApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build());
    }
}
