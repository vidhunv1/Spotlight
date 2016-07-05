package com.stairway.spotlight;

import android.app.Application;

import com.facebook.stetho.*;
import com.stairway.data.manager.DatabaseManager;
import com.stairway.data.manager.Logger;
import com.stairway.data.GenericCache;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by vidhun on 05/07/16.
 */
public class SpotlightApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        DatabaseManager.init(this);
        GenericCache gc = new GenericCache();


        // Setting default font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/DefaultFont.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );


        if(BuildConfig.DEBUG) {
            // Initialize facebook Stetho
            Stetho.initialize(
                    Stetho.newInitializerBuilder(this)
                            .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                            .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                            .build());

            Logger.initLogging();
        }

    }

}
