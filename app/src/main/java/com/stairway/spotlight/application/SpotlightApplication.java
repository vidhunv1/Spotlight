package com.stairway.spotlight.application;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.stairway.data.config.Logger;
import com.stairway.data.db.core.DatabaseManager;
import com.stairway.spotlight.core.di.component.AppComponent;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.core.di.component.DaggerAppComponent;
import com.stairway.spotlight.core.di.module.AppModule;
import com.stairway.spotlight.core.di.module.DataModule;
import com.stairway.spotlight.core.di.module.UtilModule;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by vidhun on 05/07/16.
 */
public class SpotlightApplication extends Application {
    private ComponentContainer componentContainer;
    private String netBaseUrl;

    @Override
    public void onCreate() {
        super.onCreate();

        initDatabase();
        initDagger();

//         Setting default font
//        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
//                .setDefaultFontPath("fonts/rmedium.ttf")
//                .setFontAttrId(com.stairway.spotlight.R.attr.fontPath)
//                .build());

        if(com.stairway.spotlight.BuildConfig.DEBUG) {
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

        AppComponent appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .utilModule(new UtilModule())
                .dataModule(new DataModule())
                .build();

        componentContainer = new ComponentContainer(appComponent);
    }

    public void initDatabase() {
        DatabaseManager.init(this);
    }

    public ComponentContainer getComponentContainer() {
        return componentContainer;
    }
}
