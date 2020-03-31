package com.example.retrofit.net.convert;

import androidx.annotation.NonNull;

import com.example.retrofit.net.ApiException;
import com.example.retrofit.net.BaseBean;
import com.example.retrofit.net.GsonUtils;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Converter;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CustomGsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private Gson gson;
    private TypeAdapter<T> adapter;

    CustomGsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }

    @Override
    public T convert(@NonNull ResponseBody value) throws IOException {
        String json = value.string();
        //处理异常
        BaseBean obj = GsonUtils.GsonToBean(json, BaseBean.class);
        if (obj.getCode() == 404) {
            //如果是服务端返回的错误码，则抛出自定义异常
            throw new ApiException(obj.getCode(), obj.getMsg());
        }
        value.close();
        /*return new Gson().fromJson(json, new TypeToken<BaseBean<T>>() {
        }.getType());*/
        MediaType contentType = value.contentType();
        Charset charset = contentType != null ? contentType.charset(UTF_8) : UTF_8;
        InputStream inputStream = new ByteArrayInputStream(json.getBytes());
        Reader reader = new InputStreamReader(inputStream, charset);
        JsonReader jsonReader = gson.newJsonReader(reader);

        try {
            return adapter.read(jsonReader);
        } finally {
            value.close();
        }
    }
}
