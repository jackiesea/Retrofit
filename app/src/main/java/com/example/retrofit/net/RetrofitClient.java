package com.example.retrofit.net;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.example.retrofit.net.convert.CustomGsonConverterFactory;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import java.io.IOException;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class RetrofitClient {
    private static RetrofitClient INSTANCE = null;
    private Map<String, Retrofit> retrofitMap;
    private Map<String, Object> serviceMap;
    private OkHttpClient okHttpClient;
    private volatile String baseUrl;

    private RetrofitClient() {
        retrofitMap = new HashMap<>();
        serviceMap = new HashMap<>();
    }

    public static RetrofitClient getInstance() {
        if (INSTANCE == null) {
            synchronized (RetrofitClient.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RetrofitClient();
                }
            }
        }
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceClass) {
        return getService(baseUrl, serviceClass);
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(String baseUrl, Class<T> serviceClass) {
        Object service = serviceMap.get(serviceClass.getName());
        if (service == null) {
            Retrofit retrofit = getRetrofit(baseUrl);
            T t = retrofit.create(serviceClass);
            serviceMap.put(serviceClass.getName(), t);
            return t;
        }
        return (T) service;
    }

    public void initRetrofit(Context context, String baseUrl) {
        initRetrofit(context, baseUrl, null);
    }

    public void initRetrofit(Context context, String baseUrl, Proxy proxy) {
        String key = NetUtil.md5(baseUrl);
        if (!retrofitMap.containsKey(key)) {
            if (this.baseUrl == null) {
                this.baseUrl = baseUrl;
            }
            Retrofit.Builder builder = new Retrofit.Builder()
                    .client(getOkHttpClient(context, proxy))
                    .addConverterFactory(CustomGsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .baseUrl(baseUrl);
            retrofitMap.put(key, builder.build());
        }
    }

    public Retrofit getRetrofit() {
        return getRetrofit(baseUrl);
    }

    public Retrofit getRetrofit(String baseUrl) {
        String key = NetUtil.md5(baseUrl);
        Retrofit retrofit = retrofitMap.get(key);
        if (retrofit == null)
            throw new IllegalArgumentException("You has not injected the [ " + baseUrl + " ] retrofit client.");
        return retrofit;
    }

    private OkHttpClient getOkHttpClient(Context context, Proxy proxy) {
        if (okHttpClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(@NonNull Chain chain) throws IOException {
                    Request.Builder originRequestBuilder = chain.request().newBuilder();
                    for (Map.Entry<String, String> e : Header.getHeaderParams().entrySet()) {
                        if (!TextUtils.isEmpty(e.getValue())) {
                            originRequestBuilder.addHeader(e.getKey(), e.getValue());
                        }
                    }
                    return chain.proceed(originRequestBuilder.build());
                }
            });
            builder.addInterceptor(new HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY));
            builder.connectTimeout(60, TimeUnit.SECONDS);
            builder.writeTimeout(60, TimeUnit.SECONDS);
            builder.readTimeout(60, TimeUnit.SECONDS);
            builder.cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context)));

            if (proxy != null) {
                builder.proxy(proxy);
            }
            return okHttpClient = builder.build();
        } else {
            return okHttpClient;
        }
    }
}
