package com.chat.ichat.api;

import com.chat.ichat.api.app.AppApi;
import com.chat.ichat.api.bot.BotApi;
import com.chat.ichat.api.message.MessageApi;
import com.chat.ichat.api.phone_contacts.PhoneContactsApi;
import com.chat.ichat.api.user.UserApi;
import com.chat.ichat.config.AppConfig;
import com.chat.ichat.core.Logger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.schedulers.Schedulers;

/**
 * Created by vidhun on 16/10/16.
 */
public class ApiManager {
    private static ApiManager instance;

    public static ApiManager getInstance() {
        if (instance == null) {
            instance = new ApiManager();
        }
        return instance;
    }

    private Retrofit retrofitClient;

    private PhoneContactsApi phoneContactsApi;
    private UserApi userApi;
    private BotApi botApi;
    private AppApi appApi;
    private MessageApi messageApi;

    public ApiManager() {
        RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));

        this.retrofitClient = new Retrofit.Builder()
                .baseUrl(AppConfig.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(rxAdapter)
                .client(httpClient.build())
                .build();
    }

    public void setAuthorization(String authToken) {
        Logger.d(this, "Setting auth: "+authToken);
        RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder().addHeader("Authorization", authToken).build();
                        return chain.proceed(request);
                });

        getInstance().retrofitClient = new Retrofit.Builder()
                .baseUrl(AppConfig.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(rxAdapter)
                .client(httpClient.build())
                .build();

        //TODO: Ugly. Need to do after access token is available.
        getInstance().phoneContactsApi = null;
        getInstance().userApi = null;
        getInstance().botApi = null;
        getInstance().appApi = null;
        getInstance().messageApi = null;
    }

    public static PhoneContactsApi getPhoneContactsApi() {
        if(getInstance().phoneContactsApi == null)
            getInstance().phoneContactsApi = getInstance().retrofitClient.create(PhoneContactsApi.class);
        return getInstance().phoneContactsApi;
    }

    public static UserApi getUserApi() {
        if(getInstance().userApi == null) {
            getInstance().userApi = getInstance().retrofitClient.create(UserApi.class);
        }
        return getInstance().userApi;
    }

    public static BotApi getBotApi() {
        if(getInstance().botApi == null) {
            getInstance().botApi = getInstance().retrofitClient.create(BotApi.class);
        }
        return getInstance().botApi;
    }

    public static AppApi getAppApi() {
        if(getInstance().appApi == null) {
            getInstance().appApi = getInstance().retrofitClient.create(AppApi.class);
        }
        return getInstance().appApi;
    }

    public static MessageApi getMessageApi() {
        if(getInstance().messageApi == null) {
            getInstance().messageApi = getInstance().retrofitClient.create(MessageApi.class);
        }
        return getInstance().messageApi;
    }

    public static void reset() {
        instance = null;
    }
}