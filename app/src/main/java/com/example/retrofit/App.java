package com.example.retrofit;

import android.app.Application;

import com.example.retrofit.net.RetrofitClient;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        RetrofitClient.getInstance().initRetrofit(this, "http://www.mxnzp.com");
    }
}
