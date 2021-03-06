package com.vng.datasync;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.StrictMode;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.vng.datasync.data.DataProvider;
import com.vng.datasync.data.ProfileRepository;
import com.vng.datasync.data.local.room.MessageIdGenerator;
import com.vng.datasync.data.local.room.RoomDatabaseManager;
import com.vng.datasync.data.model.Profile;
import com.vng.datasync.data.remote.ChatHandler;
import com.vng.datasync.data.remote.ServiceProvider;
import com.vng.datasync.data.remote.rest.response.ProfileResponse;
import com.vng.datasync.data.remote.rest.response.Response;
import com.vng.datasync.data.remote.websocket.FakeWebSocketManager;
import com.vng.datasync.data.remote.websocket.FakeWebsocketDataGenerator;
import com.vng.datasync.util.Logger;
import com.vng.datasync.util.ResponseSubscriber;
import com.vng.datasync.util.SimpleSubscriber;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Copyright (C) 2017, VNG Corporation.
 *
 * @author namnt4
 * @since 21/05/2018
 */

public class DataSyncApp extends Application {

    private static final boolean DEBUG = true;

    private static final Logger L = Logger.getLogger(DataSyncApp.class, DEBUG);

    private static volatile DataSyncApp sInstance = null;

    public static DataSyncApp getInstance() {
        return sInstance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;

        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
        }

//        ProfileRepository.getInstance()
//                .get(900332)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new SimpleSubscriber<Profile>() {
//                    @Override
//                    public void onNext(Profile profile) {
//                        if (profile == null) {
//                            L.e("profile is null");
//                        } else {
//                            final Gson gson = new Gson();
//                            L.e("profile = %s", gson.toJson(profile));
//                        }
//                    }
//                });
        ChatHandler.getInstance().init(this);

        MessageIdGenerator.getInstance().init();

        DataProvider.getInstance().init(this);

        Injector.providesWebSocketManager().connect();

        RoomDatabaseManager.getInstance().initForUser(DataSyncApp.getInstance());

//        FakeWebsocketDataGenerator.getInstance().setWebSocketManager(FakeWebSocketManager.getInstance());
//        FakeWebsocketDataGenerator.getInstance().startGenerator();
//        new Handler().postDelayed(() -> FakeWebsocketDataGenerator.getInstance().shutdownGenerator(), 5000);

        ServiceProvider.getUserService()
                .fetchProfile(900332)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResponseSubscriber<ProfileResponse>() {
                    @Override
                    public void onSuccess(ProfileResponse response) {
                        Profile data = response.getData();
                        final Gson gson = new Gson();
                        L.e("return from service =:> profile = %s", gson.toJson(data));
                    }
                });
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.with(this).onTrimMemory(level);
    }
}
