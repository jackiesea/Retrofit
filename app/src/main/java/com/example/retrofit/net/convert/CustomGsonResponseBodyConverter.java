package com.example.retrofit.net.convert;

import androidx.annotation.NonNull;

import com.example.retrofit.net.ApiException;
import com.example.retrofit.net.BaseBean;
import com.example.retrofit.net.GsonUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

public class CustomGsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {

    CustomGsonResponseBodyConverter(Gson gson, TypeToken<T> typeToken) {

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
        return new Gson().fromJson(json, new TypeToken<BaseBean>() {
        }.getType());
    }
}
