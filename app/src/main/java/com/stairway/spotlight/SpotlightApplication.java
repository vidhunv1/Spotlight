package com.stairway.spotlight;

import android.app.Application;

import com.facebook.stetho.*;
import com.stairway.data.manager.DatabaseManager;
import com.stairway.data.manager.Logger;
import com.stairway.data.GenericCache;
import com.stairway.spotlight.internal.di.component.DaggerNetComponent;
import com.stairway.spotlight.internal.di.component.NetComponent;
import com.stairway.spotlight.internal.di.module.NetModule;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by vidhun on 05/07/16.
 */
public class SpotlightApplication extends Application {
    private NetComponent netComponent;
    private String baseUrl = "http://spotlight.com";
    @Override
    public void onCreate() {
        super.onCreate();

        initdagger();
//        DatabaseManager.init(this);
//        GenericCache gc = new GenericCache();


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

            Logger.init();
        }

    }

    public void initdagger() {
        netComponent = DaggerNetComponent.builder()
                .netModule(new NetModule(baseUrl))
                .build();
    }


    public NetComponent getNetComponent() {
        return netComponent;
    }
}
