package com.ivy.sdk.remote.config

import com.ivy.sdk.base.net.HttpUtil
import okhttp3.Request


internal object RemoteDataTask {

    @Throws(Exception::class)
    fun loadData(url: String): String? {
        val okHttpClient = HttpUtil.Instance.okHttpClient
        val request = Request.Builder().url(url).get().build()
        val response = okHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
            return response.body?.string()
        }
        return null
    }


}