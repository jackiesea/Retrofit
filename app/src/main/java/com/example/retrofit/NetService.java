package com.example.retrofit;

import com.example.retrofit.net.BaseBean;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 可参见：
 * https://blog.csdn.net/guohaosir/article/details/78942485
 */

public interface NetService {
    @GET("/api/lottery/common/aim_lottery")
    Observable<BaseBean<Object>> getNetTest(@Query("expect") String expect, @Query("code") String code);
}
