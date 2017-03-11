package com.stairway.spotlight.api;

import com.stairway.spotlight.api.bot.BotApi;
import com.stairway.spotlight.api.contacts.ContactsApi;
import com.stairway.spotlight.api.user.UserApi;
import com.stairway.spotlight.config.AppConfig;
import com.stairway.spotlight.core.Logger;

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

    private ContactsApi contactsApi;
    private UserApi userApi;
    private BotApi botApi;

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
        getInstance().contactsApi = null;
        getInstance().userApi = null;
        getInstance().botApi = null;
    }

    public static ContactsApi getContactsApi() {
        if(getInstance().contactsApi == null)
            getInstance().contactsApi = getInstance().retrofitClient.create(ContactsApi.class);
        return getInstance().contactsApi;
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
}

//    public Observable<UserResponse> uploadProfileDP(File imageFile, String fileName, String authToken) {
//        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile);
//        MultipartBody.Part imageFileBody = MultipartBody.Part.createFormData("profile_dp", fileName, requestBody);
//
//        UserEndpoint userEndpoint = ApiManager.getInstance(authToken).create(UserEndpoint.class);
//        Observable<UserResponse> profileDp = userEndpoint.uploadProfileDP(imageFileBody);
//        return profileDp;
//    }