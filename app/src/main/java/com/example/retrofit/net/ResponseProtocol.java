package com.example.retrofit.net;

public interface ResponseProtocol<T> {

    int code();

    String message();

    T getData();

}
