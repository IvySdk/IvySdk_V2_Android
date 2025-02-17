package com.ivy.sdk.base.net

import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class HttpUtil private constructor() {

    companion object {
        val Instance: HttpUtil by lazy(LazyThreadSafetyMode.NONE) { HttpUtil() }
    }

    private var _okhttpClient: OkHttpClient? = null

    val okHttpClient: OkHttpClient
        get() {
            if (_okhttpClient == null) {
                _okhttpClient = OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build()
            }
            return _okhttpClient!!
        }

    fun request(request: Request, callback: Callback) =
        okHttpClient.newCall(request).enqueue(callback)


}