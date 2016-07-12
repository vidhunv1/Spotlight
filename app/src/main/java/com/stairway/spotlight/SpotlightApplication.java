package com.stairway.spotlight;

import android.app.Application;

import com.facebook.stetho.*;
import com.stairway.data.manager.DatabaseManager;
import com.stairway.data.manager.Logger;
import com.stairway.data.GenericCache;
import com.stairway.spotlight.internal.di.component.AppComponent;
import com.stairway.spotlight.internal.di.component.DaggerAppComponent;
import com.stairway.spotlight.internal.di.module.AppModule;
import com.stairway.spotlight.internal.di.module.NetModule;
import com.stairway.spotlight.internal.di.module.UtilModule;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by vidhun on 05/07/16.
 */
public class SpotlightApplication extends Application {
    private AppComponent appComponent;
    private String netBaseUrl;

    @Override
    public void onCreate() {
        super.onCreate();

        initDatabase();
        initDagger();


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

    public void initDagger() {

        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .utilModule(new UtilModule())
                .netModule(new NetModule(netBaseUrl))
                .build();
    }

    public void initDatabase() {
        DatabaseManager.init(this);
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}
